package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.GameModel
import com.gamebot.app.data.GameRepository
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val games = repository.getGames()
            _uiState.value = _uiState.value.copy(
                games = games,
                isLoading = false
            )
        }
    }

    fun addGame(game: GameModel) {
        viewModelScope.launch {
            val success = repository.addGame(game)
            if (success) {
                loadGames()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Igra nije spremljena."
                )
            }
        }
    }

    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            val success = repository.deleteGame(gameId)
            if (success) {
                loadGames()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Igra nije obrisana."
                )
            }
        }
    }

    fun updateProgress(game: GameModel, newProgress: Float) {
        viewModelScope.launch {
            val success = repository.updateGame(game.copy(progress = newProgress))
            if (success) {
                loadGames()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Progress was not updated."
                )
            }
        }
    }

    fun updateGame(game: GameModel) {
        viewModelScope.launch {
            val success = repository.updateGame(game)
            if (success) {
                loadGames()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Igra nije azurirana."
                )
            }
        }
    }
}
