package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.data.model.User
import com.example.dydemo.ui.main.tabs.FollowingViewModel
import com.example.dydemo.ui.theme.DY_DarkBackground
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.ui.theme.DY_White
//import com.google.android.gms.cast.tv.cac.UserAction
import com.example.dydemo.data.UserAction
import com.example.dydemo.ui.theme.DY_LightGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// 定义一个格式化时间的函数
fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActionBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    viewModel: FollowingViewModel,
    // 【修改】添加选项选择回调
    onOptionSelected: (UserAction) -> Unit
) {
    val displayName = remember(user.customRemark, user.nickname) {
        if (!user.customRemark.isNullOrBlank()) {
            user.customRemark!!
        } else {
            user.nickname
        }
    }
    //  确定是否存在备注名
    val hasRemark = !user.customRemark.isNullOrBlank()
    // 计算第二行副标题文本
    val subtitleText = remember(hasRemark, user.nickname, user.id, user.followTimestamp) {
        // 格式化关注时间
        val followTimeText = user.followTimestamp?.let { timestamp ->
            // 注意：followTimestamp 是 Long? 类型，必须判空
            "关注时间: ${formatTimestamp(timestamp)}"
        } ?: "" // 如果时间戳为空（已取关，但在列表中不应出现），则为空字符串

        // 拼接原名部分
        val remarkPrefix = if (hasRemark) {
            "原名: ${user.nickname} ｜"
        } else {
            ""
        }

        // 最终的副标题：[原名:] ID: yyy [| 关注时间: zzz]
        "${remarkPrefix}ID: ${user.id}\n$followTimeText"
    }
    val scope = rememberCoroutineScope()
    // 确保 BottomSheet 默认是全展开状态
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 状态管理：控制备注对话框
    var showRemarkDialog by remember { mutableStateOf(false) }
    // 状态管理：控制取关确认对话框
    var showUnfollowConfirm by remember { mutableStateOf(false) }

    // 1. 设置备注对话框
    if (showRemarkDialog) {
        // 使用 CustomDialogs.kt 中定义的对话框
        SetRemarkDialog(
            initialRemark = user.customRemark,
            onDismissRequest = { showRemarkDialog = false },
            onConfirm = { newRemark ->
                scope.launch {
                    viewModel.onUpdateRemark(user.id, newRemark)
                }
            }
        )
    }

    // 2. 取关确认对话框
    if (showUnfollowConfirm) {
        AlertDialog(
            onDismissRequest = { showUnfollowConfirm = false },
            title = { Text("确认取关", color = DY_White) },
            text = { Text("您确定要取消关注 ${user.nickname} 吗？此操作不可撤销。", color = DY_White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.onUnfollowUser(user.id)
                            onDismiss() // 关闭 Bottom Sheet
                        }
                        showUnfollowConfirm = false
                    }
                ) {
                    Text("取关", color = DY_PrimaryRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfollowConfirm = false }) {
                    Text("取消", color = DY_White)
                }
            },
            containerColor = DY_DarkBackground,
        )
    }

    // 3. 底部操作表单 (Bottom Sheet)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DY_DarkBackground // 底部表单背景色
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 用户头像和昵称 (顶部信息)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 占位头像 (与 ListItem 中保持一致)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = user.nickname.first().toString(),
                        color = DY_White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Spacer(modifier = Modifier.width(16.dp))

                // 【核心修改】将昵称和副标题放入 Column 中
                Column {

                    // 优先备注名/昵称 (第一行)
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DY_White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 【关键修改】副标题行 (始终存在)
                    Text(
                        text = subtitleText, // <-- 使用计算出的副标题文本
                        color = DY_LightGray.copy(alpha = 0.7f), // 较暗淡的颜色
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Divider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            // 操作项 1: 设置特别关注
            ActionItem(
                // 修正图标名称：使用 Notifications (开启) 和 NotificationsOff (关闭)
                icon = if (user.isSpecialFollow) Icons.Filled.FavoriteBorder else Icons.Filled.Notifications,
                text = if (user.isSpecialFollow) "取消特别关注" else "设为特别关注",
                onClick = {
                    // 调用 ViewModel 逻辑，Flow 会自动更新 UI
                    scope.launch {
                        viewModel.onToggleSpecialFollow(user.id)
                        onDismiss()
                    }
                }
            )

            // 操作项 2: 设置备注
            ActionItem(
                icon = Icons.Default.Edit,
                text = "设置备注",
                onClick = {
                    onDismiss() // 1. 关闭 Bottom Sheet
                    // 2. 【关键修改】通过回调通知外部（FollowingScreen）执行备注编辑操作
                    onOptionSelected(UserAction.REMARK_EDIT)
                }
            )

            ActionItem(
                icon = Icons.Default.Close,
                text = "取消关注",
                textColor = DY_PrimaryRed,
                onClick = {
                    // 1. 【核心修改】直接调用 ViewModel 中的取关逻辑
                    // 假设您的 ViewModel 中有一个方法来处理关注状态切换。
                    // 我们将 isFollowing 设置为 false 来取消关注。
                    viewModel.onFollowToggle(user.id)

                    // 2. 关闭 Bottom Sheet
                    onDismiss()

                    // 注意：原有的 showUnfollowConfirm = true 逻辑被移除
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Bottom Sheet 中的单个操作项
 */
@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color = DY_White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = textColor, fontSize = 16.sp)
    }
}