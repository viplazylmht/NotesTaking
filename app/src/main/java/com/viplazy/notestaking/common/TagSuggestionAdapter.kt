package com.viplazy.notestaking.common

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import kotlinx.android.synthetic.main.list_tag_suggestion_item.view.*
import java.util.*

class TagSuggestionAdapter(private val activity: Activity, private val layout: Int, private var list: List<String>) : BaseAdapter(), Filterable {

    var dummyListNote = list

    data class ViewHolder(val tv: TextView)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(activity)
            view = inflater.inflate(layout, parent)

            holder = ViewHolder(view.tv_tag)
            view.tag = holder
        }
        else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        holder.tv.text = getItem(position)

        return view
    }

    override fun getItem(position: Int): String {
        return dummyListNote[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return dummyListNote.size
    }

    override fun getFilter(): Filter {
        return myFilter
    }

    private val myFilter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<String> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(list)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim { it <= ' ' }
                for (item in list) {
                    if (item.toLowerCase(Locale.ROOT).contains(filterPattern)) {
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

            dummyListNote = results.values as List<String>
        }
    }

    fun updateList(newList: List<String>, filterString: String? = null) {

        if (filterString != null) {
            filter.filter(filterString)
        }
        else {
            list = newList
            dummyListNote = newList
        }

        notifyDataSetChanged()
    }
}