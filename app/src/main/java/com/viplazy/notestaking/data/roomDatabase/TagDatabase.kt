package com.viplazy.notestaking.data.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.viplazy.notestaking.data.roomDao.TagDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [NoteTag::class], version = 1, exportSchema = true)

abstract class TagDatabase : RoomDatabase() {

    abstract fun tagDao() : TagDao

    private class DatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            TagDatabase.INSTANCE?.let { database ->
                scope.launch {
                    //populateDatabase(context, database.tagDao())
                }
            }
        }

        suspend fun populateDatabase(context: Context, tagDao: TagDao) {
            // Delete all content here.
            tagDao.deleteAll()

            val timeStamp = Calendar.getInstance().time.time / 1000

            tagDao.insertTags(
                NoteTag("tag1", timeStamp),
                NoteTag("tag1", timeStamp),
                NoteTag("tag1", timeStamp))
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TagDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TagDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            kotlinx.coroutines.internal.synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TagDatabase::class.java, "Tag.db"
                )
                    .addCallback(TagDatabase.DatabaseCallback(context, scope))
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}