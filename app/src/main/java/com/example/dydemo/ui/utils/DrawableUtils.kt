package com.example.dydemo.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

/**
 * 安全地从资源 ID 加载 Drawable 并将其转换为 Bitmap。
 */
/**
 * 解决 LayerList 渲染偏移问题的工具函数：
 * 强制在高分辨率空间 (sourceSizePx=2000) 绘制，然后降采样到目标尺寸 (targetSizePx=128)。
 * * @param targetSizePx Compose 实际需要的尺寸 。
 * @param sourceSizePx XML 坐标系所需的最小绘制尺寸。
 */
fun getBitmapFromDrawable(context: Context, drawableId: Int, targetSizePx: Int = 128, sourceSizePx: Int = 2000): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    // 1. 高精度渲染 (保证坐标不偏移)

    // 创建高分辨率的 Bitmap，用于精确绘制
    val highResBitmap = createBitmap(sourceSizePx, sourceSizePx)

    val canvas = Canvas(highResBitmap)

    // 强制 Drawable 绘制到 2000x2000 的区域
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    // 2. 降采样

    try {
        // 创建最终的小尺寸 Bitmap，并进行缩放
        val finalBitmap = highResBitmap.scale(targetSizePx, targetSizePx)

        // 确保回收高分辨率的中间 Bitmap，释放内存
        highResBitmap.recycle()

        return finalBitmap

    } catch (e: Exception) {
        // 处理内存不足等异常
        highResBitmap.recycle()
        return null
    }
}