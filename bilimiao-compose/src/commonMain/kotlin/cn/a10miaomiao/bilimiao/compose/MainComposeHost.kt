package cn.a10miaomiao.bilimiao.compose

import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_PORTRAIT
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import androidx.compose.animation.AnimatedContent
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.PlatformContext
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.LocalEmitter
import cn.a10miaomiao.bilimiao.compose.common.LocalPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.bottomSheetContentInsets
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffoldPlayerLayoutState
import cn.a10miaomiao.bilimiao.compose.common.mypage.LocalPageConfigState
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import cn.a10miaomiao.bilimiao.compose.components.appbar.LocalAppBarState
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerProvider
import cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffold
import cn.a10miaomiao.bilimiao.compose.components.start.SearchOverlay
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import com.a10miaomiao.bilimiao.comm.store.AppStore
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI

class MainComposeNavigator(
    private val launchUrl: (String) -> Unit,
    private val scannerLauncher: (callback: (result: String) -> Unit) -> Boolean = { false },
    private val onClose: () -> Unit = {},
) {
    private var navController: NavHostController? = null

    val pageNavigation = PageNavigation(
        navHostController = { navController ?: error("Compose NavHost is not ready") },
        launchUrl = { url -> launchUrl(url) },
        scannerLauncher = scannerLauncher,
        onClose = onClose,
    )

    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            if (!BilibiliNavigation.navigationTo(pageNavigation, uri)) {
                BilibiliNavigation.navigationToWeb(pageNavigation, uri)
            }
        }
    }

    internal fun attach(navController: NavHostController) {
        this.navController = navController
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
        navController?.popBackStack(HomePage, false)
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
    val navController = rememberNavController()
    val appBarState = remember { AppBarState() }
    val pageConfig = pageConfigState.collectConfigAsState().value
    val bottomSheetPage by bottomSheetState.page.collectAsState()
    val playerState = startViewState.playerState
    val orientation = if (isCompactWindow()) ORIENTATION_PORTRAIT else ORIENTATION_LANDSCAPE
    val showPlayer = playerState.showPlayer
    val allowDrawerOpenGesture = bottomSheetPage == null && !playerState.fullScreenPlayer
    val portraitPlayerLayoutState = playerState.portraitPlayerLayoutState
    val floatingPlayerLayoutState = playerState.floatingPlayerLayoutState
    val playerLayoutState = remember(
        showPlayer,
        playerState.fullScreenPlayer,
        orientation,
        portraitPlayerLayoutState,
        floatingPlayerLayoutState,
        playerState.playerVideoRatio,
        playerState.anchorBounds,
    ) {
        ComposeScaffoldPlayerLayoutState(
            showPlayer = showPlayer,
            fullScreenPlayer = playerState.fullScreenPlayer,
            orientation = orientation,
            portraitState = portraitPlayerLayoutState,
            floatingState = floatingPlayerLayoutState,
            playerVideoRatio = playerState.playerVideoRatio,
            anchorBounds = playerState.anchorBounds,
        )
    }
    LaunchedEffect(navController) {
        navigator.attach(navController)
        onReady()
    }
    LaunchedEffect(initialDeepLink, navController) {
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
        // 同步 pageSearchMethod，使搜索弹窗的"站内搜索"模式可用
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
        LocalPageNavigation provides navigator.pageNavigation,
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
                        MyNavHost(navController, HomePage)
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
                            page = bottomSheetPage!!,
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
    navController: NavHostController,
    startRoute: Any,
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
    ) {
        BilimiaoPageRoute(this)
            .initRoute()
    }
}

@Composable
fun MyBottomSheet(
    page: ComposePage,
    onClose: () -> Unit,
) {
    val parentPageNavigation by rememberInstance<PageNavigation>()
    val bottomSheetNav = rememberNavController()
    val pageNavigation = remember {
        PageNavigation(
            navHostController = { bottomSheetNav },
            launchUrl = parentPageNavigation::launchWebBrowser,
            scannerLauncher = parentPageNavigation::openScanner,
            onClose = onClose,
        )
    }
    val pageConfigState = remember {
        PageConfigState()
    }
    org.kodein.di.compose.subDI(
        diBuilder = {
            bindSingleton(
                overrides = true
            ) { pageNavigation }
            bindSingleton<cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator>(
                overrides = true
            ) { pageNavigation }
        }
    ) {
        CompositionLocalProvider(
            LocalContentInsets provides bottomSheetContentInsets(),
            LocalPageConfigState provides pageConfigState,
            LocalPageNavigation provides pageNavigation,
        ) {
            AutoSheetDialog(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 500.dp),
                content = {
                    MyNavHost(bottomSheetNav, page)
                    MyBottomSheetTitleBar(pageConfigState, onClose)
                },
                onDismiss = onClose,
                onPreDismiss = bottomSheetNav::popBackStack,
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
