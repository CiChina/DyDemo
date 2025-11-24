package com.example.dydemo.di

/**
 * 注入新的 PagingSource 和远程数据源的实例。
 */

import com.example.dydemo.data.remote.FollowingApiService
import com.example.dydemo.data.remote.MockFollowingDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用模块：负责绑定和提供远程数据和 Paging 相关的依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * 将 MockFollowingDataSource 绑定到 FollowingApiService 接口。
     * 在生产环境中，这里应绑定 Retrofit 实现。
     */
    @Singleton
    @Binds
    abstract fun bindFollowingApiService(
        mockDataSource: MockFollowingDataSource
    ): FollowingApiService

    // PagingSource 会通过其构造函数自动注入依赖，无需在这里显式提供。
}

