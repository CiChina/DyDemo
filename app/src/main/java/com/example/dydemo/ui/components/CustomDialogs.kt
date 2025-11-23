package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dydemo.ui.theme.DY_DarkBackground
import com.example.dydemo.ui.theme.DY_InputBackground
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.ui.theme.DY_White

/**
 * 设置备注的对话框
 * @param initialRemark 初始备注内容
 * @param onDismissRequest 关闭回调
 * @param onConfirm 确认回调，返回新的备注内容
 */
@Composable
fun SetRemarkDialog(
    initialRemark: String?,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue(initialRemark ?: "")) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Text("设置备注",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.background)
            Spacer(modifier = Modifier.height(16.dp))

            // 文本输入框
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("备注名称") },
                colors = OutlinedTextFieldDefaults.colors(
                    // 聚焦和未聚焦的文本颜色
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    // 光标和聚焦边框
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,

                    // 未聚焦的边框和标签
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onConfirm(textState.text.trim())
                        onDismissRequest()
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}