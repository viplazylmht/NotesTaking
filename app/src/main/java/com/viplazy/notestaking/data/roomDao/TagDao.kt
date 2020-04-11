package com.viplazy.notestaking.data.roomDao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.viplazy.notestaking.data.roomDatabase.NoteTag

@Dao

interface TagDao {
    @Query("SELECT * FROM TagList WHERE isDeleted = 0")
    fun getTags(): LiveData<List<NoteTag>>

    @Query("SELECT * FROM TagList WHERE isDeleted = 0 AND tagName IN (:tagNames)")
    fun getTags(tagNames: List<String>): List<NoteTag>

    @Query("SELECT tagName FROM TagList WHERE isDeleted = 0")
    fun getTagNames(): LiveData<List<String>>

    @Query("SELECT * FROM TagList WHERE inNoteIds LIKE :param")
    fun getTagNamesByParam(param: String): LiveData<List<NoteTag>>
        //val param = "%|$noteId|%"

    @Query("SELECT * FROM TagList WHERE inNoteIds NOT LIKE :param")
    fun getTagNamesByNotIncludeParam(param: String): LiveData<List<NoteTag>>
        //val param = "%|$noteId|%"

    @Insert
    fun insertTags(vararg tag: NoteTag)

    @Delete
    fun deleteTags(vararg tag: NoteTag)

    @Delete
    fun delete(vararg tag: NoteTag): Int

    @Update
    fun updateTags(vararg tag: NoteTag)

    @Query("UPDATE TagList SET isDeleted = :isDeleted WHERE tagName = :tagName")
    fun updateTagState(tagName: String, isDeleted: Boolean)

    @Query("DELETE FROM TagList")
    suspend fun deleteAll()

    @Query("SELECT * FROM TagList WHERE tagName = :tagName")
    fun getTagByTagName(tagName: String) : NoteTag?

    @Query("SELECT EXISTS(SELECT * FROM TagList WHERE tagName = :tagName)")
    fun checkTagNameExist(tagName: String): Int


}