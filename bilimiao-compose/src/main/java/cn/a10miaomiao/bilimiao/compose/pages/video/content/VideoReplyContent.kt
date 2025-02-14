package cn.a10miaomiao.bilimiao.compose.pages.video.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VideoReplyContent(
    viewModel: MainReplyViewModel,
    listState: LazyListState,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val upMid = viewModel.upMid

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
            ),
        state = listState,
        contentPadding = innerPadding,
    ) {
        items(
            list.size,
            { list[it].id }
        ) {
            val replyItem = list[it]
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
                },
                item = replyItem,
                upMid = upMid,
                onAvatarClick = {
                    viewModel.toUserPage(replyItem.mid.toString())
                },
                onLikeClick = {
                    viewModel.switchLike(it)
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