package com.example.dydemo.data.repository

import com.example.dydemo.data.database.UserDao
import com.example.dydemo.data.model.User
import com.example.dydemo.data.model.UserMapper
import com.example.dydemo.data.source.JsonDataSource
import com.example.dydemo.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.dydemo.data.SortingMode

// 依赖注入：通过构造函数传入 UserDao
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val jsonDataSource: JsonDataSource
) {
    // 【修改】现在需要一个参数来确定使用哪个 DAO 方法
    fun getFollowingList(mode: SortingMode): Flow<List<User>> {
        val entityFlow = when (mode) {
            SortingMode.COMPREHENSIVE -> userDao.getFollowingUsersByComprehensive()
            SortingMode.TIME_ORDER -> userDao.getFollowingUsersByTime()
        }

        return entityFlow.map { entities ->
            entities.map { entity ->
                // UserEntity 转换为 User model 的逻辑保持不变
                User(
                    id = entity.id,
                    nickname = entity.nickname,
                    avatarResId = entity.avatarResId,
                    authenticationLabelId = entity.authenticationLabelId,
                    isSpecialFollow = entity.isSpecialFollow,
                    isMutual = entity.isMutual,
                    customRemark = entity.customRemark,
                    followTimestamp = entity.followTimestamp,
                )
            }
        }
    }

    // 模拟初始数据（仅在第一次运行时插入）
    suspend fun initializeData() {
        if (userDao.countUsers() == 0) {
            // 从 JSON 数据源获取初始数据
            val initialUsers = jsonDataSource.getInitialUsers()
            userDao.insertAll(initialUsers)
        }
    }

    /**
     * 从数据库获取关注列表，并将其转换为 UI 领域模型 (User) 的 Flow。
     * 数据库更新时，此 Flow 会自动发射新数据。
     */
    fun getFollowingList(): Flow<List<User>> {
        return userDao.getFollowingUsersByComprehensive().map { entities ->
            UserMapper.mapFromEntityList(entities)
        }
    }

    suspend fun toggleSpecialFollow(userId: Int) {
        // 从数据库获取当前用户状态
        val userEntity = userDao.getFollowingUsersByComprehensive().map { it.find { user -> user.id == userId } }.firstOrNull()

        // 切换状态并更新数据库
        userEntity?.let {
            val updatedEntity = it.copy(isSpecialFollow = !it.isSpecialFollow)
            userDao.update(updatedEntity)
        }
    }

    suspend fun setSpecialFollow(userId: Int, isSpecialFollow: Boolean) {
        // 数据库操作：根据 isSpecialFollow 参数设置数据库中的状态
        userDao.updateSpecialFollowStatus(userId, isSpecialFollow)
    }

    suspend fun unfollowUser(userId: Int) {
        userDao.deleteById(userId)
    }

    suspend fun updateRemark(userId: Int, newRemark: String) {
        val userEntity = userDao.getFollowingUsersByComprehensive().map { it.find { user -> user.id == userId } }.firstOrNull()

        userEntity?.let {
            val updatedEntity = it.copy(customRemark = newRemark)
            userDao.update(updatedEntity)
        }
    }

    /**
     * 根据布尔状态更新用户的关注时间戳。
     * @param userId 用户ID
     * @param isFollowing true 表示关注 (设置时间戳)，false 表示取关 (清除时间戳)。
     */
    suspend fun updateFollowingStatus(userId: Int, isFollowing: Boolean) {
        // 1. 将布尔状态转换为时间戳 Long?
        val timestamp: Long? = if (isFollowing) System.currentTimeMillis() else null

        // 2. 调用 UserDao 中我们新增的更新时间戳的方法
        userDao.updateFollowTimestamp(userId, timestamp)
    }
}