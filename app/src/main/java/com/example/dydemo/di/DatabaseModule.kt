package com.example.dydemo.di

/**
 * 依赖注入层：Hilt配置应独立于数据层
 * 注入数据库、DAO、Repository等依赖
 */
import android.content.Context
import androidx.room.Room
import com.example.dydemo.data.local.database.AppDatabase
import com.example.dydemo.data.local.database.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 单例组件，生命周期与应用一致
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "douyin_clone_db" // 数据库名称
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}