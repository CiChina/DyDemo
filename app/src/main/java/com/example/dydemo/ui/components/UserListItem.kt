package com.example.dydemo.ui.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.domain.model.User
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.ui.theme.DY_White
//import com.example.dydemo.ui.theme.MaterialTheme.colorScheme.surface
//import com.example.dydemo.ui.theme.MaterialTheme.colorScheme.onSurface
//import com.example.dydemo.ui.theme.MaterialTheme.colorScheme.onSurfaceVariant
//import com.example.dydemo.ui.theme.MaterialTheme.colorScheme.onSurface
import com.example.dydemo.ui.utils.getBitmapFromDrawable

/**
 * 图片优化。 25-11-25
 * 确保头像图片加载使用现代库（如 Coil 或 Glide）的 AsyncImage，
 * 并配置 size() 或 Modifier.size()，避免加载过大的原始图片，以最小化内存消耗和提高加载速度。
 */

@SuppressLint("LocalContextResourcesRead")
@Composable
fun UserListItem(
    user: User,
    // 交互回调函数
    onFollowButtonClick: (User) -> Unit, // 点击“已关注”按钮
    onItemClick: (User) -> Unit,         // 点击整个 Item，弹出toast
    onMoreOptionsClick: (User) -> Unit, // 点击“···”按钮的回调，弹出下拉
    //  pendingActions Map 参数
    pendingFollowActions: Map<Int, Boolean>,
    onFollowToggle: (Int) -> Unit
) {
    // 确定要显示的用户名
    val displayName = remember(user.customRemark, user.nickname) {
        // 如果 customRemark 不为空且非空白，则显示 customRemark，否则显示 nickname
        if (!user.customRemark.isNullOrBlank()) {
            user.customRemark!!
        } else {
            user.nickname
        }
    }
    //  确定是否存在备注名（用于显示原名）
    val hasRemark = !user.customRemark.isNullOrBlank()

    // 获取 UI 应该显示的最终关注状态 (列表隐含状态为 true)
    val dbIsFollowing = true
    val pendingState = pendingFollowActions[user.id]
    // currentIsFollowing = false (待定取关) 或 true (已关注/待定关注)
    val currentIsFollowing = pendingState ?: dbIsFollowing

    // 判断是否处于待定状态 (用于设置透明度)
    val isPending = pendingState != null

    // 3. 决定按钮的样式
    // 如果 currentIsFollowing 为 true (已关注或待定关注)，显示“已关注”
    val dbIsMutual = user.isMutual
    // 如果 currentIsFollowing 为 false (待定取关)，显示“关注”
    val displayButtonText = if (currentIsFollowing) {
        // 如果处于 '已关注' 状态 (稳定或待定关注)
        if (dbIsMutual && !isPending) {
            // 只有当用户是互关状态，且当前没有待定操作（即稳定在互关状态）时，显示“互相关注”
            "互相关注"
        } else {
            "已关注"
        }
    } else {
        // 如果处于 '关注' 状态 (待定取关或实际取关)
        "关注"
    }

    // 如果 currentIsFollowing 为 true，按钮是灰底；如果为 false (待定取关)，按钮是红底。
    val containerColor = if (currentIsFollowing) MaterialTheme.colorScheme.onSurface else DY_PrimaryRed
    val contentColor = if (currentIsFollowing) MaterialTheme.colorScheme.surface else DY_White
    // 获取 Context
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(user) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //  1. 头像
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface) // 占位背景色，作为图片加载失败时的背景
        ) {
            val avatarBitmap = remember(user.avatarResId) {
                if (user.avatarResId > 0) {
                    val resName = "rand_avatar_${user.avatarResId.toString().padStart(2, '0')}"

                    // 1. 查找资源 ID
                    val actualResId = context.resources.getIdentifier(
                        resName, "drawable", context.packageName
                    )

                    if (actualResId > 0) {

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

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 昵称和备注区域 (主体内容)
        // 将名字和原名放入 Column 中
        Column(
            modifier = Modifier
                .weight(0.8f) // 占据剩余空间
                .align(Alignment.CenterVertically)
                .padding(start = 12.dp) // 假设您在这里有一个内边距
        ) {

            //  备注名/昵称 (主要行)
            Row(verticalAlignment = Alignment.CenterVertically) {

                // 优先备注名/昵称
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    maxLines = 1,
                    // 只有当有备注时，才允许截断，否则昵称应该独占
                    modifier = if (hasRemark) Modifier.weight(1f, fill = false) else Modifier
                )

                // 互关/特别关注图标 (保持不变)
//                if (user.isMutual) {
//                    Spacer(modifier = Modifier.width(4.dp))
//                    // Icon(painter = painterResource(id = R.drawable.ic_mutual), ...) // 互关图标
//                    Text("互关", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
//                }
                if (user.isSpecialFollow) {
                    Spacer(modifier = Modifier.width(4.dp))
                    // Icon(painter = painterResource(id = R.drawable.ic_bell), ...) // 铃铛图标
                    Text("🔔", fontSize = 10.sp)
                }
            }

            //  原昵称 (次要行 - 仅当有备注时显示)
            if (hasRemark) {
                Spacer(modifier = Modifier.height(2.dp)) // 稍微隔开
                Text(
                    text = "名字: ${user.nickname}", // <-- 显示原昵称
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // 使用浅灰色，降低优先级
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        // 直接渲染 Button
        Button(
            // 点击时触发 ViewModel 的状态切换
            onClick = { onFollowToggle(user.id) },

            // 样式设置
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            // 添加一个可视的标记，指示它是待定状态
            modifier = Modifier
                .width(88.dp)
                .alpha(if (isPending) 1.0f else 1.0f)
        ) {
            Text(
                text = displayButtonText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // 4. “···”更多操作按钮
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "更多操作",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = 90f
                }
                // 将逻辑放入 clickable 中
                .clickable {
                    // 1. 检查 pendingFollowActions 中是否有当前用户的状态
                    val pendingStatus = pendingFollowActions[user.id]

                    // 2. 最终的关注状态 (isFollowed):
                    //    如果 pendingStatus 存在（非 null），则使用 pendingStatus 的值 (true/false)
                    //    如果 pendingStatus 不存在，则回退到检查 user 对象中的 followTimestamp
                    val isFollowed = pendingStatus ?: (user.followTimestamp != null)

                    if (isFollowed) {
                        // 1. 如果已关注，执行原有逻辑：打开 Bottom Sheet
                        onMoreOptionsClick(user)
                    } else {
                        // 2. 如果已取关，显示 Toast 消息
                        Toast.makeText(
                            context,
                            "已取关，无法使用",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        )
    }
}

// 独立的 "已关注" 按钮组件
@Composable
fun FollowButton(isFollowing: Boolean, onClick: () -> Unit) {
    // 已关注按钮是深色圆角，文字是灰色
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(28.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface) // 深色背景
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "已关注",
            color = DY_White,
            fontSize = 12.sp
        )
    }
}
