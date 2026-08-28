package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.EventModel
import com.gamebot.app.data.EventRepository
import com.gamebot.app.data.GameModel
import com.gamebot.app.data.GameRepository
import com.gamebot.app.data.CommunityRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentlyPlaying: GameModel? = null,
    val totalGames: Int = 0,
    val backlogCount: Int = 0,
    val completedCount: Int = 0,
    val communityPostCount: Int = 0,
    val upcomingEventCount: Int = 0,
    val upcomingEvents: List<EventModel> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val gameRepository = GameRepository()
    private val eventRepository = EventRepository()
    private val communityRepository = CommunityRepository()

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
            val posts = communityRepository.getPosts()
            val today = LocalDate.now()
            val upcomingEvents = events
                .filter { event ->
                    event.year > 0 &&
                        event.month > 0 &&
                        event.day > 0 &&
                        !LocalDate.of(event.year, event.month, event.day).isBefore(today)
                }
                .sortedWith(
                    compareBy<EventModel> { if (it.year > 0) it.year else Int.MAX_VALUE }
                        .thenBy { if (it.month > 0) it.month else Int.MAX_VALUE }
                        .thenBy { if (it.day > 0) it.day else Int.MAX_VALUE }
                )
            _uiState.value = HomeUiState(
                currentlyPlaying = games.maxByOrNull { it.addedAt },
                totalGames = games.size,
                backlogCount = games.count { it.category == "backlog" },
                completedCount = games.count { it.category == "completed" },
                communityPostCount = posts.size,
                upcomingEventCount = upcomingEvents.size,
                upcomingEvents = upcomingEvents.take(3),
                isLoading = false
            )
        }
    }
}
