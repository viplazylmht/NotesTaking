package com.viplazy.notestaking.ui.main

import android.R.layout
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.CursorAdapter
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.viplazy.notestaking.R
import com.viplazy.notestaking.common.ListNoteAdapter
import com.viplazy.notestaking.common.TagSuggestionAdapter
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import kotlinx.android.synthetic.main.dialog_filter_layout.*
import kotlinx.android.synthetic.main.list_notes_fragment_layout.*
import java.util.*


class ListNotesFragment : Fragment(), ListNoteAdapter.NoteClickListener {

    private lateinit var noteAdapter : ListNoteAdapter

    companion object {
        const val SEARCH_QUERY_MODE = "searchQueryMode"
        const val SEARCH_QUERY_STRING = "searchQueryStr"

        const val SEARCH_MODE_NORMAL = 0
        const val SEARCH_MODE_TAG = 1

        fun showSnackBar(
            message: String = "Hello Kotlin!",
            duration: Int = Snackbar.LENGTH_LONG,
            parent: View?
        ) {

            if (parent != null) {
                Snackbar.make(parent, message, duration).show()
            }
        }

        fun showSnackBarTop(
            message: String = "Hello Kotlin on air!",
            duration: Int = Snackbar.LENGTH_LONG,
            parent: View?
        ) {

            if (parent != null) {
                val snackBarView = Snackbar.make(parent, message, duration)
                val view = snackBarView.view
                val params = view.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                view.layoutParams = params

                snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                snackBarView.show()
            }
        }
    }

    private lateinit var viewModel: NotesViewModel
    private lateinit var navController: NavController
    private lateinit var menu: Menu
    private lateinit var dialogFilter: Dialog

    private var listRoomNote: List<RoomNote>? = null

