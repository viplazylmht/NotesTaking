package com.viplazy.notestaking.common

import androidx.recyclerview.widget.DiffUtil
import com.viplazy.notestaking.data.roomDatabase.NoteListImage

class ImageDiffCallBack : DiffUtil.ItemCallback<NoteListImage>() {
    override fun areItemsTheSame(oldItem: NoteListImage, newItem: NoteListImage): Boolean {
        return oldItem.imageId == newItem.imageId
    }

    override fun areContentsTheSame(oldItem: NoteListImage, newItem: NoteListImage): Boolean {
        return oldItem == newItem
    }
}