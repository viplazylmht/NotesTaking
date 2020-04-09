package com.viplazy.notestaking.common

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.viplazy.notestaking.R
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import com.viplazy.notestaking.ui.main.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class ListNoteAdapter(private val activity: Activity, private val onItemClickListener: NoteClickListener) : ListAdapter<RoomNote, ListNoteAdapter.DataViewHolder> (
    AsyncDifferConfig.Builder<RoomNote>(NoteDiffCallBack())
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()), Filterable {

    var selectedPos = RecyclerView.NO_POSITION

    var dummyListNote = listOf<RoomNote>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNoteAdapter.DataViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.list_notes_item, parent, false)
        return DataViewHolder(listItem, onItemClickListener, activity)
    }

    class DataViewHolder(itemView: View, var onItemClickListener: NoteClickListener, val activity: Activity) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val tv_title: TextView = itemView.findViewById<View>(R.id.item_title) as TextView
        val tv_date_info: TextView = itemView.findViewById<View>(R.id.item_date_info) as TextView

        var noteId: Int = 0

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)

        }
        fun bindData(note: RoomNote) {

            noteId = note.noteId

            tv_title.text = note.title

            var txtdate = ""

            if (note.lastModified != "") {


                val dateModified = Date(note.lastModified.toLong() *1000)
                val modificationDate = String.format("%tb %td, %tY", dateModified, dateModified, dateModified)
                txtdate += activity.getString(R.string.item_date_info_modify, modificationDate)
            }

            val dateCreate = Date(note.creationDate.toLong() *1000)
            val creationDate = String.format("%tb %td, %tY", dateCreate, dateCreate, dateCreate)
            txtdate += " " + activity.getString(R.string.item_date_info_create, creationDate)

            tv_date_info.text = txtdate
        }

        override fun onClick(v: View?) {
            onItemClickListener.onClick(itemView, noteId, isLongClick = false)
        }

        override fun onLongClick(v: View?): Boolean {
            onItemClickListener.onClick(itemView, adapterPosition, isLongClick = true)


            return true
        }
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {

        holder.itemView.isSelected = (selectedPos == position)

        holder.bindData(getItem(position))

    }

    override fun getItemId(position: Int): Long {
        return getItem(position).noteId.toLong()
    }

    override fun submitList(list: List<RoomNote>?) {
        super.submitList(list ?: listOf())
    }

    interface NoteClickListener {
        fun onClick(
            view: View?,
            noteId: Int,
            isLongClick: Boolean
        )
    }

    fun notifyItemSelected(position: Int) {

        if (selectedPos == position && position == RecyclerView.NO_POSITION) return

        val lastpos = selectedPos
        selectedPos = position

        // rebindVH
        notifyItemChanged(lastpos)
        notifyItemChanged(selectedPos)
    }

    override fun getFilter(): Filter {

        return myFilter
    }

    private val myFilter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<RoomNote> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(dummyListNote)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim { it <= ' ' }
                for (item in dummyListNote) {
                    if (item.title.toLowerCase(Locale.ROOT).contains(filterPattern)
                        || item.content.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {

            submitList((results.values as List<RoomNote>))
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun updateList(newList: List<RoomNote>, filterStart: Long, filterEnd: Long, filterString: String? = null) {

        val list = mutableListOf<RoomNote>()

        val start = if (filterStart == NotesViewModel.NO_DATE_FILTER) {
            Long.MIN_VALUE
        }
        else filterStart

        val end = if (filterEnd == NotesViewModel.NO_DATE_FILTER) {
            Long.MAX_VALUE
        }
        else filterEnd + 86399 // next day

        for (note in newList) {
            val curDate = if (note.lastModified.isNotEmpty()) note.lastModified.toLong() else note.creationDate.toLong()
            if (curDate in start..end) list.add(note)
        }

        dummyListNote = list

        if (filterString != null) {
            filter.filter(filterString)
        }
        else {
            submitList(dummyListNote)
        }
    }

    fun getSelectedNoteId() = getItem(selectedPos).noteId
}