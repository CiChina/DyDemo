package com.example.dydemo.data.local.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dydemo.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY isSpecialFollow DESC, id ASC")
    fun getFollowingPagingSourceByComprehensive(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY followTimestamp DESC, id ASC")
    fun getFollowingPagingSourceByTime(): PagingSource<Int, UserEntity>

    // --- 新增：用于手动分页的挂起函数 ---
    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY isSpecialFollow DESC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getFollowingByComprehensive(limit: Int, offset: Int): List<UserEntity>

    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY followTimestamp DESC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getFollowingByTime(limit: Int, offset: Int): List<UserEntity>
    // -------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM following_users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("UPDATE following_users SET customRemark = :newRemark WHERE id = :userId")
    suspend fun updateRemark(userId: Int, newRemark: String?)

    @Query("UPDATE following_users SET isSpecialFollow = :isSpecialFollow WHERE id = :userId")
    suspend fun updateSpecialFollow(userId: Int, isSpecialFollow: Boolean)

    @Query("SELECT COUNT(id) FROM following_users WHERE followTimestamp IS NOT NULL")
    fun getFollowingCount(): Flow<Int>

    @Query("UPDATE following_users SET followTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateFollowTimestamp(userId: Int, timestamp: Long?)

    @Query("SELECT COUNT(id) FROM following_users")
    suspend fun countUsers(): Int
}