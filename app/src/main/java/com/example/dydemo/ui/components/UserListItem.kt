package com.example.dydemo.ui.components

import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.R // 假设资源文件在 R 中
import com.example.dydemo.data.model.User
import com.example.dydemo.ui.theme.DY_InputBackground
import com.example.dydemo.ui.theme.DY_LightGray
import com.example.dydemo.ui.theme.DY_MediumGray
import com.example.dydemo.ui.theme.DY_White


@Composable
fun UserListItem(
    user: User,
    // 交互回调函数
    onFollowButtonClick: (User) -> Unit, // 点击“已关注”按钮
    onItemClick: (User) -> Unit,         // 点击整个 Item，弹出toast
    onMoreOptionsClick: (User) -> Unit, // 点击“···”按钮的回调，弹出下拉
    // 【修改】新增 pendingActions Map 参数
    pendingFollowActions: Map<Int, Boolean>,
    onFollowToggle: (Int) -> Unit
) {
    // 【新增逻辑】确定要显示的用户名
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

    // 1. 获取 UI 应该显示的最终关注状态 (列表隐含状态为 true)
    val dbIsFollowing = true
    val pendingState = pendingFollowActions[user.id]
    // currentIsFollowing = false (待定取关) 或 true (已关注/待定关注)
    val currentIsFollowing = pendingState ?: dbIsFollowing

    // 2. 判断是否处于待定状态 (用于设置透明度)
    val isPending = pendingState != null

    // 3. 决定按钮的样式

    // 如果 currentIsFollowing 为 true (已关注或待定关注)，显示“已关注”
    // 如果 currentIsFollowing 为 false (待定取关)，显示“关注”
    val displayButtonText = if (currentIsFollowing) "已关注" else "关注"

    // 如果 currentIsFollowing 为 true，按钮是灰底；如果为 false (待定取关)，按钮是红底。
    val containerColor = if (currentIsFollowing) Color.Gray.copy(alpha = 0.5f) else Color.Red
    val contentColor = DY_White
    // 获取 Context 以便显示 Toast
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(user) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 头像
        // 实际应用中应该使用 Coil 或 Glide 加载，这里使用本地资源占位
        // Image(painter = painterResource(id = user.avatarResId), ...)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DY_InputBackground) // 占位背景色
        ) {
            // 占位符，实际应加载图片
            Text(
                text = user.nickname.first().toString(),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 昵称和备注区域 (主体内容)
        // 【核心修改】将名字和原名放入 Column 中
        Column(
            modifier = Modifier
                .weight(1f) // 占据剩余空间
                .align(Alignment.CenterVertically)
                .padding(start = 12.dp) // 假设您在这里有一个内边距
        ) {

            // --- 备注名/昵称 (主要行) ---
            Row(verticalAlignment = Alignment.CenterVertically) {

                // 优先备注名/昵称
                Text(
                    text = displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    // 只有当有备注时，才允许截断，否则昵称应该独占
                    modifier = if (hasRemark) Modifier.weight(1f, fill = false) else Modifier
                )

                // 互关/特别关注图标 (保持不变)
                if (user.isMutual) {
                    Spacer(modifier = Modifier.width(4.dp))
                    // Icon(painter = painterResource(id = R.drawable.ic_mutual), ...) // 互关图标
                    Text("互关", color = DY_LightGray, fontSize = 10.sp)
                }
                if (user.isSpecialFollow) {
                    Spacer(modifier = Modifier.width(4.dp))
                    // Icon(painter = painterResource(id = R.drawable.ic_bell), ...) // 铃铛图标
                    Text("🔔", fontSize = 10.sp)
                }
            }

            // --- 原昵称 (次要行 - 仅当有备注时显示) ---
            if (hasRemark) {
                Spacer(modifier = Modifier.height(2.dp)) // 稍微隔开
                Text(
                    text = "名字: ${user.nickname}", // <-- 显示原昵称
                    color = DY_LightGray.copy(alpha = 0.7f), // 使用浅灰色，降低优先级
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        // 【核心修改点】直接渲染 Button
        Button(
            // 【核心逻辑】点击时触发 ViewModel 的状态切换
            onClick = { onFollowToggle(user.id) },

            // 样式设置
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            // 添加一个可视的标记，指示它是待定状态 (例如：轻微透明度)
            modifier = Modifier.alpha(if (isPending) 0.7f else 1.0f)
        ) {
            Text(
                text = displayButtonText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 4. “···”更多操作按钮
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "更多操作",
            tint = DY_MediumGray,
            modifier = Modifier
                .size(24.dp)
                // 【核心修改】将逻辑放入 clickable 中
                .clickable {
                    // 1. 检查 pendingFollowActions 中是否有当前用户的状态
                    val pendingStatus = pendingFollowActions[user.id]

                    // 2. 最终的关注状态 (isFollowed):
                    //    - 如果 pendingStatus 存在（非 null），则使用 pendingStatus 的值 (true/false)
                    //    - 如果 pendingStatus 不存在，则回退到检查 user 对象中的 followTimestamp
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
    // 抖音风格的已关注按钮是深色圆角，文字是灰色
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(28.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(DY_InputBackground) // 深色背景
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "已关注",
            color = DY_LightGray,
            fontSize = 12.sp
        )
    }
}

// -----------------------------------------------------------
// 注意：为了运行，您需要在您的项目中添加必要的资源文件，例如 R.drawable.ic_mutual 等。
// 如果项目缺少 R.drawable，您可能需要将 avatarResId 替换为 URL 或其他占位符。
// -----------------------------------------------------------