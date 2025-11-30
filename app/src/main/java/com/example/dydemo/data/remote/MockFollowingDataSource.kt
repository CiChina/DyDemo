package com.example.dydemo.data.remote

import com.example.dydemo.data.remote.dto.UserDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.min

/**
 * 模拟服务端数据源，实现 API 接口
 * Hilt 会将这个类的实例提供给需要 FollowingApiService 的地方。
 */
class MockFollowingDataSource @Inject constructor() : FollowingApiService {

    companion object {
        private val allUsers: List<UserDto>

        // 准备一组高质量的头像 URL，用于模拟真实的用户头像
        private val avatarUrls = listOf(
            "https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1043471/pexels-photo-1043471.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1559486/pexels-photo-1559486.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1080213/pexels-photo-1080213.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/614810/pexels-photo-614810.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/733872/pexels-photo-733872.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/91227/pexels-photo-91227.jpeg?auto=compress&cs=tinysrgb&w=600",
            "https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg?auto=compress&cs=tinysrgb&w=600"
        )

        // 在伴生对象的 init 块中预先生成1000个用户数据，避免重复创建
        init {
            val userList = mutableListOf<UserDto>()
            val currentTime = System.currentTimeMillis()
            for (i in 1..1000) {
                userList.add(
                    UserDto(
                        id = i,
                        nickname = "用户 $i",
                        avatarUrl = avatarUrls[i % avatarUrls.size],
                        // authenticationLabelId 使用 0 作为占位符，表示没有认证图标
                        authenticationLabelId = 0,
                        isMutual = i % 5 == 0,
                        isSpecialFollow = i % 50 == 0,
                        customRemark = if (i % 30 == 0) "我的好友 $i" else null,
                        // 时间戳递减，模拟关注时间的先后顺序
                        followTimestamp = currentTime - (1000 - i) * 1000 * 60 * 5
                    )
                )
            }
            allUsers = userList
        }
    }

    /**
     * 实现接口方法，模拟网络请求并返回分页数据
     * @param page 页码（1-based）
     * @param size 每页数量
     */
    override suspend fun getFollowing(page: Int, size: Int): List<UserDto> {
        // 模拟 300ms 的网络延迟
        delay(300)

        val start = (page - 1) * size
        // 如果请求的起始位置超出总数据量，返回空列表
        if (start >= allUsers.size) {
            return emptyList()
        }

        // 计算结束位置，防止越界
        val end = min(start + size, allUsers.size)
        return allUsers.subList(start, end)
    }
}
