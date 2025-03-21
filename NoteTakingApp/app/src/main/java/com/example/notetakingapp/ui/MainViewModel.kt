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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: StateFlow<List<Note>>
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        
        viewModelScope.launch {
            repository.allNotes.collect { notes ->
                _allNotes.value = notes
            }
        }
    }

    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())

    fun searchNotes(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            repository.searchNotes(query).collect { notes ->
                _allNotes.value = notes
            }
        }
    }

    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(note)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        viewModelScope.launch {
            repository.allNotes.collect { notes ->
                _allNotes.value = notes
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}