package com.example.dydemo.domain.mapper

import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.domain.model.User

/**
 * 负责 UserEntity (数据库) 和 User (UI 领域) 之间的相互转换
 */
object UserMapper {

    // 从数据库实体转换为 UI 领域模型
    fun mapFromEntity(entity: UserEntity): User {
        return User(
            id = entity.id,
            nickname = entity.nickname,
            avatarResId = entity.avatarResId,
            authenticationLabelId = entity.authenticationLabelId,
            isMutual = entity.isMutual,
            isSpecialFollow = entity.isSpecialFollow,
            customRemark = entity.customRemark,
            followTimestamp = entity.followTimestamp
        )
    }

    // 从 UI 领域模型转换为数据库实体
    fun mapToEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            nickname = user.nickname,
            avatarResId = user.avatarResId,
            authenticationLabelId = user.authenticationLabelId,
            isMutual = user.isMutual,
            isSpecialFollow = user.isSpecialFollow,
            customRemark = user.customRemark,
            followTimestamp = user.followTimestamp,
        )
    }

    // 列表转换的辅助函数
    fun mapFromEntityList(entities: List<UserEntity>): List<User> {
        return entities.map { mapFromEntity(it) }
    }
}