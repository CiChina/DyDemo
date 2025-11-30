package com.example.dydemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dydemo.data.repository.UserRepository
import com.example.dydemo.domain.model.SortingMode
import com.example.dydemo.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _sortingMode = MutableStateFlow(SortingMode.COMPREHENSIVE)
    val sortingMode: StateFlow<SortingMode> = _sortingMode.asStateFlow()

    val followingUsersStream: Flow<PagingData<User>> = sortingMode
        .flatMapLatest { mode ->
            userRepository.getFollowingUsersStream(mode)
        }
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(FollowingUiState())
    val uiState: StateFlow<FollowingUiState> = _uiState.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.initializeData()
            _followingCount.value = userRepository.getFollowingCount().first()
            _isInitializing.value = false
        }
    }

    fun onFollowToggle(userId: Int, isCurrentlyFollowing: Boolean) {
        val newFollowingState = !isCurrentlyFollowing

        _uiState.update {
            val newPendingActions = it.pendingFollowActions.toMutableMap().apply {
                this[userId] = newFollowingState
            }
            it.copy(pendingFollowActions = newPendingActions)
        }

        if (newFollowingState) {
            _followingCount.update { it + 1 }
        } else {
            _followingCount.update { it - 1 }
        }

        viewModelScope.launch {
            if (newFollowingState) {
                userRepository.followUser(userId)
            } else {
                userRepository.unfollowUser(userId)
            }
        }
    }

    fun toggleSortingMode() {
        _sortingMode.update {
            if (it == SortingMode.COMPREHENSIVE) SortingMode.TIME_ORDER else SortingMode.COMPREHENSIVE
        }
    }

    fun onToggleSpecialFollow(userId: Int, isChecked: Boolean) {
        _uiState.update {
            val newPendingSpecialFollows = it.pendingSpecialFollows.toMutableMap().apply {
                this[userId] = isChecked
            }
            it.copy(pendingSpecialFollows = newPendingSpecialFollows)
        }

        viewModelScope.launch {
            userRepository.setSpecialFollow(userId, isChecked)
        }
    }

    fun showRemarkDialog(user: User) {
        _uiState.update {
            it.copy(
                userToEditRemark = user,
                currentRemarkInput = it.pendingRemarks[user.id] ?: user.customRemark ?: ""
            )
        }
    }

    fun updateRemarkInput(newInput: String) {
        _uiState.update { it.copy(currentRemarkInput = newInput) }
    }

    fun clearRemarkInput() {
        _uiState.update { it.copy(currentRemarkInput = "") }
    }

    fun hideRemarkDialog() {
        _uiState.update {
            it.copy(
                userToEditRemark = null,
                currentRemarkInput = ""
            )
        }
    }

    fun saveRemark() {
        val user = _uiState.value.userToEditRemark ?: return
        val newRemark = _uiState.value.currentRemarkInput.trim()
        val remarkToSave = newRemark.ifEmpty { null }

        _uiState.update {
            val newPendingRemarks = it.pendingRemarks.toMutableMap().apply {
                this[user.id] = remarkToSave
            }
            it.copy(pendingRemarks = newPendingRemarks)
        }

        hideRemarkDialog()

        viewModelScope.launch {
            userRepository.updateRemark(user.id, remarkToSave)
        }
    }
}

data class FollowingUiState(
    val userToEditRemark: User? = null,
    val currentRemarkInput: String = "",
    val pendingFollowActions: Map<Int, Boolean> = emptyMap(),
    val pendingRemarks: Map<Int, String?> = emptyMap(),
    val pendingSpecialFollows: Map<Int, Boolean> = emptyMap()
)
