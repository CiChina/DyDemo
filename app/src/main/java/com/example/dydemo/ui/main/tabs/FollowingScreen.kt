package com.example.dydemo.ui.main.tabs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.dydemo.R
import com.example.dydemo.domain.model.User
import com.example.dydemo.domain.model.UserAction
import com.example.dydemo.ui.components.RemarkEditDialog
import com.example.dydemo.ui.components.UserActionBottomSheet
import com.example.dydemo.ui.components.UserListItem
import com.example.dydemo.ui.theme.DY_MediumGray
import com.example.dydemo.ui.utils.getBitmapFromDrawable
import com.example.dydemo.viewmodel.FollowingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    viewModel: FollowingViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val lazyPagingItems: LazyPagingItems<User> = viewModel.followingUsersStream.collectAsLazyPagingItems()
    val followingCount by viewModel.followingCount.collectAsState()
    val sortingMode by viewModel.sortingMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val placeholderPainter: Painter? = remember(R.drawable.rand_avatar_01) {
        getBitmapFromDrawable(context, R.drawable.rand_avatar_01)?.let {
            BitmapPainter(it.asImageBitmap())
        }
    }

    PullToRefreshBox(
        isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading,
        onRefresh = { lazyPagingItems.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.Error -> {
                val error = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "加载失败: ${error.localizedMessage}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { lazyPagingItems.retry() }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {
                if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.NotLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无关注", color = DY_MediumGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "我的关注 (${followingCount}人)",
                                    color = DY_MediumGray
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.toggleSortingMode() }
                                ) {
                                    Text(text = sortingMode.displayName, color = DY_MediumGray)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "排序", tint = DY_MediumGray)
                                }
                            }
                        }

                        items(
                            count = lazyPagingItems.itemCount,
                            key = { index -> lazyPagingItems.peek(index)?.id ?: index }
                        ) { index ->
                            val user = lazyPagingItems[index]
                            if (user != null) {
                                UserListItem(
                                    user = user,
                                    pendingFollowActions = uiState.pendingFollowActions,
                                    pendingRemarks = uiState.pendingRemarks,
                                    pendingSpecialFollows = uiState.pendingSpecialFollows,
                                    onFollowToggle = viewModel::onFollowToggle,
                                    onItemClick = {
                                        Toast.makeText(context, "选中了 ${it.nickname}", Toast.LENGTH_SHORT).show()
                                    },
                                    onMoreOptionsClick = {
                                        selectedUser = it
                                        showActionDialog = true
                                    },
                                    placeholder = placeholderPainter
                                )
                                HorizontalDivider(
                                    color = DY_MediumGray.copy(alpha = 0.2f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 72.dp)
                                )
                            }
                        }

                        item {
                            when (lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is LoadState.Error -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Button(onClick = { lazyPagingItems.retry() }) {
                                            Text("加载更多失败，点击重试")
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.userToEditRemark != null) {
        val user = uiState.userToEditRemark!!
        RemarkEditDialog(
            userNickname = user.nickname,
            initialRemark = uiState.currentRemarkInput,
            onInputChange = viewModel::updateRemarkInput,
            onClearInput = viewModel::clearRemarkInput,
            onDismiss = viewModel::hideRemarkDialog,
            onConfirm = viewModel::saveRemark
        )
    }

    if (showActionDialog && selectedUser != null) {
        UserActionBottomSheet(
            user = selectedUser!!,
            onDismiss = { showActionDialog = false },
            viewModel = viewModel,
            pendingRemarks = uiState.pendingRemarks,
            pendingSpecialFollows = uiState.pendingSpecialFollows,
            onOptionSelected = { action ->
                if (action != UserAction.REMARK_EDIT) {
                    lazyPagingItems.refresh()
                } else {
                    viewModel.showRemarkDialog(selectedUser!!)
                }
            },
            placeholder = placeholderPainter
        )
    }
}
