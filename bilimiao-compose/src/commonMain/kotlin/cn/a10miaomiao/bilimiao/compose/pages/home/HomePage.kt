package cn.a10miaomiao.bilimiao.compose.pages.home

import cn.a10miaomiao.bilimiao.compose.common.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.foundation.combinedTabDoubleClick
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfo
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorage
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.pager.DrawerAwareHorizontalPager
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomePopularContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeRecommendContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeTimeMachineContent
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoSettingInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.store.AppStore.HomeSettingState
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.Calendar
import java.util.GregorianCalendar

@Serializable
object HomePage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: HomePageViewModel = diViewModel { HomePageViewModel(it) }
        HomePageContent(viewModel)
    }

}

@Stable
private sealed class HomePageTab(
    val id: String,
    val name: String,
) {

    @Composable
    abstract fun PageContent(pageState: HomePageState)

    data object TimeMachine: HomePageTab(
        id = PageTabIds.HomeTimeMachine,
        name = "时光姬",
    ) {
        @Composable
        override fun PageContent(pageState: HomePageState) {
            HomeTimeMachineContent(pageState)
        }
    }

    data object Recommend : HomePageTab(
        id = PageTabIds.HomeRecommend,
        name = "推荐"
    ) {
        @Composable
        override fun PageContent(pageState: HomePageState) {
            HomeRecommendContent()
        }
    }

    data object Popular : HomePageTab(
        id = PageTabIds.HomePopular,
        name = "热门"
    ) {
        @Composable
        override fun PageContent(pageState: HomePageState) {
            HomePopularContent()
        }
    }

}

