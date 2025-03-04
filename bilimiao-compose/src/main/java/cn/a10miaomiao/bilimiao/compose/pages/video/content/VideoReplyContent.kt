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
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import cn.a10miaomiao.bilimiao.compose.pages.community.components.ReplyEditDialog
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyListContent
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu

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
    val sortOrder by viewModel.sortOrder.collectAsState()
    val upMid = viewModel.upMid

    if (usePageConfig && isActive) {
        val configId = PageConfig(
            title = "AV${arcData.aid}\n/\n${detailData.bvid}",
            menu = rememberMyMenu(sortOrder) {
                myItem {
                    key = MenuKeys.send
                    iconFileName = "ic_baseline_send_24"
                    title = "发布评论"
                }
                val sortOrderList = viewModel.sortOrderList
                myItem {
                    key = MenuKeys.sort
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    title = sortOrderList
                        .find { it.first == sortOrder }
                        ?.second ?: "排序"
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = sortOrder
                        sortOrderList.forEach {
                            myItem {
                                key = it.first
                                title = it.second
                            }
                        }
                    }
                }
            }
        )
        PageListener(
            configId = configId,
            onMenuItemClick = viewModel::menuItemClick,
        )
    }

    ReplyListContent(
        viewModel = viewModel,
        innerPadding = innerPadding,
        listState = listState,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )
    
}