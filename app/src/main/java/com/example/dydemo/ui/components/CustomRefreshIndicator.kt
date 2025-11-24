package com.example.dydemo.ui.components

/**
 * 自定义的刷新标志，暂时废弃
 */
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState

val SPINNER_SIZE = 24.dp
val CROSSFADE_DURATION_MILLIS = 150
val INDICATOR_BACKGROUND = Color(0xFF333333) // 深灰色背景

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White
) {
    // 整个指示器的容器 (圆形背景)
    Box(
        modifier = modifier
            .size(40.dp) // 指示器尺寸
            .clip(CircleShape)
            .background(INDICATOR_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = CROSSFADE_DURATION_MILLIS),
            modifier = Modifier.align(Alignment.Center)
        ) { refreshing ->
            if (refreshing) {
                // 1. 刷新中状态: 显示加载圈
                CircularProgressIndicator(
                    modifier = Modifier.size(SPINNER_SIZE),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                // 2. 下拉中状态: 显示图标并根据下拉距离缩放
                val distanceFraction = { state.distanceFraction.coerceIn(0f, 1f) }
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = contentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            val progress = distanceFraction()
                            // 缩放图标 (从 0 到 1)
                            this.scaleX = progress
                            this.scaleY = progress
                        }
                )
            }
        }
    }
}