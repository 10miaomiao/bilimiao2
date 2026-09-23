package cn.a10miaomiao.bilimiao.compose

import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import androidx.compose.animation.AnimatedContent
import cn.a10miaomiao.bilimiao.compose.animation.pageCloseTransition
import cn.a10miaomiao.bilimiao.compose.animation.pageOpenTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.PlatformContext
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.LocalEmitter
import cn.a10miaomiao.bilimiao.compose.common.LocalPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.bottomSheetContentInsets
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.BottomBarBackStack
import cn.a10miaomiao.bilimiao.compose.common.navigation.decorateEntries
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.SheetPageNavigator
import cn.a10miaomiao.bilimiao.compose.common.navigation.rememberBottomBarBackStack
import cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffoldPlayerLayoutState
import cn.a10miaomiao.bilimiao.compose.common.mypage.LocalPageConfigState
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import cn.a10miaomiao.bilimiao.compose.components.appbar.LocalAppBarState
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerProvider
import cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffold
import cn.a10miaomiao.bilimiao.compose.components.start.SearchOverlay
import com.a10miaomiao.bilimiao.comm.store.AppStore
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.compose.withDI

class MainComposeNavigator(
    internal val launchUrl: (String) -> Unit,
    internal val scannerLauncher: (callback: (result: String) -> Unit) -> Boolean = { false },
    internal val onClose: () -> Unit = {},
    val topLevelRoutes: Set<NavKey> = setOf<NavKey>(HomePage, DynamicPage()),
    val startRoute: NavKey = HomePage,
) {
    private var bottomBar: BottomBarBackStack? = null

    /**
     * PageNavigation 实例。在 MainComposeHost 组合期间通过 attach() 设置，
     * 保证 LocalPageNavigation provides 时已就绪（避免 by lazy 在 attach 前被访问）。
     */
    private var pageNavigationImpl: PageNavigation? = null

    val pageNavigation: PageNavigation
        get() = pageNavigationImpl ?: error("PageNavigation not attached; MainComposeHost must compose first")

    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            if (!BilibiliNavigation.navigationTo(pageNavigation, uri)) {
                BilibiliNavigation.navigationToWeb(pageNavigation, uri)
            }
        }
    }

    internal fun attach(bottomBar: BottomBarBackStack, pageNavigation: PageNavigation) {
        this.bottomBar = bottomBar
        this.pageNavigationImpl = pageNavigation
    }

    fun navigateByUri(deepLink: String): Boolean {
        return pageNavigation.navigateByUri(deepLink)
    }

    fun navigate(page: ComposePage) {
        pageNavigation.navigate(page)
    }

    fun canPopBackStack(): Boolean {
        return pageNavigation.canPopBackStack()
    }

    fun popBackStack(): Boolean {
        return pageNavigation.popBackStack()
    }

    fun goBackHome() {
        val bb = bottomBar ?: return
        if (bb.topLevelRoute != startRoute) {
            bb.topLevelRoute = startRoute
        }
    }
}

