package cn.a10miaomiao.bilimiao.compose

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.LocalContainerView
import cn.a10miaomiao.bilimiao.compose.common.LocalContentInsets
import cn.a10miaomiao.bilimiao.compose.common.LocalEmitter
import cn.a10miaomiao.bilimiao.compose.common.LocalPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.bottomSheetContentInsets
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.toContentInsets
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.layout.ComposeScaffoldPlayerLayoutState
import cn.a10miaomiao.bilimiao.compose.components.layout.calculateComposeScaffoldLayout
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
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import com.a10miaomiao.bilimiao.comm.store.AppStore
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI

class MainActivityComposeNavigator(
    private val launchUrl: (Uri) -> Unit,
    private val onClose: () -> Unit = {},
) {
    private var navController: NavHostController? = null

    val pageNavigation = PageNavigation(
        navHostController = { navController ?: error("Compose NavHost is not ready") },
        launchUrl = launchUrl,
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

    fun navigateByUri(deepLink: Uri): Boolean {
        return pageNavigation.navigateByUri(deepLink)
    }

    fun navigate(page: ComposePage) {
        pageNavigation.navigate(page)
    }

    fun popBackStack() {
        pageNavigation.popBackStack()
    }

    fun goBackHome() {
        navController?.popBackStack(HomePage, false)
    }
}

@Composable
fun MainActivityComposeHost(
    navigator: MainActivityComposeNavigator,
    hostDi: DI,
    startViewWrapper: StartViewWrapper,
    appState: AppStore.State,
    pageConfigState: PageConfigState,
    emitter: SharedFlowEmitter,
    messageDialogState: MessageDialogState,
    bottomSheetState: BottomSheetState,
    containerView: ViewGroup?,
    bottomSheetContainerView: ViewGroup?,
    appBarBackgroundColor: androidx.compose.ui.graphics.Color,
    onBackClick: () -> Unit,
    initialDeepLink: Uri? = null,
    onInitialDeepLinkConsumed: () -> Unit = {},
    onReady: () -> Unit = {},
) {
    val navController = rememberNavController()
    val appBarState = remember { AppBarState() }
    val pageConfig = pageConfigState.collectConfigAsState().value
    val bottomSheetPage by bottomSheetState.page.collectAsState()
    val orientation = startViewWrapper.orientation
    val showPlayer = startViewWrapper.showPlayer
    val rawWindowInsets = WindowInsets.safeDrawing.toContentInsets()
    val mainContentInsets = with(LocalDensity.current) {
        calculateComposeScaffoldLayout(
            viewportWidth = with(this) { startViewWrapper.activity.resources.displayMetrics.widthPixels.toDp() },
            viewportHeight = with(this) { startViewWrapper.activity.resources.displayMetrics.heightPixels.toDp() },
            rawWindowInsets = rawWindowInsets,
            appBarState = appBarState,
            playerState = ComposeScaffoldPlayerLayoutState(
                showPlayer = showPlayer,
                fullScreenPlayer = startViewWrapper.fullScreenPlayer,
                orientation = orientation,
                smallModePlayerCurrentHeight = startViewWrapper.smallModePlayerCurrentHeight,
                smallModePlayerMinHeight = startViewWrapper.smallModePlayerMinHeight,
                playerSmallShowAreaWidth = startViewWrapper.playerSmallShowAreaWidth,
                playerSmallShowAreaHeight = startViewWrapper.playerSmallShowAreaHeight,
                playerVideoRatio = startViewWrapper.playerVideoRatio,
            ),
        ).contentInsets
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
    LaunchedEffect(pageConfig, appBarBackgroundColor, orientation) {
        val menus = pageConfig.menu?.items?.map { item ->
            cn.a10miaomiao.bilimiao.compose.components.appbar.MenuItemData.fromPropInfo(item, startViewWrapper.activity)
        } ?: emptyList()
        appBarState.title = pageConfig.title
        appBarState.menus = menus
        appBarState.canBack = pageConfig.menu?.checkable != true
        appBarState.isNavigationMenu = pageConfig.menu?.checkable == true
        appBarState.checkedKey = pageConfig.menu?.takeIf { it.checkable }?.checkedKey
        appBarState.backgroundColor = appBarBackgroundColor
        appBarState.orientation = if (orientation == 2) {
            cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation.Horizontal
        } else {
            cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarOrientation.Vertical
        }
        appBarState.syncExpandedMenusWith(menus)
        appBarState.showBar()
        appBarState.showMenu()
    }
    LaunchedEffect(onBackClick) {
        appBarState.setOnBackClickListener(onBackClick)
        appBarState.setOnMenuClickListener {
            startViewWrapper.openDrawer()
        }
        appBarState.setOnMenuItemClickListener {
            pageConfigState.onMenuItemClick(startViewWrapper.activity.window.decorView, it.toPropInfo())
        }
    }

    CompositionLocalProvider(
        LocalContainerView provides containerView,
        LocalPageConfigState provides pageConfigState,
        LocalPageNavigation provides navigator.pageNavigation,
        LocalEmitter provides emitter,
        LocalUriHandler provides navigator.uriHandler,
        LocalAppBarState provides appBarState,
    ) {
        withDI(di = hostDi) {
            BilimiaoTheme(appState = appState) {
                ImagePreviewerProvider(
                    contentPadding = mainContentInsets.toPaddingValues(),
                    previewer = { state, innerPadding ->
                        MyImagePreviewer(state, innerPadding)
                    }
                ) {
                    ComposeScaffold(
                        startViewWrapper = startViewWrapper,
                        appBarState = appBarState,
                        drawerContent = {
                            StartViewContent(
                                startTopHeight = startViewWrapper.touchStart.dp,
                                navigateTo = startViewWrapper.navigateTo,
                                navigateUrl = startViewWrapper.navigateUrl,
                                openSearch = {
                                    startViewWrapper.openSearchDialog("", 0, true)
                                },
                                openScanner = startViewWrapper.openScanner,
                                isSearchVisible = startViewWrapper.showSearchDialog,
                                searchInitKeyword = startViewWrapper.searchInitKeyword,
                                searchInitMode = startViewWrapper.searchInitMode,
                                pageSearchMethod = startViewWrapper.pageSearchMethod,
                                onCloseSearch = startViewWrapper::closeSearchDialog,
                            )
                        }
                    ) {
                        MyNavHost(navController, HomePage)
                    }
                    if (bottomSheetPage != null) {
                        MyBottomSheet(
                            container = bottomSheetContainerView,
                            page = bottomSheetPage!!,
                            onClose = bottomSheetState::close,
                        )
                    }
                }
                MessageDialog(messageDialogState)
            }
        }
    }

    BackHandler(startViewWrapper.isDrawerOpen()) {
        startViewWrapper.closeDrawer()
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

@SuppressLint("RestrictedApi")
@Composable
fun MyBottomSheet(
    container: ViewGroup?,
    page: ComposePage,
    onClose: () -> Unit,
) {
    val parentPageNavigation by rememberInstance<PageNavigation>()
    val bottomSheetNav = rememberNavController()
    val pageNavigation = remember {
        PageNavigation(
            navHostController = { bottomSheetNav },
            launchUrl = parentPageNavigation::launchWebBrowser,
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
        }
    ) {
        CompositionLocalProvider(
            LocalContainerView provides container,
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
