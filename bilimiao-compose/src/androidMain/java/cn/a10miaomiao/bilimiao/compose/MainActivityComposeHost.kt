package cn.a10miaomiao.bilimiao.compose

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cn.a10miaomiao.bilimiao.compose.platform.AndroidPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.LocalSystemBarsController
import cn.a10miaomiao.bilimiao.compose.platform.SystemBarsControllerAndroid
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarState
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.store.AppStore
import org.kodein.di.DI

class MainActivityComposeNavigator(
    launchUrl: (Uri) -> Unit,
    scannerLauncher: (callback: (result: String) -> Unit) -> Boolean = { false },
    onClose: () -> Unit = {},
) {
    internal val delegate = MainComposeNavigator(
        launchUrl = { url -> launchUrl(Uri.parse(url)) },
        scannerLauncher = scannerLauncher,
        onClose = onClose,
    )

    val pageNavigation get() = delegate.pageNavigation

    fun navigateByUri(deepLink: Uri): Boolean {
        return delegate.navigateByUri(deepLink.toString())
    }

    fun canPopBackStack(): Boolean = delegate.canPopBackStack()

    fun popBackStack(): Boolean = delegate.popBackStack()

    /**
     * 覆盖层返回（BottomSheet / 全屏播放 / 搜索框 / 抽屉）
     *
     * Android 的返回入口在派发给系统分发器之前先调用它，
     * 保证覆盖层优先于页面级 BackHandler 被处理。
     */
    fun handleOverlayBack(): Boolean = delegate.handleOverlayBack()

    /**
     * 导航返回与退出（页面导航后退 → 两次返回退出）
     *
     * 由系统返回兜底回调调用：系统分发器上的页面级 BackHandler 都不消费时才走到这里。
     */
    fun handleNavigationBack(): Boolean = delegate.handleNavigationBack()

    fun goBackHome() = delegate.goBackHome()
}

@Composable
fun MainActivityComposeHost(
    navigator: MainActivityComposeNavigator,
    hostDi: DI,
    startViewState: StartViewState,
    appState: AppStore.State,
    pageConfigState: PageConfigState,
    emitter: SharedFlowEmitter,
    messageDialogState: MessageDialogState,
    bottomSheetState: BottomSheetState,
    platformContext: AndroidPlatformContext,
    playerDelegate: BasePlayerDelegate,
    playerContent: (@Composable () -> Unit)? = null,
    onBackClick: () -> Unit,
    onExitApp: () -> Unit,
    initialDeepLink: Uri? = null,
    onInitialDeepLinkConsumed: () -> Unit = {},
    onReady: () -> Unit = {},
) {
    // 注入 Android 系统栏控制器：ComposeScaffold 控制状态栏前景色，
    // 全屏播放器控制系统栏显隐（统一经同一控制器操作窗口，互不覆写）
    val activity = LocalContext.current as Activity
    val systemBarsController = remember(activity) {
        SystemBarsControllerAndroid(activity)
    }
    CompositionLocalProvider(
        LocalSystemBarsController provides systemBarsController,
    ) {
        MainComposeHost(
            navigator = navigator.delegate,
            hostDi = hostDi,
            startViewState = startViewState,
            appState = appState,
            pageConfigState = pageConfigState,
            emitter = emitter,
            messageDialogState = messageDialogState,
            bottomSheetState = bottomSheetState,
            platformContext = platformContext,
            playerDelegate = playerDelegate,
            playerContent = playerContent,
            onBackClick = onBackClick,
            onExitApp = onExitApp,
            initialDeepLink = initialDeepLink?.toString(),
            onInitialDeepLinkConsumed = onInitialDeepLinkConsumed,
            onReady = onReady,
        )
    }
}