@Composable
fun MainComposeHost(
    navigator: MainComposeNavigator,
    hostDi: DI,
    startViewState: StartViewState,
    appState: AppStore.State,
    pageConfigState: PageConfigState,
    emitter: SharedFlowEmitter,
    messageDialogState: MessageDialogState,
    bottomSheetState: BottomSheetState,
    platformContext: PlatformContext,
    playerContent: (@Composable () -> Unit)? = null,
    onBackClick: () -> Unit,
    initialDeepLink: String? = null,
    onInitialDeepLinkConsumed: () -> Unit = {},
    onReady: () -> Unit = {},
) {
    val bottomBar = rememberBottomBarBackStack(
        startRoute = navigator.startRoute,
        topLevelRoutes = navigator.topLevelRoutes,
    )
    val pageNavigation = remember(bottomBar) {
        PageNavigation(
            bottomBar = bottomBar,
            launchUrl = { url -> navigator.launchUrl(url) },
            scannerLauncher = navigator.scannerLauncher,
            onClose = navigator.onClose,
        ).also {
            // 组合期立即挂载：页面内容位于 ComposeScaffold 的 SubcomposeLayout 子组合中，
            // 会在同一帧的测量阶段、LaunchedEffect 执行之前组合（页面 ViewModel 也会在此时
            // 从 DI 取 PageNavigation）。若延后到 LaunchedEffect 挂载，页面 ViewModel 构造
            // 时会取到未挂载的实例并抛 "PageNavigation not attached"。
            navigator.attach(bottomBar, pageNavigation = it)
        }
    }
    LaunchedEffect(bottomBar, pageNavigation) {
        onReady()
    }
    val appBarState = remember { AppBarState() }
    val pageConfig = pageConfigState.collectConfigAsState().value
    val bottomSheetPage by bottomSheetState.page.collectAsState()
    val playerState = startViewState.playerState
    val orientation = if (isCompactWindow()) ORIENTATION_PORTRAIT else ORIENTATION_LANDSCAPE
    val showPlayer = playerState.showPlayer
    val fullScreenPlayer by playerState.fullScreenPlayer.collectAsState()
    val pictureInPicture by playerState.pictureInPicture.collectAsState()
    // 画中画窗口内不响应抽屉手势：窗口已被播放器铺满，抽屉只会被画在播放器下层而不可见
    val allowDrawerOpenGesture = bottomSheetPage == null && !fullScreenPlayer && !pictureInPicture
    val portraitPlayerLayoutState = playerState.portraitPlayerLayoutState
    val floatingPlayerLayoutState = playerState.floatingPlayerLayoutState
    val playerLayoutState = remember(
        showPlayer,
        fullScreenPlayer,
        orientation,
        portraitPlayerLayoutState,
        floatingPlayerLayoutState,
        playerState.playerVideoRatio,
        playerState.anchorBounds,
    ) {
        ComposeScaffoldPlayerLayoutState(
            showPlayer = showPlayer,
            fullScreenPlayer = fullScreenPlayer,
            orientation = orientation,
            portraitState = portraitPlayerLayoutState,
            floatingState = floatingPlayerLayoutState,
            playerVideoRatio = playerState.playerVideoRatio,
            anchorBounds = playerState.anchorBounds,
        )
    }
    LaunchedEffect(initialDeepLink, bottomBar) {
        initialDeepLink?.let {
            if (navigator.navigateByUri(it)) {
                onInitialDeepLinkConsumed()
            }
        }
    }
    LaunchedEffect(pageConfig, orientation) {
        val menus = pageConfig.menu?.items?.map { item ->
            cn.a10miaomiao.bilimiao.compose.components.appbar.MenuItemData.fromPropInfo(item)
        } ?: emptyList()
        appBarState.title = pageConfig.title
        appBarState.menus = menus
        appBarState.canBack = pageConfig.menu?.checkable != true
        appBarState.isNavigationMenu = pageConfig.menu?.checkable == true
        appBarState.checkedKey = pageConfig.menu?.takeIf { it.checkable }?.checkedKey
        appBarState.orientation = if (orientation == ORIENTATION_LANDSCAPE) {
            cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation.Horizontal
        } else {
            cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation.Vertical
        }
        appBarState.syncExpandedMenusWith(menus)
        appBarState.showBar()
        appBarState.showMenu()
        val searchConfig = pageConfig.search
        startViewState.setPageSearchMethod(
            if (searchConfig?.name.isNullOrBlank()) {
                null
            } else {
                object : cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod {
                    override val name: String
                        get() = searchConfig?.name ?: ""
                    override fun onSearch(keyword: String) {
                        pageConfigState.onSearchSelfPage(keyword)
                    }
                }
            }
        )
    }
    LaunchedEffect(onBackClick) {
        appBarState.setOnBackClickListener(onBackClick)
        appBarState.setOnMenuClickListener {
            startViewState.openDrawer()
        }
        appBarState.setOnMenuItemClickListener {
            pageConfigState.onMenuItemClick(it.toPropInfo())
        }
    }
    LaunchedEffect(startViewState, pageConfigState) {
        pageConfigState.openSearch = {
            val searchConfig = pageConfigState.currentConfig.search
            val keyword = searchConfig?.keyword ?: ""
            val mode = if (searchConfig?.name.isNullOrBlank()) 0 else 1
            startViewState.openSearchDialog(keyword, mode, false)
        }
    }

    CompositionLocalProvider(
        LocalPlatformContext provides platformContext,
        LocalPageConfigState provides pageConfigState,
        LocalPageNavigation provides pageNavigation,
        LocalEmitter provides emitter,
        LocalUriHandler provides navigator.uriHandler,
        LocalAppBarState provides appBarState,
    ) {
        withDI(di = hostDi) {
            BilimiaoTheme(appState = appState) {
                val toasterState = rememberToasterState()
                LaunchedEffect(toasterState) {
                    GlobalToaster.init(toasterState)
                }
                ImagePreviewerProvider(
                    previewer = { state, innerPadding ->
                        MyImagePreviewer(state, innerPadding)
                    }
                ) {
                    ComposeScaffold(
                        startViewState = startViewState,
                        playerContent = playerContent,
                        appBarState = appBarState,
                        allowDrawerOpenGesture = allowDrawerOpenGesture,
                        drawerContent = {
                            StartViewContent(
                                startTopHeight = startViewState.touchStart.dp,
                                openSearch = {
                                    startViewState.closeDrawer()
                                    startViewState.openSearchDialog("", 0, true)
                                },
                                closeDrawer = { startViewState.closeDrawer() },
                            )
                        }
                    ) {
                        MyNavHost(bottomBar)
                    }
                    SearchOverlay(
                        visible = startViewState.showSearchDialog,
                        searchAnimation = startViewState.searchAnimation,
                        initKeyword = startViewState.searchInitKeyword,
                        initMode = startViewState.searchInitMode,
                        pageSearchMethod = startViewState.pageSearchMethod,
                        onDismissRequest = startViewState::closeSearchDialog,
                    )
                    if (bottomSheetPage != null) {
                        MyBottomSheet(
                            bottomSheetState = bottomSheetState,
                            onClose = bottomSheetState::close,
                        )
                    }
                }
                MessageDialog(messageDialogState)
                Toaster(
                    state = toasterState,
                    alignment = Alignment.BottomCenter,
                    richColors = true,
                )
            }
        }
    }
}