    private var queryString = ""
//    private var searchMode = SEARCH_MODE_NORMAL

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SEARCH_QUERY_STRING, queryString)
//        outState.putInt(SEARCH_QUERY_MODE, searchMode)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.list_notes_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        noteAdapter = ListNoteAdapter( activity!!, this)

        dialogFilter = Dialog(activity!!, R.style.Custom_Dialog)

        viewModel = ViewModelProviders.of(activity!!).get(NotesViewModel::class.java)

        savedInstanceState?.let {
            queryString = it.getString(SEARCH_QUERY_STRING, "")
//            searchMode = it.getInt(SEARCH_QUERY_MODE, SEARCH_MODE_NORMAL)
            activity!!.invalidateOptionsMenu()
        }

        list_note.apply {
            layoutManager = LinearLayoutManager(activity!!)
            adapter = noteAdapter

            // reject this feature
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        dialogFilter.apply {
            setContentView(R.layout.dialog_filter_layout)

            edt_start.setOnClickListener { editField ->

                val calendar = if (viewModel.startFilter.value != NotesViewModel.NO_DATE_FILTER) {
                    Calendar.getInstance().apply {
                        time = Date(viewModel.startFilter.value!! * 1000)
                    }
                }
                else {
                    Calendar.getInstance()
                }

                val (year, month, day) = listOf(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                val datePickerDialog = DatePickerDialog(
                    activity!!,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val date = getDate(year,  monthOfYear, dayOfMonth)

                        date?.let {
                            viewModel.startFilter.postValue(it.time / 1000)
                        }
                    },
                    year, month, day)

                datePickerDialog.setCancelable(false)
                datePickerDialog.setCanceledOnTouchOutside(false)
                datePickerDialog.setOnCancelListener {
                    viewModel.startFilter.postValue(NotesViewModel.NO_DATE_FILTER)
                }
                datePickerDialog.show()
            }

            edt_end.setOnClickListener { editField ->
                val calendar = if (viewModel.endFilter.value != NotesViewModel.NO_DATE_FILTER) {
                    Calendar.getInstance().apply {
                        time = Date(viewModel.endFilter.value!! * 1000)
                    }
                }
                else {
                    Calendar.getInstance()
                }

                val (year, month, day) = listOf(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                val datePickerDialog = DatePickerDialog(activity!!,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        val date = getDate(year,  monthOfYear, dayOfMonth)

                        date?.let {
                            viewModel.endFilter.postValue(it.time / 1000)
                        }
                    },
                    year, month, day)

                datePickerDialog.setCancelable(false)
                datePickerDialog.setCanceledOnTouchOutside(false)
                datePickerDialog.setOnCancelListener {
                    viewModel.endFilter.postValue(NotesViewModel.NO_DATE_FILTER)
                }
                datePickerDialog.show()
            }
        }

        viewModel.listNoteTitle.observe(viewLifecycleOwner, Observer {

            listRoomNote = it

            noteAdapter.updateList(it, viewModel.startFilter.value!!, viewModel.endFilter.value!!)
            noteAdapter.filter.filter(queryString)

            activity!!.invalidateOptionsMenu()
        })

        viewModel.selectedPos.observe(viewLifecycleOwner, Observer {pos ->
            noteAdapter.notifyItemSelected(pos)

            activity!!.invalidateOptionsMenu()
        })

        viewModel.startFilter.observe(viewLifecycleOwner, Observer { it ->
            it?.let { timestamp ->
                if (timestamp == NotesViewModel.NO_DATE_FILTER) {
                    dialogFilter.edt_start.setText("All")
                }
                else {
                    val date = Date(timestamp * 1000)
                    dialogFilter.edt_start.setText(String.format("%tb %td, %tY", date, date, date))
                }
            }

            listRoomNote?.let {list ->
                noteAdapter.updateList(list, viewModel.startFilter.value!!, viewModel.endFilter.value!!)
                noteAdapter.filter.filter(queryString)
            }
        })

        viewModel.endFilter.observe(viewLifecycleOwner, Observer {
            it?.let { timestamp ->
                if (timestamp == NotesViewModel.NO_DATE_FILTER) {
                    dialogFilter.edt_end.setText("All")
                }
                else {
                    val date = Date(timestamp * 1000)
                    dialogFilter.edt_end.setText(String.format("%tb %td, %tY", date, date, date))
                }
            }

            listRoomNote?.let {list ->
                noteAdapter.updateList(list, viewModel.startFilter.value!!, viewModel.endFilter.value!!)
                noteAdapter.filter.filter(queryString)

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.list_item_option_menu, menu)
        this.menu = menu

        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.queryHint = "Search notes"

        /*searchView.setOnCloseListener { //do what you want  searchview is not expanded

            container_filter.visibility = View.GONE
            false
        }



        searchView.setOnSearchClickListener {
            container_filter.visibility = View.VISIBLE
        }*/
        /*if (searchView.query != "") { // when user back to this fragment
            noteAdapter.filter.filter(searchView.query)
        }*/

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                noteAdapter.filter.filter(newText)

                queryString = newText

                return false
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (queryString != "") {
            (menu.findItem(R.id.app_bar_search).actionView as SearchView).setQuery(queryString, false)

//            container_filter.visibility = View.VISIBLE
        }
//        else {
//            container_filter.visibility = View.GONE
//        }

/*        with(menu.findItem(R.id.app_bar_search).actionView as SearchView) {
            suggestionsAdapter =  if (searchMode == SEARCH_MODE_TAG) {
                SimpleCursorAdapter(
                    activity,
                    layout.simple_list_item_1,
                    null,
                    viewModel.listTagName.value!!.toTypedArray(),
                    intArrayOf(android.R.id.text1),
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
            }
             else {
                null
            }
        }*/

        updateOptionMenu()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.app_bar_add -> {

                queryString = ""
                
                val bundle = bundleOf(NoteDetailFragment.NOTE_ID_INTEGER_EXTRA to NoteDetailFragment.DEFAULT_NOTE_ID)
                navController.navigate(R.id.action_listNotesFragment_to_noteDetailFragment, bundle)

            }
/*            R.id.app_bar_search -> {
                if ((menu.findItem(R.id.app_bar_search).actionView as SearchView).isIconified) {
                    // hide
                    container_filter.visibility = View.GONE
                }
                else {
                    container_filter.visibility = View.VISIBLE
                }
            }*/

            R.id.app_bar_edit -> {
                viewModel.selectedPos.postValue(RecyclerView.NO_POSITION)

                val itemId = noteAdapter.getSelectedNoteId()

                val bundle = bundleOf(NoteDetailFragment.NOTE_ID_INTEGER_EXTRA to itemId)
                navController.navigate(R.id.action_listNotesFragment_to_noteDetailFragment, bundle)
            }

            R.id.app_bar_delete -> {
                val itemId = noteAdapter.getSelectedNoteId()

                viewModel.moveNoteToBin(itemId)

                viewModel.selectedPos.postValue(RecyclerView.NO_POSITION)

                Snackbar.make(this.main, "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", View.OnClickListener {
                        viewModel.restoreNote(itemId)
                    })
                    .setActionTextColor(ContextCompat.getColor(activity!!, R.color.color_yellow))
                    .show()

                activity!!.invalidateOptionsMenu()
            }
        }

        return true
    }

    override fun onClick(view: View?, noteId: Int, isLongClick: Boolean) {
        when (isLongClick) {

            false -> {
                // showSnackBar("Clicked item " + position, parent = this.main)
                    val bundle = bundleOf(NoteDetailFragment.NOTE_ID_INTEGER_EXTRA to noteId)
                    navController.navigate(R.id.action_listNotesFragment_to_noteDetailFragment, bundle)
                }

            true -> {
                    viewModel.selectedPos.postValue(if (viewModel.selectedPos.value != noteId) noteId else RecyclerView.NO_POSITION)
                }
        }
    }

    private fun updateOptionMenu() {

        viewModel.selectedPos.value?.let {
            if (it != RecyclerView.NO_POSITION) {
                menu.findItem(R.id.app_bar_edit).setVisible(true)
                menu.findItem(R.id.app_bar_delete).setVisible(true)
                menu.findItem(R.id.app_bar_add).setVisible(false)
                menu.findItem(R.id.app_bar_search).setVisible(false)
            }
            else {
                menu.findItem(R.id.app_bar_edit).setVisible(false)
                menu.findItem(R.id.app_bar_delete).setVisible(false)
                menu.findItem(R.id.app_bar_add).setVisible(true)
                menu.findItem(R.id.app_bar_search).setVisible(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        view?.isFocusableInTouchMode = true
        view?.requestFocus()

        view?.setOnKeyListener( View.OnKeyListener { v, keyCode, event ->
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                if (viewModel.selectedPos.value!! != RecyclerView.NO_POSITION) {
                    viewModel.selectedPos.postValue(RecyclerView.NO_POSITION)
                } else {
                    activity!!.onBackPressed()
                }
            }
            return@OnKeyListener true
        })
    }

    private fun getDate(year: Int, month: Int, date: Int): Date? {
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = month
        cal[Calendar.DAY_OF_MONTH] = date

        // for begin of this day
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0

        return cal.time
    }
}
