package com.viplazy.notestaking.data.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.viplazy.notestaking.data.roomDao.NoteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [RoomNote::class], version = 1)

abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao() : NoteDao

    private class DatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            NoteDatabase.INSTANCE?.let { database ->
                scope.launch {
                    //populateDatabase(context, database.noteDao())
                }
            }
        }

        suspend fun populateDatabase(context: Context, noteDao: NoteDao) {
            // Delete all content here.
            noteDao.deleteAll()

            val timeStamp = Calendar.getInstance().time.time / 1000

            noteDao.insertNotes(
                RoomNote(0, "Title1","message...1", timeStamp.toString(), "339"),
                RoomNote(0, "Title2","message...2", timeStamp.toString(), "339"),
                RoomNote(0, "Title3","message...3", timeStamp.toString(), "339"))
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): NoteDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            kotlinx.coroutines.internal.synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java, "Note.db"
                )
                    .addCallback(NoteDatabase.DatabaseCallback(context, scope))
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}