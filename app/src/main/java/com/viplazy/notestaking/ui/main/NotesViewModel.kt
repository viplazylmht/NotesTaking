package com.viplazy.notestaking.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.viplazy.notestaking.data.roomDatabase.NoteDatabase
import com.viplazy.notestaking.data.roomDatabase.NoteTag
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import com.viplazy.notestaking.data.roomDatabase.TagDatabase
import com.viplazy.notestaking.data.roomRepository.NoteRepository
import com.viplazy.notestaking.data.roomRepository.TagRepository
import kotlinx.coroutines.launch
import java.util.*

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val NO_DATE_FILTER: Long = -1
    }

    val selectedPos = MutableLiveData<Int>(RecyclerView.NO_POSITION)

    // filter
    val startFilter = MutableLiveData<Long>(NO_DATE_FILTER)
    val endFilter = MutableLiveData<Long>(NO_DATE_FILTER)
    val checkTagName = MutableLiveData<Boolean>(false)
    val tagsFilter = MutableLiveData<MutableList<String>>(mutableListOf())

    val listNoteFilter = MutableLiveData<List<RoomNote>>(listOf())


    private val repoNote : NoteRepository
    private val repoTag: TagRepository

    val listNoteTitle: LiveData<List<RoomNote>>
    val listTagName: LiveData<List<String>>

    var curNote: LiveData<RoomNote>? = null

    var curTags: LiveData<List<NoteTag>>? = null
    var curTagsExclude: LiveData<List<NoteTag>>? = null


    init {
        val noteDao = NoteDatabase.getDatabase(application, viewModelScope).noteDao()
        repoNote = NoteRepository(noteDao)

        listNoteTitle = repoNote.allNote

        val tagDao = TagDatabase.getDatabase(application, viewModelScope).tagDao()
        repoTag = TagRepository(tagDao)

        listTagName = repoTag.allTagName
    }

    fun insert(vararg note: RoomNote) = viewModelScope.launch {
        repoNote.insertAll(*note)
    }

    fun update(
        id: Int,
        title: String? = null,
        lastModified: String? = null,
        isDeleted: Boolean? = null
    ) = viewModelScope.launch {

        val e = repoNote.getNoteById(id).value!!

        title?.let {
            e.title = it
        }

        lastModified?.let {
            e.lastModified = it
        }

        isDeleted?.let {
            e.isDeleted = it
        }

        repoNote.updateAll(e)
    }

    fun update(vararg note: RoomNote) = viewModelScope.launch {
        repoNote.updateAll(*note)
    }

    fun delete(vararg note: RoomNote) = viewModelScope.launch {
        repoNote.deleteAll(*note)
    }

    fun getNoteById(noteId: Int) = viewModelScope.launch {
        curNote = repoNote.getNoteById(noteId)
    }

    fun getNoteByCreateDate(timestamp: String) = viewModelScope.launch {
        curNote = repoNote.getNoteByCreateDate(timestamp)
    }

    fun insertAndGetNote(note: RoomNote) = viewModelScope.launch {
        curNote = repoNote.insertAndGetNote(note)
    }

    fun getTagNamesByNoteId(noteId: Int) = viewModelScope.launch {
        curTags = repoTag.getTagNamesByNoteId(noteId)
    }

    fun getTagNamesByNotIncludeNoteId(noteId: Int) = viewModelScope.launch {
        curTagsExclude = repoTag.getTagNamesByNotIncludeNoteId(noteId)
    }

    fun insertNoteId2Tag(tagName: String, noteId: Int) = viewModelScope.launch {
        repoTag.insertNoteId2Tag(tagName, noteId)
    }

    fun removeNoteIdFromTag(tagName: String, noteId: Int) = viewModelScope.launch {
        repoTag.removeNoteIdFromTag(tagName, noteId)
    }

    fun restoreNote(noteId: Int) = viewModelScope.launch {
        repoNote.restoreNote(noteId)
    }

    fun moveNoteToBin(noteId: Int) = viewModelScope.launch {
        repoNote.moveNoteToBin(noteId)
    }

    fun checkTagNameExist(tagName: String) = viewModelScope.launch {
        checkTagName.postValue(repoTag.checkTagNameExist(tagName))
    }

    fun getAllNote(tags: MutableList<String>) = viewModelScope.launch {

        val tagList = repoTag.getTags(tags)

        val noteId = mutableListOf<Int>()

        tagList.let {
            for (tag in it) {
                val s =tag.inNoteIds.substring(1 until tag.inNoteIds.length)
                val tokenizer = StringTokenizer(s, "||")

                while (tokenizer.hasMoreTokens()) {
                    val token = tokenizer.nextToken()
                    if (token.isNotEmpty()) {
                        val id = token.toInt()
                        if (id !in noteId) noteId.add(id)
                    }
                }
            }
        }

        listNoteFilter.postValue(repoNote.getNotes(noteId))
    }
}
