package com.viplazy.notestaking.data.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.viplazy.notestaking.data.roomDao.ImageDao
import com.viplazy.notestaking.data.roomDao.NoteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@Database(entities = [NoteListImage::class], version = 1)

abstract class ImageDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao

    private class DatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            ImageDatabase.INSTANCE?.let { database ->
                scope.launch {
                    //populateDatabase(context, database.imageDao())
                }
            }
        }

        suspend fun populateDatabase(context: Context, imageDao: ImageDao) {
            // Delete all content here.
            imageDao.deleteAll()

            imageDao.insertImages()
        }
    }
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ImageDatabase? = null



        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): ImageDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            kotlinx.coroutines.internal.synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageDatabase::class.java, "Images.db"
                )
                    .addCallback(ImageDatabase.DatabaseCallback(context, scope))
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}