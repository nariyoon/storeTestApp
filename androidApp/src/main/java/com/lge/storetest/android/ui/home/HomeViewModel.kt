package com.lge.storetest.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lge.storetest.domain.Post
import com.lge.storetest.domain.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    // Basic text state
    private val _text = MutableLiveData<String>().apply {
        value = "Post Management"
    }
    val text: LiveData<String> = _text

    // Post stream state
    private val _postState = MutableStateFlow<PostUiState>(PostUiState.Loading)
    val postState: StateFlow<PostUiState> = _postState

    // Current post ID to display
    private var currentPostId: Int = 1

    init {
        loadPost()
    }

    // 1. Function to read data stream from repository using Store flow
    fun loadPost() {
        viewModelScope.launch {
            _postState.value = PostUiState.Loading
            try {
                val post = postRepository.getPost(currentPostId)
                if (post != null) {
                    _postState.value = PostUiState.Success(post)
                } else {
                    _postState.value = PostUiState.Error("Post not found")
                }
            } catch (e: Exception) {
                _postState.value = PostUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // 2. Function to force refresh from server immediately using Store
    fun refreshFromServer() {
        viewModelScope.launch {
            _postState.value = PostUiState.Loading
            try {
                val post = postRepository.getPostFresh(currentPostId)
                if (post != null) {
                    _postState.value = PostUiState.Success(post)
                } else {
                    _postState.value = PostUiState.Error("Failed to fetch post from server")
                }
            } catch (e: Exception) {
                _postState.value = PostUiState.Error(e.message ?: "Server error")
            }
        }
    }

    // 3. Set new post ID and fetch fresh data (simulating screen transition)
    fun setPostId(postId: Int) {
        if (currentPostId != postId) {
            currentPostId = postId
            viewModelScope.launch {
                _postState.value = PostUiState.Loading
                try {
                    // Use the specialized method for screen transitions
                    val post = postRepository.getPostOnScreenChange(currentPostId)
                    if (post != null) {
                        _postState.value = PostUiState.Success(post)
                    } else {
                        _postState.value = PostUiState.Error("Post not found")
                    }
                } catch (e: Exception) {
                    _postState.value = PostUiState.Error(e.message ?: "Error loading post")
                }
            }
        }
    }

    // 4. Update post in DB only through Store
    fun updatePostInDb(title: String?, body: String?, userId: Int?) {
        viewModelScope.launch {
            try {
                val updatedPost = postRepository.updatePost(
                    postId = currentPostId,
                    title = title,
                    body = body,
                    userId = userId
                )
                _postState.value = PostUiState.Success(updatedPost)
            } catch (e: Exception) {
                _postState.value = PostUiState.Error(e.message ?: "Failed to update post")
            }
        }
    }

    // 5. Direct API call without using DB
    fun loadPostDirectFromServer() {
        viewModelScope.launch {
            _postState.value = PostUiState.Loading
            try {
                val post = postRepository.getPostDirectFromServer(currentPostId)
                if (post != null) {
                    _postState.value = PostUiState.Success(post)
                } else {
                    _postState.value = PostUiState.Error("Post not found on server")
                }
            } catch (e: Exception) {
                _postState.value = PostUiState.Error(e.message ?: "Server error")
            }
        }
    }

    // 5b. Update post directly on server (bypassing DB)
    fun updatePostDirectToServer(title: String?, body: String?, userId: Int?) {
        viewModelScope.launch {
            _postState.value = PostUiState.Loading
            try {
                val updatedPost = postRepository.updatePostDirectToServer(
                    postId = currentPostId,
                    title = title,
                    body = body,
                    userId = userId
                )

                if (updatedPost != null) {
                    _postState.value = PostUiState.Success(updatedPost)
                } else {
                    _postState.value = PostUiState.Error("Failed to update post on server")
                }
            } catch (e: Exception) {
                _postState.value = PostUiState.Error(e.message ?: "Server update error")
            }
        }
    }
}

// UI state for Post data
sealed class PostUiState {
    object Loading : PostUiState()
    data class Success(val post: Post) : PostUiState()
    data class Error(val message: String) : PostUiState()
}