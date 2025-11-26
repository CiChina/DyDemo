package com.example.dydemo.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 用户数据传输对象 (DTO)
 *
 * 该结构用于精确映射模拟的服务端 API 返回的数据。
 * 它与本地的 User/UserEntity 模型解耦，两者之间的转换将由 Mapper 层负责。
 *
 * @param id 用户唯一 ID
 * @param nickname 昵称
 * @param avatarUrl 头像的远程 URL 地址，以支持 Coil 等库进行高性能加载。
 * @param authenticationLabelId 认证标签的资源 ID，由服务端提供，UI 直接使用。
 * @param isMutual 是否互相关注
 * @param isSpecialFollow 是否设置为特别关注
 * @param customRemark 自定义备注 (可为空)
 * @param followTimestamp 关注时间戳 (可为空)
 */
@Serializable
data class UserDto(
    val id: Int,
    val nickname: String,
    val avatarUrl: String,
    val authenticationLabelId: Int,
    val isMutual: Boolean,
    val isSpecialFollow: Boolean,
    val customRemark: String?,
    val followTimestamp: Long?
)
