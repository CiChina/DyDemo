package com.example.dydemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room 数据库中的用户实体，对应数据库表结构
 */
@Entity(tableName = "following_users")
@Serializable
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val nickname: String,
    val avatarUrl: String, // <--- 核心修改
    val authenticationLabelId: Int,
    val isMutual: Boolean,
    val isSpecialFollow: Boolean,
    val customRemark: String?,
    var followTimestamp: Long?  // 关注时间戳 (以毫秒为单位)
)
