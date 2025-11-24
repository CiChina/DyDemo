package com.example.dydemo.data.source

import android.app.Application
import com.example.dydemo.data.local.entity.UserEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonDataSource @Inject constructor(
    private val application: Application // 注入 Application 上下文以访问 assets
) {
    // 惰性解析 JSON 数据，配置忽略未知键
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // 从 assets 文件夹读取文件内容
    private fun loadJsonFromAsset(fileName: String): String {
        return try {
            application.assets.open(fileName).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer, Charsets.UTF_8)
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            ""
        }
    }

    // 暴露给 Repository 的方法
    fun getInitialUsers(): List<UserEntity> {
        val jsonString = loadJsonFromAsset("initial_users.json")
        if (jsonString.isEmpty()) return emptyList()

        return try {
            // 解析 JSON 字符串为 List<UserEntity>
            json.decodeFromString<List<UserEntity>>(jsonString)
        } catch (e: Exception) {
            // 打印解析错误
            e.printStackTrace()
            emptyList()
        }
    }
}