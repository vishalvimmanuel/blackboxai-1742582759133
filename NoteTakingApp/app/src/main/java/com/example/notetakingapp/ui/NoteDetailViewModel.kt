package com.example.notetakingapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.data.NoteDatabase
import com.example.notetakingapp.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    
    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
    }

    fun loadNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = repository.getNoteById(noteId)
            _currentNote.value = note
        }
    }

    fun saveNote(title: String, content: String) {
        if (title.isBlank()) {
            _saveError.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSaving.value = true
                _saveError.value = null
                
                val note = _currentNote.value?.copy(
                    title = title,
                    content = content,
                    timestamp = Date()
                ) ?: Note(
                    title = title,
                    content = content,
                    timestamp = Date()
                )

                if (note.id == 0L) {
                    repository.insertNote(note)
                } else {
                    repository.updateNote(note)
                }
                
                _isSaving.value = false
            } catch (e: Exception) {
                _saveError.value = "Error saving note: ${e.message}"
                _isSaving.value = false
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.value?.let { note ->
                repository.deleteNote(note)
            }
        }
    }

    fun clearError() {
        _saveError.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteDetailViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}