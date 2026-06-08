import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.a10miaomiao.bilimiao.compose.MainComposeHost
import cn.a10miaomiao.bilimiao.compose.MainComposeNavigator
import cn.a10miaomiao.bilimiao.compose.StartViewState
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.common.auth.GeetestVerifier
import cn.a10miaomiao.bilimiao.compose.common.auth.GeetestVerifierDesktop
import cn.a10miaomiao.bilimiao.compose.common.proxy.ProxyRepository
import cn.a10miaomiao.bilimiao.compose.common.proxy.ProxyRepositoryDesktop
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfo
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfoDesktop
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorage
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorageDesktop
import cn.a10miaomiao.bilimiao.compose.common.download.DownloadManager
import cn.a10miaomiao.bilimiao.compose.common.download.DownloadManagerDesktop
import cn.a10miaomiao.bilimiao.compose.platform.DesktopPlatformContext
import cn.a10miaomiao.bilimiao.compose.store.RegionStore
import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore
import com.a10miaomiao.bilimiao.comm.platform.JvmBase64Provider
import com.a10miaomiao.bilimiao.comm.platform.JvmCookieProvider
import com.a10miaomiao.bilimiao.comm.platform.JvmDeviceInfoProvider
import com.a10miaomiao.bilimiao.comm.platform.JvmPlatformContext
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.DesktopPlayerDelegate
import cn.a10miaomiao.bilimiao.compose.components.player.DesktopPlayerContainer
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.store.DesktopSettingsProvider
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.SettingsProvider
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserLibraryStore
import com.a10miaomiao.bilimiao.comm.store.UserStateProvider
import com.a10miaomiao.bilimiao.comm.store.UserStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import java.io.File

fun main() {
    // Set VLC library path - try bundled first, then system VLC
    val bundledVlcDir = File(System.getProperty("user.dir"), "desktop-app/appResources/windows-x64/vlc")
    val systemVlcDir = File("C:/Program Files/VideoLAN/VLC")
    val vlcDir = if (bundledVlcDir.exists()) bundledVlcDir else systemVlcDir
    if (vlcDir.exists()) {
        System.setProperty("jna.library.path", vlcDir.absolutePath)
        System.setProperty("VLC_PLUGIN_PATH", File(vlcDir, "plugins").absolutePath)
    }

    // Initialize platform providers (bilimiao-comm)
    PlatformProviders.context = JvmPlatformContext()
    PlatformProviders.cookieProvider = JvmCookieProvider()
    PlatformProviders.deviceInfo = JvmDeviceInfoProvider()
    PlatformProviders.base64 = JvmBase64Provider()

    // Initialize core
    BilimiaoCommCore().onCreate()

    // Create DI container with all bindings (stores are lazy singletons)
    val di = DI.lazy {
        // Platform bindings
        bindSingleton<SettingsProvider> {
            DesktopSettingsProvider(File(PlatformProviders.context.filesDir, "settings.properties"))
        }
        bindSingleton<GeetestVerifier> { GeetestVerifierDesktop() }
        bindSingleton<ProxyRepository> { ProxyRepositoryDesktop() }
        bindSingleton<AppInfo> { AppInfoDesktop() }
        bindSingleton<FileStorage> { FileStorageDesktop() }
        bindSingleton<DownloadManager> { DownloadManagerDesktop() }
        bindSingleton<BasePlayerDelegate> { DesktopPlayerDelegate(instance(), instance()) }

        // Store bindings (lazy singletons - created on first access)
        bindSingleton { AppStore(di) }
        bindSingleton { PlayListStore(di) }
        bindSingleton { PlayerStore(di) }
        bindSingleton { UserStore(di) }
        bindSingleton<UserStateProvider> { instance<UserStore>() }
        bindSingleton { UserLibraryStore(di) }
        bindSingleton { MessageStore(di) }
        bindSingleton { TimeSettingStore(di) }
        bindSingleton { FilterStore(di) }
        bindSingleton { RegionStore(di) }
    }

    // Create player instance
    val storeHolderForPlayer = object : DIAware {
        override val di = di
        val playerDelegate: BasePlayerDelegate by instance()
    }
    val playerDelegate = storeHolderForPlayer.playerDelegate as DesktopPlayerDelegate
    val mediampPlayer = playerDelegate.createPlayer()

    // Initialize stores via DIAware holder
    val storeHolder = object : DIAware {
        override val di = di
        val appStore: AppStore by instance()
        val playerStore: PlayerStore by instance()
        val userStore: UserStore by instance()
        val userLibraryStore: UserLibraryStore by instance()
        val messageStore: MessageStore by instance()
        val timeSettingStore: TimeSettingStore by instance()
        val filterStore: FilterStore by instance()
        val regionStore: RegionStore by instance()
    }
    storeHolder.appStore.init()
    storeHolder.playerStore.init()
    storeHolder.userStore.init()
    storeHolder.userLibraryStore.init()
    storeHolder.messageStore.init()
    storeHolder.timeSettingStore.init()
    storeHolder.filterStore.init()
    storeHolder.regionStore.init()

    application {
        val windowState = rememberWindowState(
            width = 1100.dp,
            height = 700.dp,
            position = WindowPosition(Alignment.Center),
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "bilimiao",
            state = windowState,
        ) {
            val platformContext = DesktopPlatformContext()
            val startViewState = StartViewState()
            // 设置播放器显示/隐藏回调
            playerDelegate.onShowPlayerChanged = { show ->
                startViewState.playerState.setShowPlayer(show)
            }
            val pageConfigState = PageConfigState()
            val emitter = SharedFlowEmitter()
            val messageDialogState = MessageDialogState()
            val bottomSheetState = BottomSheetState()
            val composeNavigator = MainComposeNavigator(
                launchUrl = { url -> platformContext.openUrl(url) },
            )

            val hostDi = DI {
                extend(di)
                bindSingleton { messageDialogState }
                bindSingleton { emitter }
                bindSingleton { composeNavigator.pageNavigation }
                bindSingleton<cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator> {
                    composeNavigator.pageNavigation
                }
                bindSingleton { bottomSheetState }
            }

            val appState by storeHolder.appStore.stateFlow.collectAsState()

            MainComposeHost(
                navigator = composeNavigator,
                hostDi = hostDi,
                startViewState = startViewState,
                appState = appState,
                pageConfigState = pageConfigState,
                emitter = emitter,
                messageDialogState = messageDialogState,
                bottomSheetState = bottomSheetState,
                platformContext = platformContext,
                playerContent = {
                    DesktopPlayerContainer(
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                onBackClick = {
                    if (composeNavigator.canPopBackStack()) {
                        composeNavigator.popBackStack()
                    }
                },
            )
        }
    }
}
