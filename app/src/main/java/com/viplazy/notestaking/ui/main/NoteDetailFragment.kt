package com.viplazy.notestaking.ui.main

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import com.cunoraz.tagview.Tag
import com.viplazy.notestaking.R
import com.viplazy.notestaking.common.ImageUtil
import com.viplazy.notestaking.common.ImageUtil.getThumbnail
import com.viplazy.notestaking.common.ListImageAdapter
import com.viplazy.notestaking.common.ListNoteAdapter
import com.viplazy.notestaking.data.roomDatabase.NoteListImage
import com.viplazy.notestaking.data.roomDatabase.NoteTag
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import com.viplazy.notestaking.ui.main.ListNotesFragment.Companion.showSnackBar
import kotlinx.android.synthetic.main.note_detail_fragment.*
import java.text.FieldPosition
import java.util.*

class NoteDetailFragment() : Fragment(), ListImageAdapter.ImageClickListener, ListImageAdapter.OnItemDeleteListener {

    companion object {
        const val DEFAULT_NOTE_ID = -1
        const val NOTE_ID_INTEGER_EXTRA = "integer_extra"
        const val THUMBNAIL_SIZE = 2000.0 // 2MB

        const val REQUEST_PERMISSION_STORAGE = 11
        const val PICK_FROM_GALLERY_TO_CHANGE = 102

        //Gallery storage permission required for Marshmallow version
        fun verifyStoragePermissions(activity: Activity?) { // Check if we have write permission
            val permission =
                ActivityCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            if (permission != PackageManager.PERMISSION_GRANTED) { // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_STORAGE
                )
            }
        }
    }

    private lateinit var viewModel: NotesViewModel
    private lateinit var viewModelImage: ImagesViewModel
    private lateinit var navController: NavController

    private lateinit var imageAdapter: ListImageAdapter

    private var noteId: Int = DEFAULT_NOTE_ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.note_detail_fragment, container, false)
        (activity!! as AppCompatActivity).supportActionBar?.show()
        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        arguments?.let {
            noteId = it.getInt(NOTE_ID_INTEGER_EXTRA, DEFAULT_NOTE_ID)
        }

        savedInstanceState?.let {
            noteId = it.getInt(NOTE_ID_INTEGER_EXTRA, DEFAULT_NOTE_ID)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(NOTE_ID_INTEGER_EXTRA, noteId)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(NotesViewModel::class.java)
        viewModelImage = ViewModelProviders.of(activity!!).get(ImagesViewModel::class.java)

        syncNoteId()

        imageAdapter = ListImageAdapter( activity!!, this, this)

        list_images.apply {
            layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL)
            adapter = imageAdapter

            // reject DividerDecoration
        }

        tag_group.setOnTagDeleteListener { view, tag, position ->
            tag_group.remove(position)

            viewModel.removeNoteIdFromTag(tag.text, viewModel.curNote?.value!!.noteId)
        }

        tag_group.setOnTagClickListener { tag, position ->
            if (tag.text == "+") {
                // navigate to add tag fragment
                navController.navigate(R.id.action_noteDetailFragment_to_addTagFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        edt_title.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                edt_title.clearFocus()
            }

            return@setOnKeyListener false
        }

        edt_content.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                edt_content.clearFocus()
            }

            return@setOnKeyListener false
        }

        view?.isFocusableInTouchMode = true
        view?.requestFocus()

        view?.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                val dialog = AlertDialog.Builder(activity!!).create()
                dialog.setMessage(getString(R.string.dialog_quit_message))
                dialog.setButton(Dialog.BUTTON_POSITIVE, "SAVE", DialogInterface.OnClickListener { dialog, which ->

                    saveNote()
                    dialog.dismiss()
                    navigateUp()
                })

                dialog.setButton(Dialog.BUTTON_NEGATIVE, "DISCARD", DialogInterface.OnClickListener { dialog, which ->

                    showSnackBar("Discarded", parent = v)
                    dialog.dismiss()
                    navigateUp()
                })

                dialog.setButton(Dialog.BUTTON_NEUTRAL, "CANCEL", DialogInterface.OnClickListener{ dialog, which ->
                    dialog.cancel()
                })

                val newNote = viewModel.curNote?.value
                newNote?.let {
                    if (edt_title.text.toString() != it.title
                        || edt_content.text.toString() != it.content) {
                            dialog.show()
                    }
                    else {
                        navigateUp()
                    }
                }

                if (newNote == null) {
                    if (edt_title.text.toString() != ""
                        || edt_content.text.toString() != "") {
                        dialog.show()
                    }
                    else {
                        navigateUp()
                    }
                }

                return@OnKeyListener true
            }
            return@OnKeyListener false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.detail_note_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.app_bar_save -> {
                saveNote()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveNote() {

        if (noteId != DEFAULT_NOTE_ID) {

            viewModel.curNote!!.value?.let {

                if (edt_title.text.toString() != it.title
                    || edt_content.text.toString() != it.content) {

                    it.title = edt_title.text.toString()
                    it.content = edt_content.text.toString()

                    val timeStamp = Calendar.getInstance().time.time / 1000

                    it.lastModified = timeStamp.toString()

                    viewModel.update(it)
                    ListNotesFragment.showSnackBar("Saved", parent = view)

                }
            }
        }
        else {
            // create a note

            val timeStamp = Calendar.getInstance().time.time / 1000

            val note = RoomNote(0, edt_title.text.toString(), edt_content.text.toString(), timeStamp.toString(), "339")

            viewModel.insertAndGetNote(note)

            syncNoteId()
        }
    }

    private fun syncNoteId() {
        if (noteId != DEFAULT_NOTE_ID) {
            viewModel.getNoteById(noteId)
        }

        viewModel.curNote?.observe(viewLifecycleOwner, Observer {note ->
            note?.let {

                edt_title.setText(note.title)
                edt_content.setText(note.content)

                if (noteId == DEFAULT_NOTE_ID) {
                    noteId = note.noteId
                }


                container_not_saved.visibility = View.GONE
                container_saved.visibility = View.VISIBLE

                syncTags()
                syncImages()
            }
        })
    }

    private fun syncImages() {
        viewModelImage.getImagesById(noteId)

        viewModelImage.listImages?.observe(viewLifecycleOwner, Observer {
            it?.let {

                val list = mutableListOf<NoteListImage>()
                list.addAll(it)

                val timeStamp = Calendar.getInstance().time.time / 1000
                list.add(NoteListImage(-1, "%add%", timeStamp.toString()).apply {
                    isDeleteable = false
                })

                imageAdapter.submitList(list)
            }
        })
    }

    private fun syncTags() {
        viewModel.getTagNamesByNoteId(noteId)

        viewModel.curTags?.observe(viewLifecycleOwner, Observer {
            it?.let {

                tag_group.removeAll()

                for (str:NoteTag in it) {

                    val tag = Tag(str.tagName)
                    tag.radius = 30f

                    tag.isDeletable = true
                    tag.tagTextColor = Color.parseColor("#ffb33333")
                    tag.layoutColor = Color.parseColor("#00000000")
                    tag.layoutBorderColor = Color.parseColor("#ff131313")
                    tag.layoutBorderSize = 1f

                    tag.deleteIndicatorColor = Color.parseColor("#ff131313")
                    tag.layoutColorPress = Color.parseColor("#ffffff00")

                    tag_group.addTag(tag)
                }

                val tagAdd = Tag("+")

                with(tagAdd) {

                    text = "+"
                    isDeletable = false
                    tagTextColor = Color.parseColor("#ff00ff00")
                    layoutColor = Color.parseColor("#00000000")
                    layoutBorderColor = Color.parseColor("#ff131313")
                    layoutBorderSize = 1f


                    tag_group.addTag(this)
                }
            }
        })
    }

    private fun navigateUp() {
        viewModel.curNote = null
        navController.navigateUp()
    }

    override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
        when (isLongClick) {

            true -> {
                // to do
            }

            false -> {
                if (imageAdapter.currentList[position].path == "%add%") {
                    requestImageFromDevice()
                }
            }
        }
    }

    private fun requestImageFromDevice() {
        verifyStoragePermissions(activity)

        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image!")
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            arrayOf(pickIntent)
        )

        startActivityForResult(chooserIntent, PICK_FROM_GALLERY_TO_CHANGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == Activity.RESULT_OK) {
                showSnackBar("Permission ALLOWED!", parent = this.main)
            }
            else {
                showSnackBar("Permission DENIED!", parent = this.main)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_FROM_GALLERY_TO_CHANGE -> {
                if (resultCode == Activity.RESULT_OK) {

                    val selectedImage: Uri? = data?.data

                    if (selectedImage != null) {

                        val timeStamp = Calendar.getInstance().time.time / 1000

                        val bitmap = getThumbnail(activity!!, selectedImage)

                        val file = ImageUtil.saveBitmap(activity!!, String.format("%d.png", timeStamp), bitmap!!)

                        val img = NoteListImage(viewModel.curNote?.value!!.noteId, file.canonicalPath, timeStamp.toString())

                        viewModelImage.insert(img)

                        showSnackBar("Image selected!", parent = this.main)
                    }
                } else {
                    showSnackBar("Can't select image!", parent = this.main)
                }
            }
        }
    }

    override fun onDeleted(view: View?, imageId: Long, position: Int) {
        viewModelImage.moveToBin(imageAdapter.currentList[position])
    }
}
