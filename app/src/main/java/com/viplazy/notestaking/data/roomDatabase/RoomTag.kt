package com.viplazy.notestaking.data.roomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TagList")
data class NoteTag(

    @PrimaryKey
    val tagName: String,

    val creationDate: Long,

    var inNoteIds: String = "",

    var isDeleted: Boolean = false)