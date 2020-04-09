package com.viplazy.notestaking.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.viplazy.notestaking.data.roomDatabase.ImageDatabase
import com.viplazy.notestaking.data.roomDatabase.NoteListImage
import com.viplazy.notestaking.data.roomRepository.ImageRepository
import kotlinx.coroutines.launch

class ImagesViewModel(application: Application) : AndroidViewModel(application) {

    private val repoImage : ImageRepository
    var listImages: LiveData<List<NoteListImage>>? = null

    init {
        val imageDao = ImageDatabase.getDatabase(application, viewModelScope).imageDao()
        repoImage = ImageRepository(imageDao)
    }

    fun getImagesById(noteId: Int) = viewModelScope.launch {
        listImages = repoImage.getImagesById(noteId)
    }

    fun insert(vararg noteListImage: NoteListImage) = viewModelScope.launch {
        repoImage.insertAll(*noteListImage)
    }

    fun moveToBin(noteListImage: NoteListImage) = viewModelScope.launch {
        repoImage.moveToBin(noteListImage.imageId)
    }

    fun cleanUpStorage(context: Context) = viewModelScope.launch {
        repoImage.cleanDeletedImages(context)
    }

    fun restoreImage(imageId: Long) = viewModelScope.launch {
        repoImage.restoreImages(imageId)
    }
}