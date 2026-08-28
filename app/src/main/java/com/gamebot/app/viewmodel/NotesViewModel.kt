package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.NoteModel
import com.gamebot.app.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<NoteModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotesViewModel : ViewModel() {
    private val repository = NoteRepository()

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val notes = repository.getNotes()
            _uiState.value = NotesUiState(notes = notes, isLoading = false)
        }
    }

    fun addNote(note: NoteModel) {
        if (note.title.isBlank()) return
        viewModelScope.launch {
            val success = repository.addNote(note.copy(title = note.title.trim()))
            if (success) {
                loadNotes()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Note was not saved."
                )
            }
        }
    }

    fun updateNote(note: NoteModel) {
        if (note.title.isBlank()) return
        viewModelScope.launch {
            val success = repository.updateNote(note.copy(title = note.title.trim()))
            if (success) loadNotes()
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val success = repository.deleteNote(noteId)
            if (success) loadNotes()
        }
    }
}
