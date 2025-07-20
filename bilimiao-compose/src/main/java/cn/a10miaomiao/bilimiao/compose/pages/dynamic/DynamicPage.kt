package cn.a10miaomiao.bilimiao.compose.pages.dynamic

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.components.DynamicMiniUpperList
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.components.DynamicPageScaffold
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.components.DynamicUpperList
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.content.DynamicAllListContent
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.content.DynamicUpperContent
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.content.DynamicVideoListContent
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomePopularContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeRecommendContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeTimeMachineContent
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.compose.rememberInstance

@Serializable
class DynamicPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: DynamicViewModel = diViewModel()
        DynamicPageContent(viewModel)
    }
}

@Composable
private fun DynamicPageContent(
    viewModel: DynamicViewModel
) {
    val pageConfigId = PageConfig(
        title = "bilimiao\n-\n动态",
        menu = rememberMyMenu {
            checkable = true
            checkedKey = MenuKeys.dynamic

            myItem {
                key = MenuKeys.home
                title = "首页"
                iconFileName = "ic_baseline_home_24"
            }
            myItem {
                key = MenuKeys.dynamic
                title = "动态"
                iconFileName = "ic_baseline_icecream_24"
            }
            myItem {
                key = MenuKeys.searchInHome
                title = "搜索"
                iconFileName = "ic_search_gray"
                action = MenuActions.search
            }
        }
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
    )

    val scope = rememberCoroutineScope()

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val upperList by viewModel.upList.collectAsState()
    val selectedUpper by viewModel.selectedUpper.collectAsState()
    val pagerState = rememberPagerState(pageCount = { if (upperList.isNotEmpty()) 2 else 1 })

    BackHandler(
        onBack = {
            if (pagerState.currentPage == 0) {
                viewModel.toHomePage()
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(0)
                }
            }
        }
    )

    val saveableStateHolder = rememberSaveableStateHolder()
    DynamicPageScaffold(
        allContent = {
            saveableStateHolder.SaveableStateProvider(key = PageTabIds.DynamicAll) {
                DynamicAllListContent(
                    dynamicViewModel = viewModel
                )
            }
        },
        videoContent = {
            saveableStateHolder.SaveableStateProvider(key = PageTabIds.DynamicVideo) {
                DynamicVideoListContent()
            }
        },
        upperList = { maxWidth ->
            if (maxWidth > 72.dp) {
                DynamicUpperList(
                    modifier = Modifier
                        .width(maxWidth)
                        .fillMaxHeight(),
                    upperList = upperList,
                    selectedUpper = if (pagerState.currentPage == 1) {
                        selectedUpper
                    } else null,
                    onBackAll = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onSelected = {
                        viewModel.selectUpper(it)
                        if (pagerState.currentPage == 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    }
                )
            } else {
                DynamicMiniUpperList(
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight(),
                    upperList = upperList,
                    selectedUpper = selectedUpper,
                    onBackAll = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onSelected = viewModel::selectUpper,
                )
            }
        },
        upperContent = {
            selectedUpper?.let {
                saveableStateHolder.SaveableStateProvider(
                    key = PageTabIds.DynamicByUpper[it.uid.toString()]
                ) {
                    DynamicUpperContent(
                        dynamicViewModel = viewModel,
                        upper = it,
                    )
                }
            }
        },
        pagerState = pagerState,
        selectedUpper = selectedUpper,
    )
}