package com.example.dydemo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.dydemo.data.local.database.UserDao
import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.data.paging.LocalUserPagingSource
import com.example.dydemo.domain.mapper.UserMapper.toUser
import com.example.dydemo.domain.model.SortingMode
import com.example.dydemo.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    fun getFollowingUsersStream(sortingMode: SortingMode): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { LocalUserPagingSource(userDao, sortingMode) }
        ).flow.map { pagingData ->
            pagingData.map { it.toUser() }
        }
    }

    fun getFollowingCount(): Flow<Int> = userDao.getFollowingCount()

    suspend fun initializeData() {
        if (userDao.countUsers() == 0) {
            val random = Random(System.currentTimeMillis())
            val initialUsers = (1..1000).map { i ->
                UserEntity(
                    id = i,
                    nickname = "用户${random.nextInt(1000, 9999)}",
                    avatarUrl = "https://picsum.photos/id/${i}/200/200",
                    authenticationLabelId = 0,
                    isMutual = random.nextBoolean(),
                    isSpecialFollow = if (i <= 5) true else random.nextDouble() < 0.1,
                    customRemark = if (random.nextDouble() < 0.2) "备注${i}" else null,
                    followTimestamp = System.currentTimeMillis() - random.nextLong(1000L * 60 * 60 * 24 * 30)
                )
            }
            userDao.insertAll(initialUsers)
        }
    }

    suspend fun setSpecialFollow(userId: Int, isSpecialFollow: Boolean) {
        userDao.updateSpecialFollow(userId, isSpecialFollow)
    }

    suspend fun followUser(userId: Int) {
        userDao.updateFollowTimestamp(userId, System.currentTimeMillis())
    }

    suspend fun unfollowUser(userId: Int) {
        userDao.updateFollowTimestamp(userId, null)
    }

    suspend fun updateRemark(userId: Int, newRemark: String?) {
        userDao.updateRemark(userId, newRemark)
    }
}