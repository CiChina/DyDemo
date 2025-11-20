package com.example.dydemo.data.model

import java.net.PasswordAuthentication

/**
 * 关注列表的用户数据模型
 * @param id 用户唯一ID
 * @param nickname 昵称
 * @param avatarResId 头像资源ID (Int 引用 R.drawable)
 * @param authenticationLabelId 认证标签ID (Int 引用 R.drawable)
 * @param isMutual 是否互相关注
 * @param isSpecialFollow 是否设置为特别关注
 * @param customRemark 自定义备注 (可为空)
 */
data class User(
    val id: Int,
    val nickname: String,
    val avatarResId: Int,
    val authenticationLabelId: Int,
    var isMutual: Boolean,
    var isSpecialFollow: Boolean,
    var customRemark: String?,
    var followTimestamp: Long?  // 【新增字段】关注时间戳 (以毫秒为单位)
)