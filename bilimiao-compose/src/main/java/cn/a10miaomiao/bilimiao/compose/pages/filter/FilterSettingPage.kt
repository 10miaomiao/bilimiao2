package cn.a10miaomiao.bilimiao.compose.pages.filter

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterTagListContent
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterUpperListContent
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterWordListContent
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class FilterSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: FilterSettingPageViewModel = diViewModel()
        FilterSettingPageContent(viewModel)
    }

}

private class FilterSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FilterSettingPageContent(
    viewModel: FilterSettingPageViewModel
) {
    PageConfig(
        title = "屏蔽设置"
    )
    val scope = rememberCoroutineScope()

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val pagerState = rememberPagerState(pageCount = { 3 })
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
            Tab(
                text = {
                    Text(
                        text = "屏蔽关键字",
                        color = if (0 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
            )

            Tab(
                text = {
                    Text(
                        text = "屏蔽UP主",
                        color = if (1 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
            )

            Tab(
                text = {
                    Text(
                        text = "屏蔽标签",
                        color = if (2 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 2,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
            )
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                )
                .weight(1f),
            state = pagerState,
        ) { index ->
            when(index) {
                0 -> {
                    FilterWordListContent()
                }
                1 -> {
                    FilterUpperListContent()
                }
                2 -> {
                    FilterTagListContent()
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(windowStore.bottomAppBarHeightDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text =  "可进【设置】->【帮助】查看使用方法")
        }
        Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp))
    }
}