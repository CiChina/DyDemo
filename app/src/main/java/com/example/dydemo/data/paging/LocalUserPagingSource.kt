package com.example.dydemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dydemo.data.local.database.UserDao
import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.domain.model.SortingMode

class LocalUserPagingSource(
    private val userDao: UserDao,
    private val sortingMode: SortingMode
) : PagingSource<Int, UserEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserEntity> {
        val page = params.key ?: 1 // Paging is 1-based
        return try {
            val entities = when (sortingMode) {
                SortingMode.COMPREHENSIVE -> userDao.getFollowingByComprehensive(
                    limit = params.loadSize,
                    offset = (page - 1) * params.loadSize
                )
                SortingMode.TIME_ORDER -> userDao.getFollowingByTime(
                    limit = params.loadSize,
                    offset = (page - 1) * params.loadSize
                )
            }
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (entities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
