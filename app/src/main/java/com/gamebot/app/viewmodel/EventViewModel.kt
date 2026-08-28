package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.EventModel
import com.gamebot.app.data.EventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val events: List<EventModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val events = repository.getEvents()
            _uiState.value = EventUiState(
                events = events,
                isLoading = false,
                error = null
            )
        }
    }

    fun addEvent(event: EventModel) {
        viewModelScope.launch {
            val success = repository.addEvent(event)
            if (success) {
                loadEvents()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Event was not saved: ${repository.lastError ?: "unknown Firebase error"}"
                )
            }
        }
    }

    fun deleteEvent(event: EventModel) {
        viewModelScope.launch {
            val success = repository.deleteEvent(event.id, event.isPublic)
            if (success) {
                loadEvents()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Event was not deleted."
                )
            }
        }
    }

    fun joinPublicEvent(eventId: String) {
        viewModelScope.launch {
            val success = repository.joinPublicEvent(eventId)
            if (success) {
                loadEvents()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Could not join this event."
                )
            }
        }
    }
}
