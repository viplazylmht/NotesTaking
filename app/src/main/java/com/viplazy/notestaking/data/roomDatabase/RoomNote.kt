package com.viplazy.notestaking.data.roomDatabase

import android.content.Context
import android.graphics.Bitmap
import androidx.room.*
import com.viplazy.notestaking.common.ImageUtil

@Entity (tableName = "NoteList")
data class RoomNote(
    @PrimaryKey(autoGenerate = true)
    val noteId: Int = 0,

    @ColumnInfo(name = "title")
    var title: String,

    var content: String,

    @ColumnInfo(name = "creationDate")
    val creationDate: String,

    @ColumnInfo(name = "creatorId")
    val creatorId: String,

    @ColumnInfo(name = "isDeleted")
    var isDeleted: Boolean = false,

    var lastModified: String = ""
)

@Entity(tableName = "ImageList")
data class NoteListImage(

    val noteId: Int,

    val path: String = "",

    val creationDate: String,

    val isDeleted: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val imageId: Long = 0) {

    @Ignore
    var isDeleteable = true

    @Ignore
    private var image: Bitmap? = null



    fun getImageBitmap(context: Context, path: String): Bitmap? {

        if (image == null) {
            image = ImageUtil.loadBitmap(context, path)
        }

        return image
    }

    fun setImageBitmap(value: Bitmap) {
        // to do
        // now only set - delete
        image = value
    }
}



