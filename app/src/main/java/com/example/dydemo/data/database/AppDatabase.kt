package com.example.dydemo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dydemo.data.model.UserEntity

// 数据库版本号，如果表结构变化需要升级
@Database(entities = [UserEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // 暴露 DAO 接口
    abstract fun userDao(): UserDao
}