package com.example.dydemo.data.local.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dydemo.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 数据库访问对象
 */
@Dao
interface UserDao {

    /**
     * 【核心修改】提供给 Paging 3 的数据源 - 综合排序
     * 分页数据来自 Mock API，但如果未来需要缓存或本地筛选，则需要这个接口。
     * 排序逻辑：特别关注 (DESC), 关注时间 (ASC)
     */
    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY isSpecialFollow DESC, followTimestamp ASC")
    fun getFollowingPagingSourceByComprehensive(): PagingSource<Int, UserEntity>

    /**
     * 【核心修改】提供给 Paging 3 的数据源 - 时间排序
     * 排序逻辑：关注时间 DESC
     */
    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY followTimestamp DESC, id ASC")
    fun getFollowingPagingSourceByTime(): PagingSource<Int, UserEntity>


    /**
     * 【核心修改】实时获取关注列表。
     * 关键：只选择 followTimestamp 不为 NULL 的用户（即当前被关注的用户）。
     */
    // 【综合排序】优先级：特别关注, 关注时间
    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY isSpecialFollow DESC, followTimestamp ASC")
    fun getFollowingUsersByComprehensive(): Flow<List<UserEntity>>

    /**
     * 【核心修改】时间排序：只选择 followTimestamp 不为 NULL 的用户。
     * 优先级：关注时间 DESC (最新关注的排在最前面)
     */
    @Query("SELECT * FROM following_users WHERE followTimestamp IS NOT NULL ORDER BY followTimestamp DESC, id ASC")
    fun getFollowingUsersByTime(): Flow<List<UserEntity>>

    /**
     * 插入初始数据或更新现有数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    /**
     * 更新整个 UserEntity 对象。
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * 【新增】根据 ID 获取单个用户，用于 ViewModel 或 Repository 获取当前用户的最新状态。
     */
    @Query("SELECT * FROM following_users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity? // 返回可空 UserEntity

    /**
     * 更新用户的 customRemark 字段。
     * 使用 @Query 替代 @Update 整个对象，可以减少 Room 的开销。
     */
    @Query("UPDATE following_users SET customRemark = :newRemark WHERE id = :userId")
    suspend fun updateRemark(userId: Int, newRemark: String?)

    /**
     * 更新用户的 isSpecialFollow 状态。
     */
    @Query("UPDATE following_users SET isSpecialFollow = :isSpecialFollow WHERE id = :userId")
    suspend fun updateSpecialFollow(userId: Int, isSpecialFollow: Boolean)

    /**
     * 模拟取关：根据 ID 删除用户
     */
    @Query("DELETE FROM following_users WHERE id = :userId")
    suspend fun deleteById(userId: Int)


    /**
     * 用于判断数据库是否为空，防止重复插入初始数据。
     */
    @Query("SELECT COUNT(id) FROM following_users")
    suspend fun countUsers(): Int

    /**
     * 取关/关注操作：根据 ID 更新 followTimestamp。
     * - 设置为 NULL：取关
     * - 设置为 Long：关注
     */
    @Query("UPDATE following_users SET followTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateFollowTimestamp(userId: Int, timestamp: Long?)

    //特别关注
    @Query("UPDATE following_users SET isSpecialFollow = :isSpecialFollow WHERE id = :userId")
    suspend fun updateSpecialFollowStatus(userId: Int, isSpecialFollow: Boolean)

}