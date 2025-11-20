package com.example.dydemo.ui.main.tabs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dydemo.data.SortingMode
import com.example.dydemo.data.model.User
import com.example.dydemo.ui.components.UserActionBottomSheet
import com.example.dydemo.ui.components.UserListItem
import com.example.dydemo.ui.theme.DY_DarkBackground
import com.example.dydemo.ui.theme.DY_MediumGray
import com.example.dydemo.ui.theme.DY_PrimaryRed
//import com.google.android.gms.cast.tv.cac.UserAction
import com.example.dydemo.data.UserAction
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api


// 必须使用 @OptIn 标记，因为 Pull-to-Refresh API 仍是 Material 3 的实验性部分
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    viewModel: FollowingViewModel = hiltViewModel()
) {

    // 获取当前的 Context，用于显示 Toast 消息
    val context = LocalContext.current // <--- 新增

    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    // 1. 创建 PullToRefreshState
    val pullToRefreshState = rememberPullToRefreshState()

    // 首次初始化数据
    LaunchedEffect(Unit) {
        viewModel.initializeData() // <--- 这行代码负责数据的初始加载
    }

    // 列表为空时的处理逻辑 (保持不变)
    if (state.followingUsers.isEmpty() && !state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无关注", color = DY_MediumGray)
        }
        return // 如果为空且不加载，直接返回，避免渲染 PullToRefreshBox
    }

    // 【核心修改 2】使用 PullToRefreshBox 包装 LazyColumn
    PullToRefreshBox(
        isRefreshing = state.isLoading, // 从 ViewModel 中获取 isLoading 状态
        onRefresh = viewModel::refreshData, // 下拉触发时，调用 ViewModel 的刷新逻辑
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        // 【自定义指示器】使用我们之前定义的 CustomRefreshIndicator
//        indicator = {
////            RefreshIndicator()
//            CustomRefreshIndicator(
//                state = pullToRefreshState,
//                isRefreshing = state.isLoading,
//                // 将指示器对齐到容器的顶部中心，并设置 padding
//                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
//                contentColor = DY_White
//            )
//        }
    ){
        // 核心列表
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // 使得元素分散
                ) {
                    // 左侧：关注总数
                    Text(
                        text = "我的关注 (${state.followingUsers.size}人)",
                        color = DY_MediumGray
                    )

                    // 【新增按钮】右侧：排序切换按钮
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.toggleSortingMode() } // <--- 切换排序模式
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isTimeOrder = state.currentSortingMode == SortingMode.TIME_ORDER

                        // 按钮文本
                        Text(
                            text = if (isTimeOrder) "按时间顺序" else "综合排序",
                            color = DY_MediumGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp // 假设需要小一点的字体，需要导入 androidx.compose.ui.unit.sp
                        )

                        Spacer(Modifier.width(4.dp))

                        // 排序图标 (o)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List, // <--- 使用 Sort 图标
                            contentDescription = "切换排序",
                            tint = DY_MediumGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            items(state.followingUsers, key = { it.id }) { user ->
                UserListItem(
                    user = user,
                    // 【核心修复】传递 pendingFollowActions Map
                    pendingFollowActions = state.pendingFollowActions,
                    onFollowToggle = viewModel::onFollowToggle,
                    // 【Item 点击逻辑】
                    onItemClick = { clickedUser ->
                        // 1. 弹出 Toast
                        val message = "已选中 ${clickedUser.nickname}"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        // 2. 这里通常是跳转到用户主页的逻辑
                    },

                    // 【“已关注”按钮点击逻辑】
                    onFollowButtonClick = { clickedUser ->
                        // 1. 触发 ViewModel 取消关注，Flow 会自动更新 UI，按钮颜色/文字会根据状态变化（假设您的FollowButton有状态）
                        viewModel.onUnfollowUser(clickedUser.id)

                        // 2. 可以给一个 Toast 提示
                        val message = "已取消关注 ${clickedUser.nickname}"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },

                    // 【“···”省略号点击逻辑】
                    onMoreOptionsClick = { clickedUser ->
                        // 1. 选中用户并显示底部操作弹窗
                        selectedUser = clickedUser
                        showActionDialog = true
                    }
                )
                Divider(
                    color = DY_MediumGray.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }

//    // 2. 使用 Box 作为容器，并应用 nestedScroll 修饰符
//    // nestedScroll 必须与 PullToRefreshState 关联
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
////            .nestedScroll(pullToRefreshState.nestedScrollConnection) // 连接滚动事件
//    ) {
//
//        // --- 列表内容 ---
//        if (state.followingUsers.isEmpty() && !state.isLoading) {
//            // 空列表状态
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("暂无关注", color = DY_MediumGray)
//            }
//        } else {
//            // 核心列表
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                item {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween // 使得元素分散
//                    ) {
//                        // 左侧：关注总数
//                        Text(
//                            text = "我的关注 (${state.followingUsers.size}人)",
//                            color = DY_MediumGray
//                        )
//
//                        // 【新增按钮】右侧：排序切换按钮
//                        Row(
//                            modifier = Modifier
//                                .clickable { viewModel.toggleSortingMode() } // <--- 切换排序模式
//                                .padding(horizontal = 8.dp, vertical = 4.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            val isTimeOrder = state.currentSortingMode == SortingMode.TIME_ORDER
//
//                            // 按钮文本
//                            Text(
//                                text = if (isTimeOrder) "按时间顺序" else "综合排序",
//                                color = DY_MediumGray,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 16.sp // 假设需要小一点的字体，需要导入 androidx.compose.ui.unit.sp
//                            )
//
//                            Spacer(Modifier.width(4.dp))
//
//                            // 排序图标 (o)
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Filled.List, // <--- 使用 Sort 图标
//                                contentDescription = "切换排序",
//                                tint = DY_MediumGray,
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//                    }
//                }
//
//                items(state.followingUsers, key = { it.id }) { user ->
//                    UserListItem(
//                        user = user,
//                        // 【核心修复】传递 pendingFollowActions Map
//                        pendingFollowActions = state.pendingFollowActions,
//                        onFollowToggle = viewModel::onFollowToggle,
//                        // 【Item 点击逻辑】
//                        onItemClick = { clickedUser ->
//                            // 1. 弹出 Toast
//                            val message = "已选中 ${clickedUser.nickname}"
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//
//                            // 2. 这里通常是跳转到用户主页的逻辑
//                        },
//
//                        // 【“已关注”按钮点击逻辑】
//                        onFollowButtonClick = { clickedUser ->
//                            // 1. 触发 ViewModel 取消关注，Flow 会自动更新 UI，按钮颜色/文字会根据状态变化（假设您的FollowButton有状态）
//                            viewModel.onUnfollowUser(clickedUser.id)
//
//                            // 2. 可以给一个 Toast 提示
//                            val message = "已取消关注 ${clickedUser.nickname}"
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                        },
//
//                        // 【“···”省略号点击逻辑】
//                        onMoreOptionsClick = { clickedUser ->
//                            // 1. 选中用户并显示底部操作弹窗
//                            selectedUser = clickedUser
//                            showActionDialog = true
//                        }
//                    )
//                    Divider(
//                        color = DY_MediumGray.copy(alpha = 0.2f),
//                        thickness = 0.5.dp,
//                        modifier = Modifier.padding(start = 72.dp)
//                    )
//                }
//            }
//        }

    // 【新增】备注编辑弹窗逻辑
    state.userToEditRemark?.let { user ->
        RemarkEditDialog(
            userNickname = user.nickname,
            initialRemark = state.currentRemarkInput,
            onInputChange = viewModel::updateRemarkInput,
            onClearInput = viewModel::clearRemarkInput,
            onDismiss = viewModel::hideRemarkDialog,
            onConfirm = viewModel::saveRemark
        )
    }


    // --- 底部操作弹窗 ---
    if (showActionDialog && selectedUser != null) {
        UserActionBottomSheet(
            user = selectedUser!!,
            onDismiss = { showActionDialog = false },
            viewModel = viewModel,
            // 【关键修改】捕获备注编辑的点击事件
            onOptionSelected = { actionType ->
                // 不需要关闭 showActionDialog = false，因为它已经在 Sheet 内部调用 onDismiss() 了。
                // 我们只需要在 Sheet 关闭后执行需要的操作。
                when (actionType) {
                    UserAction.REMARK_EDIT -> {
                        // 触发 ViewModel 状态变化，从而显示 RemarkEditDialog
                        viewModel.showRemarkDialog(selectedUser!!)
                    }
                    else -> {/* 其它操作 */ }
                }
            }
        )
    }
}