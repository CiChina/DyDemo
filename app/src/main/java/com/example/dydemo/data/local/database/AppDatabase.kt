package com.example.dydemo.data.local.database

/**
 * Room数据库配置
 */
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dydemo.data.local.entity.UserEntity

// 数据库版本号，如果表结构变化需要升级
@Database(entities = [UserEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // 暴露 DAO 接口
    abstract fun userDao(): UserDao
}