package com.example.dydemo.data.remote

import com.example.dydemo.data.remote.dto.UserDto

/**
 * API 接口定义。
 * 定义获取关注列表的挂起函数，接收 page 和 pageSize 参数。
 */
interface FollowingApiService {

    /**
     * 从服务端获取关注列表分页数据
     * @param page 请求的页码
     * @param size 每页的数据量
     * @return UserDto 列表
     */
    suspend fun getFollowing(page: Int, size: Int): List<UserDto>
}
