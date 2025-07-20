package cn.a10miaomiao.bilimiao.compose.pages.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.foundation.combinedTabDoubleClick
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomePopularContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeRecommendContent
import cn.a10miaomiao.bilimiao.compose.pages.home.content.HomeTimeMachineContent
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
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
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import java.io.IOException
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Random

@Serializable
object HomePage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: HomePageViewModel = diViewModel()
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
    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()

    private val playerDelegate by instance<BasePlayerDelegate>()

    private var lastBackPressedTime = 0L

    private val _tabs = mutableStateOf(getTabs(appStore.state.home))
    val tabs get() = _tabs.value
    var initialPage = 0
        private set
    val pageState = HomePageState(pageNavigation)
    val context: Context by instance()

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
        val sp = context.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
        val calendar = GregorianCalendar()
        val curDate = version + calendar.get(Calendar.YEAR) +
                calendar.get(Calendar.MONTH) +
                calendar.get(Calendar.DATE)
        val lastDate = sp.getString("miao_init_request_date", "")
        if (curDate == lastDate) {
            // 同一天不重复请求init接口，节省服务器资源
            val inputStream = context.openFileInput("miaoInit.json")
            val jsonStr = inputStream.reader().readText()
            return Json.decodeFromString<MiaoAdInfo>(jsonStr)
        }
        val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$version"
        val res = MiaoHttp.request(url).awaitCall().json<MiaoAdInfo>()
        val cacheJsonStr = Json.encodeToString(res)
        sp.edit().putString("miao_init_request_date", curDate).apply()
        val outputStream = context.openFileOutput("miaoInit.json", Context.MODE_PRIVATE);
        outputStream.write(cacheJsonStr.toByteArray());
        outputStream.close()
        return res
    }

    /**
     * 加载广告信息
     */
    private fun loadAdData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
            val res = getMiaoInitData(longVersionCode.toString())
            if (res.code == 0) {
                val adData = res.data.ad
                withContext(Dispatchers.Main) {
                    pageState.setAdInfo(adData)
                    saveSettingList(res.data.settingList)
                    val (autoCheckUpdate, ignoreUpdateVersionCode) = SettingPreferences.mapData(context) {
                        Pair(
                            it[IsAutoCheckVersion] ?: true,
                            it[IgnoreUpdateVersionCode] ?: 0L
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
        val dialog = MaterialAlertDialogBuilder(context).apply {
            setTitle("有新版本：" + version.versionName)
            setMessage(version.content)
            setPositiveButton("去更新", null)
            if (curVersionCode >= version.miniVersionCode) {
                setNegativeButton("取消", null)
                setNeutralButton("不再提醒此版本") { dialog, which ->
                    setIgnoreUpdateVersion(version.versionCode)
                }
            } else {
                // 小于最低版本，必须更新，对话框不能关闭
                setCancelable(false)
            }
        }.create()
        dialog.show()
        // 手动设置按钮点击事件，可阻止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(version.url)
            context.startActivity(intent)
            if (curVersionCode >= version.miniVersionCode) {
                dialog.dismiss()
            }
        }
    }

    fun setIgnoreUpdateVersion(versionCode: Long) {
        viewModelScope.launch {
            SettingPreferences.edit(context) {
                it[IgnoreUpdateVersionCode] = versionCode
            }
        }
    }

    fun saveSettingList(settingList: List<MiaoSettingInfo>) {
        try {
            val jsonStr = Json.encodeToString(settingList)
            val outputStream = context.openFileOutput("settingList.json", Context.MODE_PRIVATE);
            outputStream.write(jsonStr.toByteArray());
            outputStream.close();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 随机标题
     */
    fun randomTitle() {
//        val titles = arrayOf("时光姬", "时光基", "时光姬", "时光姬")
//        val subtitles = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
//        val random = Random()
//        ui.setState {
//            title =
//                titles[random.nextInt(titles.size)] + "  " + subtitles[random.nextInt(titles.size)]
//        }
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
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

    fun backPressed() {
        if (playerDelegate.onBackPressed()) {
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastBackPressedTime > 2000) {
            PopTip.show("再按一次退出bilimiao")
            lastBackPressedTime = now
        } else {
            fragment.requireActivity().finish()
        }
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
    BackHandler(
        onBack = viewModel::backPressed,
    )

    val scope = rememberCoroutineScope()

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())


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
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            saveableStateHolder.SaveableStateProvider(index) {
                viewModel.tabs[index].PageContent(viewModel.pageState)
            }
        }
    }
}