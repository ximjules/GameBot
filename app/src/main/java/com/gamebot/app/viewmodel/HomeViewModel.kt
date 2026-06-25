package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.EventModel
import com.gamebot.app.data.EventRepository
import com.gamebot.app.data.GameModel
import com.gamebot.app.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentlyPlaying: GameModel? = null,
    val totalGames: Int = 0,
    val backlogCount: Int = 0,
    val completedCount: Int = 0,
    val upcomingEvents: List<EventModel> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val gameRepository = GameRepository()
    private val eventRepository = EventRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val games = gameRepository.getGames()
            val events = eventRepository.getEvents()
            _uiState.value = HomeUiState(
                currentlyPlaying = games.firstOrNull { it.category == "playing" },
                totalGames = games.size,
                backlogCount = games.count { it.category == "backlog" },
                completedCount = games.count { it.category == "completed" },
                upcomingEvents = events.take(3),
                isLoading = false
            )
        }
    }
}