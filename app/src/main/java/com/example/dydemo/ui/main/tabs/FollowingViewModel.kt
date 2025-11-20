package com.example.dydemo.ui.main.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dydemo.data.model.User
import com.example.dydemo.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.dydemo.data.SortingMode // <--- 新增导入
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest // <--- 新增导入
import kotlinx.coroutines.flow.combine // <--- 新增导入 (可能不需要，但 flatMapLatest 是必需的)

// 1. 使用 @HiltViewModel 标记，允许 Hilt 注入构造函数中的依赖
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 【新增】用于保存当前的排序模式
    private val _sortingMode = MutableStateFlow(SortingMode.COMPREHENSIVE)
    // UI State，存放关注列表数据
    private val _uiState = MutableStateFlow(FollowingUiState())
    val uiState: StateFlow<FollowingUiState> = _uiState.asStateFlow()

    init {
        // 【核心修改点】使用 viewModelScope.launch{} 包裹 collectLatest
        viewModelScope.launch { // <--- 新增
            _sortingMode
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .flatMapLatest { mode ->
                    // 当 mode 变化时，切换到新的 Repository Flow
                    userRepository.getFollowingList(mode)
                        .catch { exception ->
                            _uiState.update { it.copy(errorMessage = exception.message, isLoading = false) }
                        }
                        .onStart {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                }
                .collectLatest { list -> // <--- 现在在协程内调用，不会报错
                    _uiState.update {
                        it.copy(
                            followingUsers = list,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        } // <--- 结束 launch
    }

    // 【新增方法】切换排序模式
    fun toggleSortingMode() {
        val newMode = when (_sortingMode.value) {
            SortingMode.COMPREHENSIVE -> SortingMode.TIME_ORDER
            SortingMode.TIME_ORDER -> SortingMode.COMPREHENSIVE
        }
        _sortingMode.value = newMode // 触发 flatMapLatest 重新获取数据
        _uiState.update { it.copy(currentSortingMode = newMode) } // 更新 UI 状态
    }

    /**
     * 首次启动时调用，确保数据库有初始数据。
     */
    fun initializeData() {
        viewModelScope.launch {
            userRepository.initializeData()
        }
    }

    /**
     * 观察 UserRepository 返回的 Flow，实现数据响应式更新。
     */
    private fun observeFollowingList() {
        viewModelScope.launch {
            userRepository.getFollowingList()
                .onStart {
                    // 标记开始加载状态
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { exception ->
                    // 捕获错误，更新错误信息
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
                .collectLatest { list ->
                    // 接收到新数据时，更新 UI 状态并停止加载
                    _uiState.update {
                        it.copy(
                            followingUsers = list,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * 手动触发刷新，通常用于下拉刷新。
     * 在我们的 Flow 架构中，它主要用于重新开始观察流，或者在真实项目中触发网络请求。
     */
    fun loadFollowingList() {
        // 在 Room+Flow 架构中，实际的"刷新"是数据库触发的。
        // 这里可以简单地重新开始观察，或在更复杂的场景中，只用于标记 isLoading。
        // 由于 observeFollowingList 已经通过 Flow 提供了实时更新，这里只设置加载状态模拟刷新过程。
        _uiState.update { it.copy(isLoading = true) }
        // 实际操作由 observeFollowingList 接收到数据后自动停止加载。
    }

    // --- 用户交互事件处理 ---

    fun onUnfollowUser(userId: Int) {
        viewModelScope.launch {
            // Repository 操作数据库，数据库更新后 Flow 会自动通知 UI 刷新
            userRepository.unfollowUser(userId)
        }
    }

    fun onToggleSpecialFollow(userId: Int) {
        viewModelScope.launch {
            userRepository.toggleSpecialFollow(userId)
        }
    }

    fun onUpdateRemark(userId: Int, newRemark: String) {
        viewModelScope.launch {
            userRepository.updateRemark(userId, newRemark)
        }
    }

    // 修改备注的弹窗
    // 【新增方法】显示编辑备注弹窗
    fun showRemarkDialog(user: User) {
        _uiState.update {
            it.copy(
                userToEditRemark = user,
                currentRemarkInput = user.customRemark ?: "" // 初始化输入框文本
            )
        }
    }

    // 【新增方法】更新输入框内容
    fun updateRemarkInput(newInput: String) {
        _uiState.update { it.copy(currentRemarkInput = newInput) }
    }

    // 【新增方法】清除输入框内容
    fun clearRemarkInput() {
        _uiState.update { it.copy(currentRemarkInput = "") }
    }

    // 【新增方法】取消编辑，隐藏弹窗
    fun hideRemarkDialog() {
        _uiState.update {
            it.copy(
                userToEditRemark = null,
                currentRemarkInput = ""
            )
        }
    }

    // 【新增方法】保存备注
    fun saveRemark() {
        // 确保正在编辑用户
        val user = _uiState.value.userToEditRemark ?: return
        val newRemark = _uiState.value.currentRemarkInput

        viewModelScope.launch {
            // 调用 Repository 更新数据库
            newRemark.trim().ifEmpty { null }?.let { userRepository.updateRemark(user.id, it) }

            // 隐藏弹窗
            hideRemarkDialog()
        }
    }

    // 【修改】关注/取关逻辑：基于列表隐含状态 (true) 进行切换
    fun onFollowToggle(userId: Int) {
        _uiState.update { currentState ->
            // 1. 获取当前用户，隐含的数据库状态 isFollowing = true
            val user = currentState.followingUsers.find { it.id == userId } ?: return@update currentState
            val dbIsFollowing = true // 隐含状态：用户在列表中即为已关注

            // 2. 获取当前的待定状态（如果有）
            val currentPending = currentState.pendingFollowActions[userId]

            // 3. 计算 UI 上的当前状态：如果 Map 中有值，使用 Map 值，否则使用隐含的 dbIsFollowing (true)
            val currentUiState = currentPending ?: dbIsFollowing

            // 4. 切换状态：如果当前是关注 (true)，下一个就是取关 (false)；如果当前是取关 (false)，下一个就是关注 (true)
            val nextPendingState = !currentUiState

            // 5. 更新 Map：
            // 待定操作是“取关” (false) 时，我们将其添加到 Map 中。
            // 待定操作是“关注” (true) 时，它与隐含的数据库状态 (true) 相同，我们应将其从 Map 中移除。
            val newPendingMap = if (nextPendingState == dbIsFollowing) {
                // 待定操作是“关注” (true)，与数据库状态抵消，移除条目
                currentState.pendingFollowActions.toMutableMap().apply { remove(userId) }
            } else {
                // 待定操作是“取关” (false)，记录新的待定操作
                currentState.pendingFollowActions + (userId to nextPendingState)
            }

            currentState.copy(pendingFollowActions = newPendingMap)
        }
    }


    // 【修改】下拉刷新时触发的数据库提交和状态重置
    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val pendingActions = _uiState.value.pendingFollowActions

            // 1. 提交待定操作到数据库
            // pendingFollowActions Map 中只包含待定取关操作 (Value=false)
            pendingActions.forEach { (userId, newIsFollowing) ->
                // 调用 DAO 方法，将 isFollowing 状态更新为 false
                userRepository.updateFollowingStatus(userId, newIsFollowing)
            }

            // 2. 数据库更新会触发 Flow 重新收集数据。
            // Flow 重新收集数据后，数据库中状态为 false 的用户将不会出现在列表中。

            // 3. 重置 Map 和 Loading 状态
            kotlinx.coroutines.delay(500) // 模拟网络延迟
            _uiState.update {
                it.copy(
                    pendingFollowActions = emptyMap(),
                    isLoading = false // <--- 关键点 2：结束刷新
                )
            }
        }
    }
}

// 定义 UI 状态的数据类
data class FollowingUiState(
    val followingUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentSortingMode: SortingMode = SortingMode.COMPREHENSIVE, // <--- 新增状态,排序模式
    // 【新增状态】用于控制备注编辑弹窗
    val userToEditRemark: User? = null, // 当前正在编辑备注的用户，null 表示弹窗隐藏
    val currentRemarkInput: String = "", // 当前输入框中的文本
    // 【新增状态】使用 Map 存储待定的关注状态
    // Key: UserId (Int)
    // Value: 待定状态 (Boolean): true=待定关注，false=待定取关
    val pendingFollowActions: Map<Int, Boolean> = emptyMap()
)