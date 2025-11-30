package com.example.dydemo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//  1. 定义基于 DY_ 颜色的深色配色方案
private val DyDarkColorScheme = darkColorScheme(
    // 核心/强调色：用于按钮和高亮
    primary = DY_PrimaryRed,
    onPrimary = DY_White,

    // 背景色：用于整体背景和主要容器
    background = DY_DarkBackground,
    onBackground = DY_White,

    // 表面色：用于次要容器、卡片、输入框 (使用 DY_InputBackground)
    surface = DY_InputBackground,
    onSurface = DY_LightGray,

    surfaceVariant = DY_InputBackground,
    onSurfaceVariant = DY_LightGray,

    // 次要颜色 (可选，但推荐填充，保持 DY 风格)
    secondary = DY_MediumGray,
    onSecondary = DY_White,

    // 错误色
    error = DY_PrimaryRed
)

//  2. 定义基于 DY_ 颜色的浅色配色方案 (Day Mode)
private val DyLightColorScheme = lightColorScheme(
    // 核心/强调色：保持一致
    primary = DY_PrimaryRed,
    onPrimary = DY_White,

    // 背景色：设置为白色
    background = DY_White,
    onBackground = DY_Black,          // 背景上的文本设为黑色

    surface = Color(0xFFF0F0F0),
    onSurface = DY_MediumGray,        // 表面上的文本色

    surfaceVariant = DY_LightGray,
    onSurfaceVariant = DY_InputBackground,

    // 次要颜色
    secondary = DY_MediumGray,
    onSecondary = DY_White,

    // 错误色
    error = DY_PrimaryRed
)

@Composable
fun DyDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DyDarkColorScheme
        else -> DyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}