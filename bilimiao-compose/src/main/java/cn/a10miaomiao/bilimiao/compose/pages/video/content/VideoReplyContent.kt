package cn.a10miaomiao.bilimiao.compose.pages.video.content

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.archive.v1.Arc
import bilibili.app.view.v1.ViewReply
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VideoReplyContent(
    viewModel: MainReplyViewModel,
    listState: LazyListState,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    detailData: ViewReply,
    arcData: Arc,
    isActive: Boolean = false,
    usePageConfig: Boolean = false,
) {
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val upMid = viewModel.upMid

    if (usePageConfig && isActive) {
        PageConfig(
            title = "AV${arcData.aid}\n/\n${detailData.bvid}",
            menu = rememberMyMenu {
                myItem {
                    key = MenuKeys.send
                    iconFileName = "ic_baseline_send_24"
                    title = "发布评论"
                }
                myItem {
                    key = MenuKeys.sort
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    title = "排序"
                }
            }
        )
    }

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
                }.fillMaxWidth(),
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