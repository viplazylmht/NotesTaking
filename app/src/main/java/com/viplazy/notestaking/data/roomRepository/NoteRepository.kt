package com.viplazy.notestaking.data.roomRepository

import androidx.lifecycle.LiveData
import com.viplazy.notestaking.data.roomDao.NoteDao
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import java.sql.Timestamp

class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allNote: LiveData<List<RoomNote>> = noteDao.getNotes()

    suspend fun insertAll(vararg note: RoomNote) {
        noteDao.insertNotes(*note)
    }

    suspend fun updateAll(vararg note: RoomNote) {
        noteDao.updateNotes(*note)
    }

    suspend fun deleteAll(vararg note: RoomNote): Int {
        return noteDao.delete(*note)
    }

    suspend fun getNoteById(noteTitleId: Int): LiveData<RoomNote> {
        return noteDao.getNoteById(noteTitleId)
    }

    suspend fun getNoteByCreateDate(timestamp: String): LiveData<RoomNote> {
        return noteDao.getNoteByCreateDate(timestamp)
    }

    suspend fun insertAndGetNote(note: RoomNote): LiveData<RoomNote> {
        return noteDao.insertAndGetNote(note)
    }

    suspend fun restoreNote(noteId: Int) {
        noteDao.updateNoteState(noteId, false)
    }

    suspend fun moveNoteToBin(noteId: Int) {
        noteDao.updateNoteState(noteId, true)
    }
}