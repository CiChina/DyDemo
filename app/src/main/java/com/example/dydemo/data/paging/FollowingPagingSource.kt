package com.example.dydemo.data.paging

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dydemo.data.remote.FollowingApiService
import com.example.dydemo.domain.mapper.UserMapper.toUser
import com.example.dydemo.domain.model.User

import java.io.IOException
import javax.inject.Inject

// 定义分页加载的起始页码
private const val STARTING_PAGE_INDEX = 1

/**
 * Paging 3 的核心组件，负责从服务端按需加载分页数据。
 *
 * @param apiService 用于获取网络数据的 API 服务。
 */
class FollowingPagingSource @Inject constructor(
    private val apiService: FollowingApiService
) : PagingSource<Int, User>() {

    /**
     * 当需要加载新数据时，Paging 库会调用此方法。
     *
     * @param params 包含有关加载操作的信息，如要加载的页码 (key) 和加载数量 (loadSize)。
     * @return 返回一个 LoadResult 对象，可以是成功加载的 LoadResult.Page，也可以是加载失败的 LoadResult.Error。
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        // 确定当前要加载的页码。如果是首次加载 (params.key == null)，则从起始页码开始。
        val page = params.key ?: STARTING_PAGE_INDEX

        return try {
            // 1. 调用 API 从模拟数据源获取分页数据 (List<UserDto>)
            val dtoList = apiService.getFollowing(page, params.loadSize)

            // 2. 使用 Mapper 将 List<UserDto> 转换为 List<User> (领域模型)
            val userList = dtoList.map { it.toUser() }

            // 3. 构建并返回 LoadResult.Page
            LoadResult.Page(
                data = userList,
                // 如果是第一页，则没有上一页
                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                // 如果返回的数据列表为空，说明没有更多数据了，下一页的 key 为 null
                nextKey = if (userList.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            // IOException 表示可能存在网络问题
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // HttpException 表示非 2xx 的 HTTP 响应
            LoadResult.Error(e)
        }
    }

    /**
     * 用于在数据刷新或失效后，确定从哪一页开始重新加载。
     *
     * @param state 包含有关已加载页面和最近访问位置的信息。
     * @return 返回一个新的刷新键 (页码)，Paging 库将用它来开始新的加载。
     */
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        // 尝试从最近的锚点位置（例如屏幕上最后显示的位置）找到对应的页码
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