@Composable
fun MyNavHost(
    bottomBar: BottomBarBackStack,
) {
    val entryProvider = entryProvider {
        BilimiaoPageRoute.entries(this)
    }
    val entries = bottomBar.decorateEntries(entryProvider)
    NavDisplay(
        entries = entries,
        onBack = { bottomBar.pop() },
        transitionSpec = {
            // 前进导航：新页面由里扩到全屏，旧页面向外扩散消失
            pageOpenTransition()
        },
        popTransitionSpec = {
            // 返回导航：上一页由外缩回全屏，当前页向内缩小消失
            pageCloseTransition()
        },
    )
}

@Composable
fun MyBottomSheet(
    bottomSheetState: BottomSheetState,
    onClose: () -> Unit,
) {
    val parentPageNavigation by rememberInstance<PageNavigation>()
    // BottomSheet 使用独立导航栈：内部页面导航（navigate/popBackStack）全部作用于
    // BottomSheetState 内部栈，与主 backstack 互不影响，返回时先处理内部页面，
    // 直至最后一个内部页面关闭后再关闭整个 BottomSheet。
    val sheetPageNavigator = remember(bottomSheetState, parentPageNavigation) {
        SheetPageNavigator(bottomSheetState, parentPageNavigation)
    }
    val page by bottomSheetState.page.collectAsState()
    val innerPages by bottomSheetState.innerPages.collectAsState()
    val currentPage = innerPages.lastOrNull() ?: page
    val pageConfigState = remember { PageConfigState() }
    subDI(
        diBuilder = {
            bindSingleton<cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator>(
                overrides = true
            ) { sheetPageNavigator }
        }
    ) {
        CompositionLocalProvider(
            LocalContentInsets provides bottomSheetContentInsets(),
            LocalPageConfigState provides pageConfigState,
            LocalPageNavigation provides sheetPageNavigator,
        ) {
            AutoSheetDialog(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 500.dp),
                content = {
                    if (currentPage != null) {
                        currentPage.Content()
                    }
                    MyBottomSheetTitleBar(pageConfigState, onClose)
                },
                onDismiss = onClose,
                onPreDismiss = {
                    // 先处理 sheet 内部页面导航，直至最后一个页面关闭 BottomSheet
                    if (bottomSheetState.hasInnerPage) {
                        bottomSheetState.popInnerPage()
                        true
                    } else {
                        false
                    }
                },
            )
        }
    }
}

@Composable
fun MyBottomSheetTitleBar(
    state: PageConfigState,
    onClose: () -> Unit,
) {
    val config = state.collectConfigAsState()
    Box(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
    ) {
        IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors()
                .copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                        .copy(alpha = 0.75f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "close"
            )
        }

        AnimatedContent(
            modifier = Modifier
                .align(Alignment.Center),
            targetState = config.value.title,
            contentKey = { it },
            label = "BottomSheetTitle",
        ) { title ->
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer
                            .copy(alpha = 0.75f)
                    )
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = title.replace("\n", " "),
            )
        }
    }
}
