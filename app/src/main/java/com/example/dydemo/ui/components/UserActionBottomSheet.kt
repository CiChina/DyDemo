package com.example.dydemo.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.dydemo.ui.theme.DY_InputBackground
import com.example.dydemo.ui.theme.DY_LightGray
import com.example.dydemo.ui.theme.DY_MediumGray
import com.example.dydemo.ui.utils.getBitmapFromDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// 定义一个格式化时间的函数
fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@SuppressLint("LocalContextResourcesRead")
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

    // 获取 Context 以便显示 Toast
    val context = LocalContext.current

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
        containerColor = DY_DarkBackground, // 底部表单背景色
        // 【核心修改】将 dragHandle 设置为 null 以隐藏最上方的横条
        dragHandle = null
    ) {
        // 【新增】右上角关闭按钮
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(top = 24.dp, end = 24.dp)
                .size(32.dp) // 按钮大小
                .align(Alignment.End)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp) // 【修正 1】将视觉背景尺寸缩小到 24dp
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center // 确保图标居中
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = DY_White,
                    modifier = Modifier.size(28.dp) // 【修正 2】将图标尺寸缩小到 16dp，使其在 24dp 圈内居中且比例协调
                )
            }
        }
        Column(modifier = Modifier.padding(end=16.dp, start = 16.dp, bottom = 16.dp)) {

            // 用户头像和昵称 (顶部信息)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // --- 1. 头像 ---
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DY_InputBackground) // 占位背景色，作为图片加载失败时的背景
                ) {
                    val avatarBitmap = remember(user.avatarResId) {
                        if (user.avatarResId > 0) {
                            val resName =
                                "rand_avatar_${user.avatarResId.toString().padStart(2, '0')}"

                            // 1. 查找资源 ID
                            val actualResId = context.resources.getIdentifier(
                                resName, "drawable", context.packageName
                            )

                            if (actualResId > 0) {
                                // 这里会使用默认的 sizePx = 128
                                return@remember getBitmapFromDrawable(context, actualResId)
                            }
                        }
                        null // ID 无效或未找到
                    }

                    if (avatarBitmap != null) {
                        // 使用 BitmapPainter 显示 Bitmap
                        Image(
                            painter = BitmapPainter(avatarBitmap.asImageBitmap()), // ✅ 使用 BitmapPainter
                            contentDescription = "用户头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        // 占位符 Text
                        Text(
                            text = user.nickname.firstOrNull()?.toString() ?: "",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))
                Spacer(modifier = Modifier.width(16.dp))

                // 将昵称和副标题放入 Column 中
                Column {

                    // 优先备注名/昵称 (第一行)
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DY_White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 副标题行 (始终存在)
                    Text(
                        text = subtitleText, // <-- 使用计算出的副标题文本
                        color = DY_LightGray.copy(alpha = 0.7f), // 较暗淡的颜色
                        fontSize = 14.sp
                    )
                }
            }
//            Spacer(Modifier.height(16.dp))
//
//            Divider(color = Color.Gray.copy(alpha = 0.3f))
//            Spacer(Modifier.height(8.dp))
        }

        // --- 底部操作按钮区 (核心修改) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 1. 特别关注 / 设置备注 (合并的圆角矩形，上下排列) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 设置高度为 112dp，由内部 weight 决定分配
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.4f)),
            ) {
                // 操作项 1: 设置特别关注 (上部)
                Box(
                    // 【核心修改】使用 1.1 的权重，使其稍微高一点 (占 55% 的高度)
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                ) {
                    SpecialFollowItem(
                        user = user,
                        viewModel = viewModel,
                        scope = scope,
                    )
                }


                // 分隔线 (水平细线)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray.copy(alpha = 0.6f))
                )

                // 操作项 2: 设置备注 (下部)
                ActionItem( // 仍然使用原有的 ActionItemStyled helper
                    icon = Icons.Default.Edit,
                    text = "设置备注",
                    textColor = DY_White,
                    iconPositionRight = true, // 图标在右侧
                    onClick = {
                        onDismiss()
                        onOptionSelected(UserAction.REMARK_EDIT)
                    },
                    // 【核心修改】使用 0.9 的权重，使其稍微矮一点 (占 45% 的高度)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // 间隔

            // --- 2. 取消关注 (独立的圆角矩形，图标在右侧) ---
            ActionItem(
                icon = Icons.Default.Clear,
                text = "取消关注",
                textColor = DY_PrimaryRed,
                onClick = {
                    viewModel.onFollowToggle(user.id)
                    onDismiss()
                },
                iconPositionRight = true, // 图标在右侧
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.4f))
            )
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
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPositionRight: Boolean = false // 控制图标位置
) {
    Row(
        // 修正：将 clickable 放在 Row 上，并添加 padding
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (iconPositionRight) Arrangement.SpaceBetween else Arrangement.Start
    ) {
        if (!iconPositionRight) {
            // 图标在左侧
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (iconPositionRight) {
            // 图标在右侧
            Spacer(modifier = Modifier.width(16.dp)) // 保持 text-icon 间距
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SpecialFollowItem(
    user: User,
    viewModel: FollowingViewModel,
    scope: CoroutineScope,
) {

    // 1. 构建一个本地状态，用于控制 Switch 的视觉选中状态
    val initialChecked = user.isSpecialFollow
    val (isSwitchChecked, setIsSwitchChecked) = remember { mutableStateOf(initialChecked) }

    // 2. 使用 LaunchedEffect 同步全局状态：当 ViewModel 更新时，强制本地状态与全局状态一致。
    //    这确保了如果 ViewModel 状态（user.isSpecialFollow）改变，本地 Switch 也会更新。
    LaunchedEffect(user.isSpecialFollow) {
        // 当 ViewModel 中的状态 (user.isSpecialFollow) 变化时，更新本地状态
        setIsSwitchChecked(user.isSpecialFollow)
    }
    // 外部 Row 不可点击
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 确保内容与容器边缘有内边距
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 将内容推开，开关在最右侧
    ) {
        // 左侧: 标题和副标题 (Column)
        Column(modifier = Modifier.height(64.dp)) {
            // 主标题
            Text(
                text = "特别关注",
                color = DY_White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

//            Spacer(modifier = Modifier.height(4.dp))

            // 副标题 Row (包含文字和 'i' 标志)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "作品优先推荐，更新及时提示",
                    color = DY_LightGray.copy(alpha = 0.7f), // 灰色文字
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                // 圆形 'i' 标志
                Icon(
                    imageVector = Icons.Outlined.Info, // 使用 Outlined.Info 图标作为 'i'
                    contentDescription = "信息提示",
                    tint = Color.Gray, // 较深的灰色
                    modifier = Modifier.size(16.dp)
                )
            }
        }

//        Spacer(modifier = Modifier.width(8.dp))

        // 右侧: Switch 开关 (只有这里可以点击)
        Switch(
            checked = isSwitchChecked,
            onCheckedChange = { isChecked ->
                // 1. 【即时视觉反馈】立即更新本地状态，确保 Switch 视觉上保持在新的位置
                setIsSwitchChecked(isChecked)

                // 2. 【数据更新】通知 ViewModel 进行后台数据持久化
                scope.launch {
                    viewModel.onToggleSpecialFollow(user.id, isChecked)
                }
            },
            // 自定义颜色以符合 UI 风格
            colors = SwitchDefaults.colors(
                checkedThumbColor = DY_White,
                checkedTrackColor = DY_PrimaryRed, // 开启状态使用红色
                uncheckedThumbColor = DY_MediumGray, // 关闭状态使用深灰色
                uncheckedTrackColor = DY_LightGray.copy(alpha = 0.4f)
            )
        )
    }
}