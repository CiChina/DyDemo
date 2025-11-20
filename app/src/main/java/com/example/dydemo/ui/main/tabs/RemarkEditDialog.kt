package com.example.dydemo.ui.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 假设这些是您的颜色常量
val DY_DialogBackground = Color(0xFF2B2B2B) // 圆角弹窗背景
val DY_InputBackground = Color(0xFF404040) // 浅灰色输入框背景
val DY_ClearButton = Color(0xFF707070)     // 深灰色 X 图标背景
val DY_White = Color(0xFFFFFFFF)
val DY_MediumGray = Color(0xFF9E9E9E) // 取消按钮颜色


@Composable
fun RemarkEditDialog(
    userNickname: String,
    initialRemark: String,
    onInputChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val textState = remember { mutableStateOf(initialRemark) }

    // 每次 initialRemark 变化时更新内部状态
    LaunchedEffect(initialRemark) {
        textState.value = initialRemark
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DY_DialogBackground),
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "设置备注名",
                    color = DY_White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 带有 X 图标的输入框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DY_InputBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 文本输入框
                    BasicTextField(
                        value = textState.value,
                        onValueChange = {
                            textState.value = it
                            onInputChange(it) // 将更改传递给 ViewModel
                        },
                        textStyle = TextStyle(color = DY_White, fontSize = 16.sp),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        // 提示文本
                        decorationBox = { innerTextField ->
                            if (textState.value.isEmpty()) {
                                Text("设置备注名给 $userNickname", color = DY_MediumGray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )

                    // X 清除图标
                    if (textState.value.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "清除文本",
                            tint = DY_White,
                            modifier = Modifier
                                .size(24.dp)
                                .background(DY_ClearButton, CircleShape)
                                .clickable {
                                    textState.value = "" // 清除本地状态
                                    onClearInput()     // 通知 ViewModel 清除
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 取消和确定按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 取消按钮
                    Text(
                        text = "取消",
                        color = DY_MediumGray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp)
                    )
                    // 确定按钮
                    Text(
                        text = "确定",
                        color = DY_White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onConfirm)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}