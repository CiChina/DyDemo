package com.example.dydemo.domain.model

/**
 * 关注列表的用户数据模型 (领域模型)
 *
 * @param id 用户唯一ID
 * @param nickname 昵称
 * @param avatarUrl 头像的URL地址 (从 Int 修改而来，以支持网络图片)
 * @param authenticationLabelId 认证标签ID (Int 引用 R.drawable)
 * @param isMutual 是否互相关注
 * @param isSpecialFollow 是否设置为特别关注
 * @param customRemark 自定义备注 (可为空)
 * @param followTimestamp 关注时间戳 (以毫秒为单位)
 */
data class User(
    val id: Int,
    val nickname: String,
    val avatarUrl: String, // <--- 核心修改
    val authenticationLabelId: Int,
    var isMutual: Boolean,
    var isSpecialFollow: Boolean,
    var customRemark: String?,
    var followTimestamp: Long?
)
