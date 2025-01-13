package cn.a10miaomiao.bilimiao.compose.pages.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.foundation.combinedTabDoubleClick
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.mine.content.TypeBangumiContent
import cn.a10miaomiao.bilimiao.compose.pages.search.content.SearchAllContent
import cn.a10miaomiao.bilimiao.compose.pages.search.content.SearchByTypeContent
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class MyBangumiPage() : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SearchPageViewModel = diViewModel()
        MyBangumiPageContent(viewModel)
    }
}

@Stable
private sealed class MyBangumiPageTab(
    val id: String,
    val name: String,
) {

    @Composable
    abstract fun PageContent(isActive: Boolean)

    data object Bangumi: MyBangumiPageTab(
        id = PageTabIds.MyBangumi["bangumi"],
        name = "我的追番",
    ) {
        @Composable
        override fun PageContent(isActive: Boolean) {
            TypeBangumiContent("bangumi", isActive)
        }
    }

    data object Cinema : MyBangumiPageTab(
        id = PageTabIds.MyBangumi["cinema"],
        name = "我的追剧"
    ) {
        @Composable
        override fun PageContent(isActive: Boolean) {
            TypeBangumiContent("cinema", isActive)
        }
    }


}

private class SearchPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    val tabs = listOf<MyBangumiPageTab>(
        MyBangumiPageTab.Bangumi,
        MyBangumiPageTab.Cinema,
    )

}


@Composable
private fun MyBangumiPageContent(
    viewModel: SearchPageViewModel,
) {
    PageConfig(
        title = "搜索"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { viewModel.tabs.size })
    val emitter = localEmitter()
    val combinedTabClick = combinedTabDoubleClick(
        pagerState = pagerState,
        onDoubleClick = {
            scope.launch {
                emitter.emit(
                    EmitterAction.DoubleClickTab(
                        tab = viewModel.tabs[it].id
                    ))
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(windowInsets.toPaddingValues(bottom = 0.dp)),
            selectedTabIndex = pagerState.currentPage,
            indicator = { positions ->
                TabRowDefaults.PrimaryIndicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, positions),
                )
            },
        ) {
            viewModel.tabs.forEachIndexed { index, tab ->
                Tab(
                    text = {
                        Text(
                            text = tab.name,
                            color = if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        )
                    },
                    selected = pagerState.currentPage == index,
                    onClick = { combinedTabClick(index) },
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
                val isActive = index == pagerState.currentPage
                viewModel.tabs[index].PageContent(isActive)
            }
        }
    }
}