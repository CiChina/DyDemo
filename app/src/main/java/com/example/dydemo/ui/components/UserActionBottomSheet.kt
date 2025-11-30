package com.example.dydemo.ui.components

/**
 * 底部操作弹窗
 */

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dydemo.domain.model.User
import com.example.dydemo.domain.model.UserAction
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.viewmodel.FollowingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


// 格式化时间的函数
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
    pendingRemarks: Map<Int, String?>,  // 保存关注状态的中间存储内容
    pendingSpecialFollows: Map<Int, Boolean>, // 保存特别关注状态的中间存储内容
    onOptionSelected: (UserAction) -> Unit,
    placeholder: Painter?
) {
    val displayRemark = if (pendingRemarks.containsKey(user.id)) pendingRemarks[user.id] else user.customRemark

    val displayName = remember(displayRemark, user.nickname) {
        if (!displayRemark.isNullOrBlank()) {
            displayRemark
        } else {
            user.nickname
        }
    }
    val hasRemark = !displayRemark.isNullOrBlank()

    val subtitleText = remember(hasRemark, user.nickname, user.id, user.followTimestamp) {
        val followTimeText = user.followTimestamp?.let { timestamp ->
            "关注时间: ${formatTimestamp(timestamp)}"
        } ?: ""

        val remarkPrefix = if (hasRemark) {
            "原名: ${user.nickname} ｜"
        } else {
            ""
        }
        "${remarkPrefix}ID: ${user.id}\n$followTimeText"
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(top = 24.dp, end = 24.dp)
                .size(32.dp)
                .align(Alignment.End)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(end=16.dp, start = 16.dp, bottom = 16.dp)) {
            Spacer(Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "用户头像",
                    contentScale = ContentScale.Crop,
                    placeholder = placeholder,
                    error = placeholder,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitleText,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                ) {
                    SpecialFollowItem(
                        user = user,
                        viewModel = viewModel,
                        scope = scope,
                        pendingSpecialFollows = pendingSpecialFollows
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray.copy(alpha = 0.6f))
                )

                ActionItem(
                    icon = Icons.Default.Edit,
                    text = "设置备注",
                    textColor = MaterialTheme.colorScheme.onBackground,
                    iconPositionRight = true,
                    onClick = {
                        onDismiss()
                        onOptionSelected(UserAction.REMARK_EDIT)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActionItem(
                icon = Icons.Default.Clear,
                text = "取消关注",
                textColor = DY_PrimaryRed,
                onClick = {
                    viewModel.onFollowToggle(user.id, true)
                    onDismiss()
                },
                iconPositionRight = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPositionRight: Boolean = false
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (iconPositionRight) Arrangement.SpaceBetween else Arrangement.Start
    ) {
        if (!iconPositionRight) {
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (iconPositionRight) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SpecialFollowItem(
    user: User,
    viewModel: FollowingViewModel,
    scope: CoroutineScope,
    pendingSpecialFollows: Map<Int, Boolean>
) {
    val isSpecialFollow = pendingSpecialFollows[user.id] ?: user.isSpecialFollow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.height(64.dp)) {
            Text(
                text = "特别关注",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "作品优先推荐，更新及时提示",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "信息提示",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Switch(
            checked = isSpecialFollow,
            onCheckedChange = { isChecked ->
                scope.launch {
                    viewModel.onToggleSpecialFollow(user.id, isChecked)
                }
            },
        )
    }
}
