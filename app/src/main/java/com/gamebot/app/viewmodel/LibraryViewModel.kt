package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.GameModel
import com.gamebot.app.data.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val games: List<GameModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class LibraryViewModel : ViewModel() {
    private val repository = GameRepository()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val games = repository.getGames()
            _uiState.value = _uiState.value.copy(
                games = games,
                isLoading = false
            )
        }
    }

    fun addGame(game: GameModel) {
        viewModelScope.launch {
            repository.addGame(game)
            // Malo pričekamo da Firestore spremi
            delay(1500)
            loadGames()
        }
    }

    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
            delay(1000)
            loadGames()
        }
    }

    fun updateProgress(game: GameModel, newProgress: Float) {
        viewModelScope.launch {
            repository.updateGame(game.copy(progress = newProgress))
            delay(1000)
            loadGames()
        }
    }
}