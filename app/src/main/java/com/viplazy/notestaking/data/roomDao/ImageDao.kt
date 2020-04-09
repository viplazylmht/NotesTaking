package com.viplazy.notestaking.data.roomDao

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.viplazy.notestaking.data.roomDatabase.NoteListImage
import com.viplazy.notestaking.data.roomDatabase.RoomNote

@Dao

interface ImageDao {
    @Query("SELECT * FROM ImageList WHERE isDeleted = 0")
    fun getImages(): LiveData<List<NoteListImage>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertImages(vararg image: NoteListImage)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertImages(images: List<NoteListImage>)

    @Delete
    fun delete(vararg image: NoteListImage): Int

    @Query("DELETE FROM ImageList")
    suspend fun deleteAll()

    // Update notes by primary key (index)
    @Update
    fun updateImages(vararg image: NoteListImage): Int

    @Query("SELECT * FROM ImageList WHERE noteId = :noteId AND isDeleted = 0")
    fun getImagesById(noteId : Int): LiveData<List<NoteListImage>>

    @Query("SELECT path FROM ImageList WHERE isDeleted = 1")
    fun getDeletedImages() : List<String>

    fun cleanUpStorage(context: Context) {
        for (path : String in getDeletedImages()) {
            // delete image at this path
        }
    }

    @Query("UPDATE ImageList SET isDeleted = :isDeleted WHERE imageId = :imageId")
    fun updateImageState(imageId: Long, isDeleted: Boolean)
}