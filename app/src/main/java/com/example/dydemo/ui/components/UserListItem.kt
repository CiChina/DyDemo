package com.example.dydemo.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dydemo.domain.model.User
import com.example.dydemo.ui.theme.DY_InputBackground
import com.example.dydemo.ui.theme.DY_LightGray
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.ui.theme.DY_White

@Composable
fun UserListItem(
    user: User,
    onItemClick: (User) -> Unit,
    onMoreOptionsClick: (User) -> Unit,
    pendingFollowActions: Map<Int, Boolean>,
    pendingRemarks: Map<Int, String?>,
    pendingSpecialFollows: Map<Int, Boolean>,
    onFollowToggle: (Int, Boolean) -> Unit,
    placeholder: Painter?
) {
    val pendingRemark = pendingRemarks[user.id]
    val displayRemark = if (pendingRemarks.containsKey(user.id)) pendingRemark else user.customRemark

    val displayName = remember(displayRemark, user.nickname) {
        if (!displayRemark.isNullOrBlank()) displayRemark else user.nickname
    }
    val hasRemark = !displayRemark.isNullOrBlank()

    val pendingState = pendingFollowActions[user.id]
    val currentIsFollowing = pendingState ?: (user.followTimestamp != null)
    val isPending = pendingState != null

    val displayButtonText = when {
        currentIsFollowing && user.isMutual && !isPending -> "互相关注"
        currentIsFollowing -> "已关注"
        else -> "关注"
    }

    val containerColor = if (currentIsFollowing) MaterialTheme.colorScheme.surfaceVariant else DY_PrimaryRed
    val contentColor = if (currentIsFollowing) MaterialTheme.colorScheme.onSurfaceVariant else DY_White  // 按钮文本颜色

    val isSpecialFollow = pendingSpecialFollows[user.id] ?: user.isSpecialFollow

    val context = LocalContext.current

    // --- Smart Image Fallback Logic ---
    var imageUrl by remember(user.avatarUrl) { mutableStateOf(user.avatarUrl) }
    var hasFailed by remember(user.avatarUrl) { mutableStateOf(false) }
    val fallbackUrl = remember(user.id) { "https://picsum.photos/id/${user.id + 10000}/200/200.webp" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(user) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "用户头像",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            error = placeholder, // Show placeholder on final failure
            onError = {
                if (!hasFailed) {
                    hasFailed = true
                    imageUrl = fallbackUrl
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    modifier = if (hasRemark) Modifier.weight(1f, fill = false) else Modifier
                )
                if (isSpecialFollow) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🔔", fontSize = 10.sp)
                }
            }
            if (hasRemark) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "名字: ${user.nickname}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }

        Button(
            onClick = { onFollowToggle(user.id, currentIsFollowing) },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
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

        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "更多操作",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = 90f }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val isFollowed = pendingState ?: (user.followTimestamp != null)
                    if (isFollowed) {
                        onMoreOptionsClick(user)
                    } else {
                        Toast.makeText(context, "已取关，无法操作", Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }
}
