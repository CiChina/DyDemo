package com.example.dydemo.ui.main

/**
 * 根屏幕/导航容器
 */
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.ui.main.tabs.*
import com.example.dydemo.ui.theme.DY_DarkBackground
import com.example.dydemo.ui.theme.DY_White
import kotlinx.coroutines.launch

// 定义 Tab 页面列表
enum class TabPage(val title: String) {
    MUTUAL("互关"),
    FOLLOWING("关注"),
    FANS("粉丝"),
    FRIENDS("朋友")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val pages = TabPage.entries.toTypedArray()
    val pagerState = rememberPagerState(initialPage = TabPage.FOLLOWING.ordinal, pageCount = { pages.size })

    Scaffold(
        topBar = { CustomTabBar(pages = pages, pagerState = pagerState) },
        containerColor = MaterialTheme.colorScheme.background // 整个页面的背景色
    ) { paddingValues ->
        // 使用 HorizontalPager 实现左右滑动切换 Tab
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            // 根据当前 Page 索引显示对应的 Composable
            when (pages[page]) {
                TabPage.MUTUAL -> EmptyTabScreen(name = "互关")
                TabPage.FOLLOWING -> FollowingScreen() // 核心关注列表
                TabPage.FANS -> EmptyTabScreen(name = "粉丝")
                TabPage.FRIENDS -> EmptyTabScreen(name = "朋友")
            }
        }
    }
}

// 占位用的空页面 Composable
@Composable
fun EmptyTabScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无$name",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 18.sp
        )
    }
}


// 顶部的四选一bar
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomTabBar(pages: Array<TabPage>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    // 最外层 Column 包含整个 TabBar
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // 背景色给到 Column
    ) {
        // Tab Text 和 指示器的 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp), // 顶部填充，留出空间
            horizontalArrangement = Arrangement.Center, // 文本和指示器组居中排列
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(48.dp)) // 左侧占位

            pages.forEachIndexed { index, page ->
                val isSelected = pagerState.currentPage == index

                // 每个 Tab 标题和指示器作为一个独立的 Column 组合
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp) // 每个 Tab 之间的水平间距
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally // Text 和 Box 居中对齐
                ) {
                    // 1. Tab 文本
                    Text(
                        text = page.title,
                        fontSize = 20.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )

                    // 2. 选中 Tab 的指示器 (下划线)
                    // 指示器高度 3dp，且与 Text 在同一个 Column 中居中对齐
                    Spacer(modifier = Modifier.height(16.dp)) // 文本和下划线之间的间距
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 40.dp else 0.dp) // 选中时宽度，未选中时为0
                            .height(if (isSelected) 3.dp else 0.dp) // 选中时高度，未选中时为0
                            .background(MaterialTheme.colorScheme.onBackground) // 选中的指示器颜色
                    )
                }
            }
            Spacer(Modifier.width(48.dp)) // 右侧占位
        }

        // 【全局细线】 (Divider)
        // 放置在 Tab 文本和指示器 Row 的正下方
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), // 细线高度
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // 未选中页面的文本颜色或半透明白
        )
    }
}