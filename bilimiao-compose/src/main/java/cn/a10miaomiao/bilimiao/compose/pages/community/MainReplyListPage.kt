package cn.a10miaomiao.bilimiao.compose.pages.community

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleBox
import cn.a10miaomiao.bilimiao.compose.components.layout.DataDrivenNavigator
import cn.a10miaomiao.bilimiao.compose.components.layout.RightNavigationDrawer
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyDetailContent
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class MainReplyListPage {

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainReplyListPageContent(
    headerContent: LazyListScope.() -> Unit,
    viewModel: MainReplyViewModel,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val upMid = viewModel.upMid

    val scope = rememberCoroutineScope()
    val currentReply by viewModel.currentReply.collectAsState()

    BackHandler(
        enabled = currentReply != null
    ) {
        viewModel.clearCurrentReply()
    }

    DataDrivenNavigator(
        data = currentReply,
        dataKey = { it.id },
        dataContent = { data ->
            ReplyDetailContent(
                reply = data,
                innerPadding = windowInsets.toPaddingValues(),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onCloseClick = {
                    viewModel.clearCurrentReply()
                },
                onLikeRootClick = {
                    viewModel.switchLike(data)
                }
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                ),
            contentPadding = windowInsets.toPaddingValues()
        ) {
            headerContent()
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
}