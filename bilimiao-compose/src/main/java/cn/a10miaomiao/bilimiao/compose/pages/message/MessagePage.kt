package cn.a10miaomiao.bilimiao.compose.pages.message

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


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
//        MessagePageTabInfo(
//            id = 3,
//            name = "系统通知",
//        )
    )

    init {
        messageStore.clearUnread()
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagePage() {
    PageConfig(
        title = "消息通知"
    )
    val scope = rememberCoroutineScope()
    val viewModel: MessagePageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val pagerState = rememberPagerState(pageCount = { viewModel.tabs.size })
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = windowInsets.topDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                ),
            contentColor = MaterialTheme.colorScheme.onBackground,
            selectedTabIndex = pagerState.currentPage,
        ) {
            viewModel.tabs.forEachIndexed { index, tab ->
                Tab(
                    text = { Text(text = tab.name) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            when(viewModel.tabs[index].id) {
                0 -> {
                    ReplyMessagePage()
                }
                1 -> {
                    AtMessagePage()
                }
                2 -> {
                    LikeMessagePage()
                }
//                3 -> {
//                    SystemMessagePage()
//                }
            }
        }
    }
}