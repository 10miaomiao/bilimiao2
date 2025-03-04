package cn.a10miaomiao.bilimiao.compose.pages.community.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import cn.a10miaomiao.bilimiao.compose.pages.community.components.ReplyEditDialog
import com.a10miaomiao.bilimiao.comm.store.UserStore
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReplyListContent(
    viewModel: MainReplyViewModel,
    innerPadding: PaddingValues,
    listState: LazyListState = rememberLazyListState(),
    headerContent: LazyListScope.() -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val userStore by rememberInstance<UserStore>()
    val userState by userStore.stateFlow.collectAsState()
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val upMid = viewModel.upMid

    SwipeToRefresh(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshList() },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                )
                .fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            headerContent()

            items(
                list.size,
                { list[it].id }
            ) {
                val replyItem = list[it]
                val replyMid = replyItem.mid
                ReplyItemBox(
                    modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                rememberSharedContentState("reply-${replyItem.id}"),
                                animatedVisibilityScope,
                            )
                        }
                    } else {
                        Modifier
                    }.fillMaxWidth(),
                    item = replyItem,
                    isUpper = replyMid == upMid,
                    showDelete = userState.isSelf(replyMid),
                    onLikeClick = {
                        viewModel.likeReplyAt(it)
                    },
                    onAvatarClick = {
                        viewModel.toUserPage(replyItem.mid.toString())
                    },
                    onDeleteClick = {
                        viewModel.deleteReply(replyItem)
                    },
                    onClick = {
                        viewModel.setCurrentReply(replyItem)
                    }
                )
            }
            item() {
                ListStateBox(
                    loading = listLoading,
                    finished = listFinished,
                    fail = listFail,
                    listData = list,
                ) {
                    viewModel.loadMore()
                }
            }
        }
    }

    ReplyEditDialog(
        state = viewModel.editDialogState,
    )
}