package com.gamebot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamebot.app.data.CommunityPostModel
import com.gamebot.app.data.CommunityRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPostModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CommunityViewModel : ViewModel() {
    private val repository = CommunityRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val posts = repository.getPosts()
            _uiState.value = CommunityUiState(posts = posts, isLoading = false)
        }
    }

    fun addPost(content: String, username: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val success = repository.addPost(content.trim(), username)
            if (success) {
                loadPosts()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Post was not saved. Check login and internet connection."
                )
            }
        }
    }

    fun toggleLike(post: CommunityPostModel) {
        viewModelScope.launch {
            val success = repository.toggleLike(post)
            if (success) loadPosts()
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val success = repository.deletePost(postId)
            if (success) loadPosts()
        }
    }

    fun addComment(post: CommunityPostModel, username: String, comment: String) {
        if (comment.isBlank()) return
        viewModelScope.launch {
            val success = repository.addComment(post, username, comment)
            if (success) loadPosts()
        }
    }
}
