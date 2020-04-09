package com.viplazy.notestaking.data.roomRepository

import android.content.Context
import androidx.lifecycle.LiveData
import com.viplazy.notestaking.data.roomDao.ImageDao
import com.viplazy.notestaking.data.roomDatabase.NoteListImage
import com.viplazy.notestaking.data.roomDatabase.RoomNote

class ImageRepository(private val imageDao: ImageDao) {


    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    suspend fun insertAll(vararg image: NoteListImage) {
        imageDao.insertImages(*image)
    }

    suspend fun updateAll(vararg image: NoteListImage) {
        imageDao.updateImages(*image)
    }

    suspend fun deleteAll(vararg image: NoteListImage): Int {
        return imageDao.delete(*image)
    }

    suspend fun getImagesById(noteId: Int): LiveData<List<NoteListImage>> {
        return imageDao.getImagesById(noteId)
    }

    suspend fun cleanDeletedImages(context: Context) {
        imageDao.cleanUpStorage(context)
    }

    suspend fun moveToBin(imageId: Long) {
        imageDao.updateImageState(imageId, true)
    }

    suspend fun restoreImages(imageId: Long) {
        imageDao.updateImageState(imageId, false)
    }
}