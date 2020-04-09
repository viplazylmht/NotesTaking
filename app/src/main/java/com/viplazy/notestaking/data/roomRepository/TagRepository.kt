package com.viplazy.notestaking.data.roomRepository

import androidx.lifecycle.LiveData
import com.viplazy.notestaking.data.roomDao.TagDao
import com.viplazy.notestaking.data.roomDatabase.NoteTag
import java.util.*

class TagRepository(private val tagDao: TagDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allTag: LiveData<List<NoteTag>> = tagDao.getTags()
    val allTagName: LiveData<List<String>> = tagDao.getTagNames()

    suspend fun insertAll(vararg tag: NoteTag) {
        tagDao.insertTags(*tag)
    }

    suspend fun updateAll(vararg tag: NoteTag) {
        tagDao.updateTags(*tag)
    }

    suspend fun deleteAll(vararg tag: NoteTag): Int {
        return tagDao.delete(*tag)
    }

    suspend fun getTagNamesByNoteId(noteId: Int): LiveData<List<NoteTag>> {
        return tagDao.getTagNamesByParam("%|$noteId|%")
    }

    suspend fun getTagNamesByNotIncludeNoteId(noteId: Int): LiveData<List<NoteTag>> {
        return tagDao.getTagNamesByNotIncludeParam("%|$noteId|%")
    }

    suspend fun restoreTag(tagName: String) {
        tagDao.updateTagState(tagName, false)
    }

    suspend fun moveTagToBin(tagName: String) {
        tagDao.updateTagState(tagName, true)
    }

    suspend fun insertNoteId2Tag(tagName: String, noteId: Int) {
        val tag = tagDao.getTagByTagName(tagName)

        if (tag == null) {
            val t = NoteTag(tagName, Calendar.getInstance().time.time / 1000)
            t.inNoteIds += "|$noteId|"
            tagDao.insertTags(t)
        }
        else {
            tag.inNoteIds += "|$noteId|"
            tagDao.updateTags(tag)
        }
    }

    suspend fun removeNoteIdFromTag(tagName: String, noteId: Int) {
        val pattern = "|$noteId|"

        val tag = tagDao.getTagByTagName(tagName)

        tag?.let {
            if (it.inNoteIds.contains(pattern)) {
                it.inNoteIds = it.inNoteIds.replace(pattern, "")
                tagDao.updateTags(it)
            }
        }
    }
}