package com.example.dydemo.domain.mapper

import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.data.remote.dto.UserDto
import com.example.dydemo.domain.model.User

/**
 * 数据映射层（已简化）
 * 负责在 DTO、Domain 和 Entity 之间进行直接的、一对一的字段映射。
 */
object UserMapper {

    /**
     * 将网络数据传输对象 (UserDto) 转换为领域模型 (User)。
     */
    fun UserDto.toUser(): User {
        return User(
            id = this.id,
            nickname = this.nickname,
            avatarUrl = this.avatarUrl, // 直接传递 URL
            authenticationLabelId = this.authenticationLabelId,
            isMutual = this.isMutual,
            isSpecialFollow = this.isSpecialFollow,
            customRemark = this.customRemark,
            followTimestamp = this.followTimestamp
        )
    }

    /**
     * 将领域模型 (User) 转换为数据库实体 (UserEntity)。
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            nickname = this.nickname,
            avatarUrl = this.avatarUrl, // 直接传递 URL
            authenticationLabelId = this.authenticationLabelId,
            isMutual = this.isMutual,
            isSpecialFollow = this.isSpecialFollow,
            customRemark = this.customRemark,
            followTimestamp = this.followTimestamp
        )
    }

    /**
     * 将数据库实体 (UserEntity) 转换为领域模型 (User)。
     */
    fun UserEntity.toUser(): User {
        return User(
            id = this.id,
            nickname = this.nickname,
            avatarUrl = this.avatarUrl, // 直接传递 URL
            authenticationLabelId = this.authenticationLabelId,
            isMutual = this.isMutual,
            isSpecialFollow = this.isSpecialFollow,
            customRemark = this.customRemark,
            followTimestamp = this.followTimestamp
        )
    }
}
