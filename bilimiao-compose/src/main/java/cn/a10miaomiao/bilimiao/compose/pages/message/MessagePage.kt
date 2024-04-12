package cn.a10miaomiao.bilimiao.compose.pages.message

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import org.kodein.di.compose.rememberInstance
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

internal data class MessagePageTabInfo(
    val id: Int,
    val name: String,
)

internal class MessagePageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val messageStore by instance<MessageStore>()

    val tabs = listOf(
        MessagePageTabInfo(
            id = 0,
            name = "回复我的",
        ),
        MessagePageTabInfo(
            id = 1,
            name = "@我的",
        ),
        MessagePageTabInfo(
            id = 2,
            name = "收到的赞",
        ),
    )

    init {
//        messageStore.clearUnread()
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun MessagePageContent(
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
                        Row() {
                            Text(
                                text = tab.name,
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                            val unreadCount: Int = messageState.unread?.let {
                                when (index) {
                                    0 -> it.reply
                                    1 -> it.at
                                    2 -> it.like
                                    else -> 0
                                }
                            } ?: 0
                            if (unreadCount > 0) {
                                Badge(
                                    modifier = Modifier.padding(
                                        start = 5.dp
                                    )
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
            val id = viewModel.tabs[index].id
            saveableStateHolder.SaveableStateProvider(id) {
                when(id) {
                    0 -> {
                        ReplyMessageContent()
                    }
                    1 -> {
                        AtMessageContent()
                    }
                    2 -> {
                        LikeMessageContent()
                    }
//                3 -> {
//                    SystemMessagePage()
//                }
                }
            }

        }
    }
}