private class HomePageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val appStore by instance<AppStore>()
    private val appInfo by instance<AppInfo>()
    private val fileStorage by instance<FileStorage>()
    private val pageNavigation by instance<PageNavigation>()

    private val _tabs = mutableStateOf(getTabs(appStore.state.home))
    val tabs get() = _tabs.value
    var initialPage = 0
        private set
    val pageState = HomePageState(pageNavigation)

    val updateDialogState = UpdateDialogState()

    init {
        loadAdData()
        viewModelScope.launch {
            appStore.stateFlow.map {
                it.home
            }.collect {
                _tabs.value = getTabs(it)
            }
        }
    }

    private fun getTabs(setting: HomeSettingState): List<HomePageTab> {
        val entryView = setting.entryView
        val tabs = mutableListOf<HomePageTab>(
            HomePageTab.TimeMachine,
        )
        if (setting.showRecommend) {
            tabs.add(HomePageTab.Recommend)
            if (entryView == SettingConstants.HOME_ENTRY_VIEW_RECOMMEND) {
                initialPage = tabs.size - 1
            }
        }
        if (setting.showPopular) {
            tabs.add(HomePageTab.Popular)
            if (entryView == SettingConstants.HOME_ENTRY_VIEW_POPULAR) {
                initialPage = tabs.size - 1
            }
        }
        return tabs
    }

    private suspend fun getMiaoInitData(version: String): MiaoAdInfo {
        val calendar = GregorianCalendar()
        val curDate = version + calendar.get(Calendar.YEAR) +
                calendar.get(Calendar.MONTH) +
                calendar.get(Calendar.DATE)
        val lastDate = fileStorage.readText("miao_init_request_date")
        if (curDate == lastDate) {
            val jsonStr = fileStorage.readText("miaoInit.json")
            if (jsonStr != null) {
                return Json.decodeFromString<MiaoAdInfo>(jsonStr)
            }
        }
        val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$version&aid=${appInfo.appId}"
        val res = MiaoHttp.request(url).awaitCall().json<MiaoAdInfo>()
        val cacheJsonStr = Json.encodeToString(res)
        fileStorage.writeText("miao_init_request_date", curDate)
        fileStorage.writeText("miaoInit.json", cacheJsonStr)
        return res
    }

    private fun loadAdData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val longVersionCode = appInfo.versionCode
            val res = getMiaoInitData(longVersionCode.toString())
            if (res.code == 0) {
                val adData = res.data.ad
                withContext(Dispatchers.Main) {
                    pageState.setAdInfo(adData)
                    saveSettingList(res.data.settingList)
                    val (autoCheckUpdate, ignoreUpdateVersionCode) = SettingPreferences.mapPreferences {
                        Pair(
                            it[SettingPreferences.IsAutoCheckVersion] ?: true,
                            it[SettingPreferences.IgnoreUpdateVersionCode] ?: 0L
                        )
                    }
                    val version = res.data.version
                    if (autoCheckUpdate
                        && version.versionCode > longVersionCode
                        && version.versionCode != ignoreUpdateVersionCode
                    ) {
                        showUpdateDialog(version, longVersionCode)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateDialog(version: MiaoAdInfo.VersionBean, curVersionCode: Long) {
        updateDialogState.show(version, curVersionCode)
    }

    fun setIgnoreUpdateVersion(versionCode: Long) {
        viewModelScope.launch {
            SettingPreferences.editPreferences {
                it[SettingPreferences.IgnoreUpdateVersionCode] = versionCode
            }
        }
    }

    fun saveSettingList(settingList: List<MiaoSettingInfo>) {
        try {
            val jsonStr = Json.encodeToString(settingList)
            fileStorage.writeText("settingList.json", jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun menuItemClick(item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.dynamic -> {
                val nav = pageNavigation.hostController
                nav.navigate(DynamicPage(), navOptions {
                    popUpTo(nav.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                })
            }
        }
    }
}

class UpdateDialogState {
    var isVisible by mutableStateOf(false)
        private set
    var version by mutableStateOf<MiaoAdInfo.VersionBean?>(null)
        private set
    var curVersionCode by mutableStateOf(0L)
        private set
    var isForceUpdate by mutableStateOf(false)
        private set

    fun show(version: MiaoAdInfo.VersionBean, curVersionCode: Long) {
        this.version = version
        this.curVersionCode = curVersionCode
        this.isForceUpdate = curVersionCode < version.miniVersionCode
        this.isVisible = true
    }

    fun dismiss() {
        isVisible = false
    }
}


@Composable
private fun HomePageContent(
    viewModel: HomePageViewModel
) {
    val pageConfigId = PageConfig(
        title = "bilimiao\n-\n首页",
        menu = rememberMyMenu {
            checkable = true
            checkedKey = MenuKeys.home
            myItem {
                key = MenuKeys.home
                title = "首页"
                iconVector = Icons.Filled.Home
            }
            myItem {
                key = MenuKeys.dynamic
                title = "动态"
                iconVector = Icons.Filled.Icecream
            }
            myItem {
                key = MenuKeys.searchInHome
                title = "搜索"
                iconVector = Icons.Filled.Search
                action = MenuActions.search
            }
        }
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
    )
    BackHandler(
        onBack = {
            // Handled by system back
        },
    )

    val scope = rememberCoroutineScope()
    val platformContext = LocalPlatformContext.current

    val windowInsets = localContentInsets()

    val pagerState = rememberPagerState(
        pageCount = { viewModel.tabs.size },
        initialPage = viewModel.initialPage
    )
    val emitter = localEmitter()
    val combinedTabClick = combinedTabDoubleClick(
        pagerState = pagerState,
        onDoubleClick = {
            scope.launch {
                emitter.emit(EmitterAction.DoubleClickTab(
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
        DrawerAwareHorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pagerState = pagerState,
            onEdgeSwipeOpen = {

            }
        ) { index ->
            saveableStateHolder.SaveableStateProvider(index) {
                viewModel.tabs[index].PageContent(viewModel.pageState)
            }
        }
    }

    // Update dialog
    val updateState = viewModel.updateDialogState
    if (updateState.isVisible && updateState.version != null) {
        val version = updateState.version!!
        AlertDialog(
            onDismissRequest = {
                if (!updateState.isForceUpdate) {
                    updateState.dismiss()
                }
            },
            title = {
                Text(text = "有新版本：" + version.versionName)
            },
            text = {
                Text(text = version.content)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        platformContext.openUrl(version.url)
                        if (!updateState.isForceUpdate) {
                            updateState.dismiss()
                        }
                    }
                ) {
                    Text(text = "去更新")
                }
            },
            dismissButton = if (updateState.isForceUpdate) null else {
                {
                    TextButton(
                        onClick = { updateState.dismiss() }
                    ) {
                        Text(text = "取消")
                    }
                }
            },
        )
    }
}
