package cn.a10miaomiao.bilimiao.compose.pages.message

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.message.content.AtMessageContent
import cn.a10miaomiao.bilimiao.compose.pages.message.content.LikeMessageContent
import cn.a10miaomiao.bilimiao.compose.pages.message.content.ReplyMessageContent
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance

class MessagePage : ComposePage() {
    override val route: String
        get() = "message"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: MessagePageViewModel = diViewModel()
        MessagePageContent(viewModel)
    }

}

private sealed class MessagePageTab(
    val id: Int,
    val name: String,
) {
    @Composable
    abstract fun PageContent()
    data object Reply : MessagePageTab(
        id = 0,
        name = "回复我的"
    ) {
        @Composable
        override fun PageContent() {
            ReplyMessageContent()
        }
    }

    data object At : MessagePageTab(
        id = 1,
        name = "@我的"
    ) {
        @Composable
        override fun PageContent() {
            AtMessageContent()
        }
    }

    data object Like : MessagePageTab(
        id = 2,
        name = "收到的赞"
    ) {
        @Composable
        override fun PageContent() {
            LikeMessageContent()
        }
    }

}

private class MessagePageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val messageStore by instance<MessageStore>()

    val tabs = listOf<MessagePageTab>(
        MessagePageTab.Reply,
        MessagePageTab.At,
        MessagePageTab.Like,
    )

    init {
//        messageStore.clearUnread()
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MessagePageContent(
    viewModel: MessagePageViewModel
) {
    PageConfig(
        title = "消息通知"
    )
    val scope = rememberCoroutineScope()

    val messageStore: MessageStore by rememberInstance()
    val messageState = messageStore.stateFlow.collectAsState().value

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val pagerState = rememberPagerState(pageCount = { viewModel.tabs.size })
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PrimaryTabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = windowInsets.topDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                ),
            selectedTabIndex = pagerState.currentPage,
        ) {
            viewModel.tabs.forEachIndexed { index, tab ->
                Tab(
                    text = {
                        Box() {
                            val unreadCount: Int = messageState.unread?.let {
                                when (index) {
                                    0 -> it.reply
                                    1 -> it.at
                                    2 -> it.like
                                    else -> 0
                                }
                            } ?: 0
                            Text(
                                modifier = Modifier.padding(
                                    end = if (unreadCount > 0) 16.dp else 0.dp
                                ),
                                text = tab.name,
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                            if (unreadCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(unreadCount.toString())
                                }
                            }
                        }
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
        val saveableStateHolder = rememberSaveableStateHolder()
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            saveableStateHolder.SaveableStateProvider(index) {
                viewModel.tabs[index].PageContent()
            }
        }
    }
}