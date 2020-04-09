package com.viplazy.notestaking.data.roomDao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.viplazy.notestaking.data.roomDatabase.RoomNote
import java.sql.Timestamp

@Dao
// https://stackoverflow.com/questions/44330452/android-persistence-room-cannot-figure-out-how-to-read-this-field-from-a-curso/44424148#44424148
interface NoteDao {

    @Query("SELECT * FROM NoteList WHERE isDeleted = 0") // 0 is false, 1 is true
    fun getNotes(): LiveData<List<RoomNote>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNotes(vararg notes: RoomNote)

    @Delete
    fun delete(vararg notes: RoomNote): Int

    @Query("DELETE FROM NoteList")
    suspend fun deleteAll()

    // Update notes by primary key (index)
    @Update
    fun updateNotes(vararg notes: RoomNote): Int

    @Query("SELECT * FROM NoteList WHERE noteId = :noteId LIMIT 1")
    fun getNoteById(noteId : Int): LiveData<RoomNote>

    @Query("SELECT * FROM NoteList WHERE creationDate = :timestamp LIMIT 1")
    fun getNoteByCreateDate(timestamp: String) : LiveData<RoomNote>


    @Query("UPDATE NoteList SET isDeleted = :isDeleted WHERE noteId = :noteId")
    fun updateNoteState(noteId: Int, isDeleted: Boolean)

    fun insertAndGetNote(note: RoomNote): LiveData<RoomNote> {
        insertNotes(note)

        return getNoteByCreateDate(note.creationDate)
    }
}