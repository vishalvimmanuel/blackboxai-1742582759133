package com.example.notetakingapp.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    // By default Room runs suspend queries off the main thread
    @WorkerThread
    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    @WorkerThread
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    @WorkerThread
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    @WorkerThread
    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }

    @WorkerThread
    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }
}