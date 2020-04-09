package com.viplazy.notestaking.common

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.viplazy.notestaking.R
import com.viplazy.notestaking.data.roomDatabase.NoteListImage
import kotlinx.android.synthetic.main.list_images_item.view.*
import java.util.concurrent.Executors

class ListImageAdapter(private val activity: Activity, private val onItemClickListener: ImageClickListener,private val onItemDeleteListener: OnItemDeleteListener) : ListAdapter<NoteListImage, ListImageAdapter.DataViewHolder>(

    AsyncDifferConfig.Builder<NoteListImage>(ImageDiffCallBack())
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()) {

    interface ImageClickListener {
        fun onClick(
            view: View?,
            position: Int,
            isLongClick: Boolean
        )
    }

    interface OnItemDeleteListener {
        fun onDeleted(view: View?, imageId: Long, position: Int)
    }

    class DataViewHolder(itemView: View, var onItemClickListener: ImageClickListener, val activity: Activity, var onItemDeleteListener: OnItemDeleteListener?) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val img_picture: ImageView = itemView.findViewById<View>(R.id.img_item) as ImageView

        var imageId: Long = 0

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)

            itemView.btn_delete.setOnClickListener { v ->
                onItemDeleteListener?.onDeleted(v, imageId, adapterPosition)
            }
        }
        fun bindData(image: NoteListImage) {

            imageId = image.imageId

            itemView.btn_delete.visibility = if (image.isDeleteable) View.VISIBLE else View.GONE

            val bitmap = if (image.path == "%add%") {
                BitmapFactory.decodeResource(
                    activity.resources,
                    R.drawable.ic_add_images
                )
            }
            else {
                image.getImageBitmap(activity, image.path)
            }

            bitmap?.let {
                img_picture.setImageBitmap(bitmap)
            }
        }

        override fun onClick(v: View?) {
            onItemClickListener.onClick(itemView, adapterPosition, isLongClick = false)
        }

        override fun onLongClick(v: View?): Boolean {
            onItemClickListener.onClick(itemView, adapterPosition, isLongClick = true)

            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.list_images_item, parent, false)
        return DataViewHolder(listItem, onItemClickListener, activity, onItemDeleteListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }


}