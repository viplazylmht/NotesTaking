package com.viplazy.notestaking.common

import androidx.recyclerview.widget.DiffUtil
import com.viplazy.notestaking.data.roomDatabase.RoomNote

class NoteDiffCallBack : DiffUtil.ItemCallback<RoomNote>() {
    override fun areItemsTheSame(oldItem: RoomNote, newItem: RoomNote): Boolean {
        return oldItem.noteId == newItem.noteId
    }

    override fun areContentsTheSame(oldItem: RoomNote, newItem: RoomNote): Boolean {
        return oldItem == newItem
    }
}