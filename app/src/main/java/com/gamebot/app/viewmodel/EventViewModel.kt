package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.EventModel
import com.gamebot.app.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val events: List<EventModel> = emptyList(),
    val isLoading: Boolean = true
)

class EventViewModel : ViewModel() {
    private val repository = EventRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val events = repository.getEvents()
            _uiState.value = EventUiState(
                events = events,
                isLoading = false
            )
        }
    }

    fun addEvent(event: EventModel) {
        viewModelScope.launch {
            repository.addEvent(event)
            kotlinx.coroutines.delay(1000)
            loadEvents()
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            kotlinx.coroutines.delay(500)
            loadEvents()
        }
    }
}