package com.viplazy.notestaking.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.viplazy.notestaking.R
import com.viplazy.notestaking.data.roomDatabase.NoteTag
import kotlinx.android.synthetic.main.fragment_add_tag.*


class AddTagFragment : Fragment() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var navController: NavController

    private var noteId: Int = NoteDetailFragment.DEFAULT_NOTE_ID

    private lateinit var adapter : ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_add_tag, container, false)
        (activity!! as AppCompatActivity).supportActionBar?.hide()
        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        arguments?.let {
            noteId = it.getInt(
                NoteDetailFragment.NOTE_ID_INTEGER_EXTRA,
                NoteDetailFragment.DEFAULT_NOTE_ID
            )
        }

        savedInstanceState?.let {
            noteId = it.getInt(
                NoteDetailFragment.NOTE_ID_INTEGER_EXTRA,
                NoteDetailFragment.DEFAULT_NOTE_ID
            )
        }

        edt_tag_name.setOnFocusChangeListener { v, hasFocus ->

        }

        edt_tag_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })

        btn_tag_cancel.setOnClickListener {
            navController.navigateUp()
        }

        btn_tag_done.setOnClickListener{

            val strTag = edt_tag_name.text.toString().trim()

            viewModel.curNote?.value?.let {
                viewModel.insertNoteId2Tag(strTag, it.noteId)
            }

            navController.navigateUp()
        }

        adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_list_item_1, listOf())
        list_avail_tag.adapter = adapter

        list_avail_tag.setOnItemClickListener { parent, view, position, id ->
            edt_tag_name.setText(adapter.getItem(position))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(NoteDetailFragment.NOTE_ID_INTEGER_EXTRA, noteId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(NotesViewModel::class.java)
        viewModel.getTagNamesByNotIncludeNoteId(viewModel.curNote?.value!!.noteId)

        viewModel.curTagsExclude?.observe(viewLifecycleOwner, Observer {

            it?.let {
                val listTag = mutableListOf<String>()
                for (tag: NoteTag in it) {
                    listTag.add(tag.tagName)
                }

                adapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, listTag)
                list_avail_tag.adapter = adapter
            }
        })


    }

    override fun onResume() {
        super.onResume()

        view?.isFocusableInTouchMode = true
        view?.requestFocus()

        view?.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                edt_tag_name.clearFocus()

                navController.navigateUp()
            }
            return@OnKeyListener true
        })

    }
}