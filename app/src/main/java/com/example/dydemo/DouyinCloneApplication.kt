package com.example.dydemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DouyinDemoApplication : Application() {
    // 应用程序启动时 Hilt 会自动初始化
}