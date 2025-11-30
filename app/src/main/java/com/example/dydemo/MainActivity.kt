package com.example.dydemo

import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.example.dydemo.ui.main.MainScreen
import com.example.dydemo.ui.theme.DyDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 【新增】开启最高帧率
        // 请求使用设备的最高刷新率
        val windowManager = windowManager
        val layoutParams = window.attributes

        // 获取设备支持的所有显示模式
        val supportedModes = windowManager.defaultDisplay.supportedModes

        // 找到支持的最高刷新率
        val highestRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }

        // 如果找到了最高刷新率模式，则应用它
        highestRefreshRateMode?.let {
            layoutParams.preferredDisplayModeId = it.modeId
            window.attributes = layoutParams
        }

        setContent {
            // 3. 应用深色主题
            DyDemoTheme {
                // 4. 使用 Surface 作为容器，应用背景色
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 5. 启动主界面 Composable
                    MainScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DyDemoTheme {
        MainScreen()
    }
}
