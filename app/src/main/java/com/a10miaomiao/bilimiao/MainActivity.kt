package com.a10miaomiao.bilimiao

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.DisplayCutout
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cn.a10miaomiao.bilimiao.compose.MainActivityComposeHost
import cn.a10miaomiao.bilimiao.compose.MainActivityComposeNavigator
import cn.a10miaomiao.bilimiao.compose.ORIENTATION_LANDSCAPE
import cn.a10miaomiao.bilimiao.compose.StartViewState
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.ComposeHostBridge
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.components.player.BiliVideoScaffold
import com.a10miaomiao.bilimiao.comm.BiliGeetestUtilImpl
import com.a10miaomiao.bilimiao.comm.BilimiaoStatService
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.a10miaomiao.bilimiao.comm.scanner.BilimiaoScanner
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import cn.a10miaomiao.bilimiao.compose.common.auth.GeetestVerifier
import cn.a10miaomiao.bilimiao.compose.common.auth.GeetestVerifierAndroid
import cn.a10miaomiao.bilimiao.compose.common.proxy.ProxyRepository
import cn.a10miaomiao.bilimiao.compose.common.proxy.ProxyRepositoryAndroid
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfo
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfoAndroid
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorage
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorageAndroid
import cn.a10miaomiao.bilimiao.compose.common.download.DownloadManager
import cn.a10miaomiao.bilimiao.compose.common.download.DownloadManagerAndroid
import cn.a10miaomiao.bilimiao.compose.platform.AndroidPlatformContext as ComposePlatformContext
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlaybackService
import com.a10miaomiao.bilimiao.store.Store
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.materialkolor.hct.Hct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster

class MainActivity : AppCompatActivity(), DIAware {

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        store.loadStoreModules(this)
        bindSingleton { startViewState }
        bindSingleton<BasePlayerDelegate> { basePlayerDelegate }
        bindSingleton { themeDelegate }
        bindSingleton { statusBarHelper }
        bindSingleton { supportHelper }
        bindSingleton { biliGeetestUtil }
        bindSingleton<GeetestVerifier> { GeetestVerifierAndroid(biliGeetestUtil) }
        bindSingleton<ProxyRepository> { ProxyRepositoryAndroid(this@MainActivity) }
        bindSingleton<AppInfo> { AppInfoAndroid(this@MainActivity) }
        bindSingleton<FileStorage> { FileStorageAndroid(this@MainActivity) }
        bindSingleton<DownloadManager> { DownloadManagerAndroid(this@MainActivity) }
    }

    private val store by lazy { Store(this, di) }
    private val themeDelegate by lazy { ThemeDelegate(this, di) }
    private val statusBarHelper by lazy { StatusBarHelper(this) }
    private val supportHelper by lazy { SupportHelper(this) }
    private val biliGeetestUtil: BiliGeetestUtil by lazy { BiliGeetestUtilImpl(this, lifecycle) }

    private val messageDialogState = cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState()
    private val bottomSheetState = BottomSheetState()
    private val pageConfigState = PageConfigState()
    private val emitter = SharedFlowEmitter()
    private val composeNavigator = MainActivityComposeNavigator(
        launchUrl = ::launchWebBrowser,
        scannerLauncher = { callback ->
            BilimiaoScanner.openScanner(
                this,
                themeDelegate.themeColor.toInt(),
                callback,
            )
        },
    )
    private var appBarBackgroundColor by mutableStateOf(ComposeColor.Unspecified)
    private val composeHostBridge = object : ComposeHostBridge {
        override val context: Context
            get() = this@MainActivity

        override val activity: Activity
            get() = this@MainActivity

        override fun finishHost() {
            finish()
        }

        override fun onBackPressed() {
            this@MainActivity.handleActivityBackPressed()
        }

        override fun startActivity(intent: Intent) {
            this@MainActivity.startActivity(intent)
        }

        override fun runOnUiThread(action: () -> Unit) {
            this@MainActivity.runOnUiThread(action)
        }
    }
    private val composeHostDi by lazy {
        DI.lazy {
            extend(di)
            bindSingleton<ComposeHostBridge> { composeHostBridge }
            bindSingleton { Bundle() }
            bindSingleton { messageDialogState }
            bindSingleton { emitter }
            bindSingleton { composeNavigator.pageNavigation }
            bindSingleton<cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator> { composeNavigator.pageNavigation }
            bindSingleton { bottomSheetState }
        }
    }
    private var pendingDeepLink: Uri? = null
    private var lastExitBackPressedTime = 0L

    private val startViewState by lazy {
        StartViewState(
            fullScreenPlayer = basePlayerDelegate.fullscreenController.isFullscreen,
        )
    }
    private val basePlayerDelegate: PlayerDelegateImpl by lazy {
        // 初始化平台 Provider（ExoPlayerMediampPlayer 需要 AndroidPlatformContext）
        com.a10miaomiao.bilimiao.comm.platform.PlatformProviders.context = com.a10miaomiao.bilimiao.comm.platform.AndroidPlatformContext(application as android.app.Application)
        val playerStore: com.a10miaomiao.bilimiao.comm.store.PlayerStore by di.instance()
        val playListStore: com.a10miaomiao.bilimiao.comm.store.PlayListStore by di.instance()
        val appStore: com.a10miaomiao.bilimiao.comm.store.AppStore by di.instance()
        PlayerDelegateImpl(
            playerStore = playerStore,
            playListStore = playListStore,
            isLockScreenOrientationPortraitProvider = { appStore.state.isLockScreenOrientationPortrait },
        ).also {
            it.createPlayer()
            it.onShowPlayerChanged = { show ->
                startViewState.playerState.setShowPlayer(show)
                updateStatusBarStyle()
                findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        super.onCreate(savedInstanceState)
        // 设置 ActivityHolder 供 FullscreenController 调用 requestedOrientation
        com.a10miaomiao.bilimiao.comm.delegate.player.ActivityHolder.set(this)
        themeDelegate.onCreate(savedInstanceState)

        BilimiaoStatService.setAuthorizedState(this, false)
        BilimiaoStatService.start(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        store.onCreate(savedInstanceState)
        pendingDeepLink = intent.data
        initRootView(savedInstanceState)

        lifecycleScope.launch {
            store.appStore.stateFlow.mapNotNull { it.theme }
                .flowOn(Dispatchers.Main)
                .collect {
                    val themeColor = it.color
                    val bgColor = if (it.appBarType == 0) {
                        val hct = Hct.fromInt(themeColor)
                        val isDark = when (it.darkMode) {
                            0 -> themeDelegate.isSystemInDark()
                            1 -> false
                            else -> true
                        }
                        val tone = if (isDark) 20.0 else 90.0
                        Hct.from(hct.hue, 10.0, tone).toInt()
                    } else {
                        config.blockBackgroundColor
                    }
                    themeDelegate.setThemeColor(themeColor)
                    appBarBackgroundColor = ComposeColor(
                        (bgColor and 0x00FFFFFF) or (0xF8000000).toInt()
                    )
                }
        }
        lifecycleScope.launch {
            store.appStore.stateFlow.mapNotNull {
                it.isLockScreenOrientationPortrait
            }.flowOn(Dispatchers.Main).collect {
                if (!basePlayerDelegate.fullscreenController.isFullscreen.value) {
                    requestedOrientation = when (it) {
                        true -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        }
        // 全屏播放时系统栏由播放器接管；退出全屏时恢复状态栏前景色（由 updateStatusBarStyle 处理）
        lifecycleScope.launch {
            basePlayerDelegate.fullscreenController.isFullscreen.collect {
                updateStatusBarStyle()
            }
        }
    }

    private fun initRootView(savedInstanceState: Bundle?) {
        basePlayerDelegate.onCreate()
        startViewState.playerState.setShowPlayer(basePlayerDelegate.isPlaying())
        updateSmallModePlayerMaxHeight()

        val rootComposeView = ComposeView(this).apply {
            setContent {
                val appState = store.appStore.stateFlow.collectAsState().value
                val platformContext = remember { ComposePlatformContext(this@MainActivity) }
                MainActivityComposeHost(
                    navigator = composeNavigator,
                    hostDi = composeHostDi,
                    startViewState = startViewState,
                    appState = appState,
                    pageConfigState = pageConfigState,
                    emitter = emitter,
                    messageDialogState = messageDialogState,
                    bottomSheetState = bottomSheetState,
                    platformContext = platformContext,
                    playerContent = {
                        BiliVideoScaffold(
                            delegate = basePlayerDelegate,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { basePlayerDelegate.closePlayer() },
                            onToggleFullscreen = { basePlayerDelegate.fullscreenController.toggleFullscreen() },
                        )
                    },
                    onBackClick = ::handleActivityBackPressed,
                    initialDeepLink = pendingDeepLink,
                    onInitialDeepLinkConsumed = {
                        pendingDeepLink = null
                    },
                    onReady = {
                        pendingDeepLink?.let {
                            if (composeNavigator.navigateByUri(it)) {
                                pendingDeepLink = null
                            }
                        }
                    },
                )
            }
        }
        setContentView(rootComposeView)

        findViewById<View>(android.R.id.content).post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
                findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { _, insets ->
                    setWindowInsets(insets)
                    insets
                }
            } else {
                setWindowInsetsAndroidL()
            }
        }
        updateStatusBarStyle()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.data
        pendingDeepLink?.let {
            if (composeNavigator.navigateByUri(it)) {
                pendingDeepLink = null
            }
        }
    }



    fun openBottomSheet(page: ComposePage) {
        bottomSheetState.open(page)
    }

    fun goBackHome() {
        composeNavigator.goBackHome()
    }


    fun setWindowInsetsAndroidL() {
        val rectangle = Rect()
        val displayMetrics = DisplayMetrics()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val top = statusBarHelper.getStatusBarHeight()
        val bottom = displayMetrics.heightPixels - rectangle.bottom - rectangle.top
        val right = displayMetrics.widthPixels - rectangle.right
        setWindowInsets(0, top, right, bottom, null)
    }

    fun setWindowInsets(insets: WindowInsets) {
        val left = insets.systemWindowInsetLeft
        val top = insets.systemWindowInsetTop
        val right = insets.stableInsetRight
        val bottom = insets.systemWindowInsetBottom
        val displayCutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            insets.displayCutout
        } else {
            null
        }
        setWindowInsets(left, top, right, bottom, displayCutout)
    }

    fun setWindowInsets(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        displayCutout: DisplayCutout?
    ) {
        // PlayerDelegateImpl.setWindowInsets 为空实现（Compose VideoScaffold 自行处理 insets）
        basePlayerDelegate.setWindowInsets(left, top, right, bottom)
        updateStatusBarStyle()
    }

    private fun updateStatusBarStyle() {
        // 全屏播放时系统栏由播放器接管（状态栏前景色白色、导航栏隐藏等），此处跳过避免覆盖
        if (basePlayerDelegate.fullscreenController.isFullscreen.value) {
            return
        }
        statusBarHelper.isLightStatusBar =
            !startViewState.playerState.showPlayer ||
                (startViewState.playerState.orientation == ORIENTATION_LANDSCAPE &&
                    !basePlayerDelegate.fullscreenController.isFullscreen.value)
    }

    override fun onResume() {
        super.onResume()
        basePlayerDelegate.onResume()
        BilimiaoStatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        basePlayerDelegate.onPause()
        BilimiaoStatService.onPause(this)
    }

    override fun onDestroy() {
        basePlayerDelegate.onDestroy()
        store.onDestroy()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        basePlayerDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        basePlayerDelegate.onStop()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val showPlayer = startViewState.playerState.showPlayer
        when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> {
                if (showPlayer) {
                    if (basePlayerDelegate.isPlaying()) {
                        basePlayerDelegate.pause()
                    } else {
                        basePlayerDelegate.resume()
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (showPlayer) {
                    basePlayerDelegate.seekTo(basePlayerDelegate.currentPosition() - 5000)
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (showPlayer) {
                    basePlayerDelegate.seekTo(basePlayerDelegate.currentPosition() + 5000)
                }
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                if (showPlayer) {
                    basePlayerDelegate.onBackPressed()
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BilimiaoScanner.REQUEST_CODE -> {
                BilimiaoScanner.onActivityResult(ActivityResult(resultCode, data))
            }
        }
    }

    private fun jumpNotificationSetting() {
        val intent = Intent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", packageName)
                intent.putExtra("app_uid", applicationInfo.uid)
            } else {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (_: Exception) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }
    }

    private fun showNotificationPermissionTips() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("请求授权”通知权限“")
            setMessage("从Android13开始，需要您授予通知权限，在您向该应用授予该权限之前，该应用都将无法发送通知。\n受影响的功能：通知栏播放器控制器、下载进度通知")
            setCancelable(false)
            setPositiveButton("去授权") { _, _ ->
                jumpNotificationSetting()
            }
            setNegativeButton("拒绝", null)
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            val i = permissions.indexOf(Manifest.permission.POST_NOTIFICATIONS)
            if (i != -1 && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionTips()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        basePlayerDelegate.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startViewState.playerState.setOrientation(newConfig.orientation)
        updateSmallModePlayerMaxHeight()
        basePlayerDelegate.onConfigurationChanged(newConfig.orientation)
        basePlayerDelegate.fullscreenController.onOrientationChanged(newConfig.orientation)
        updateStatusBarStyle()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setWindowInsetsAndroidL()
        } else {
            findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
        }
    }

    override fun onBackPressed() {
        if (basePlayerDelegate.fullscreenController.isFullscreen.value && basePlayerDelegate.onBackPressed()) {
            return
        }
        if (startViewState.showSearchDialog) {
            startViewState.closeSearchDialog()
            return
        }
        if (startViewState.isDrawerOpen()) {
            startViewState.closeDrawer()
            return
        }
        super.onBackPressed()
    }

    private fun handleActivityBackPressed() {
        if (composeNavigator.canPopBackStack()) {
            composeNavigator.popBackStack()
        } else {
            handleRootBackPressed()
        }
    }

    private fun handleRootBackPressed() {
        if (!basePlayerDelegate.onBackPressed()) {
            val now = System.currentTimeMillis()
            if (now - lastExitBackPressedTime > 2000) {
                GlobalToaster.show("再按一次退出bilimiao")
                lastExitBackPressedTime = now
            } else {
                finish()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }

    private fun launchWebBrowser(uri: Uri) {
        val typedValue = TypedValue()
        val attrId = com.google.android.material.R.attr.colorSurfaceVariant
        theme.resolveAttribute(attrId, typedValue, true)
        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(ContextCompat.getColor(this, typedValue.resourceId))
                    .build()
            )
            .build()
        intent.launchUrl(this, uri)
    }

    /**
     * 更新竖屏小窗播放器高度（受屏幕尺寸与视频比例限制）
     *
     * 由配置变化 / 视频比例变化时调用，写入 [PlayerState.setSmallModePlayerHeight]。
     */
    private fun updateSmallModePlayerMaxHeight() {
        val metrics = resources.displayMetrics
        val playerState = startViewState.playerState
        val minHeightPx = (200 * metrics.density).toInt()
        val maxHeightPx = minOf(
            (metrics.widthPixels / playerState.playerVideoRatio).toInt(),
            metrics.heightPixels / 2,
        ).coerceAtLeast(minHeightPx)
        val currentHeightPx = playerState.portraitPlayerLayoutState.currentHeightPx
            .coerceIn(minHeightPx, maxHeightPx)
        playerState.setSmallModePlayerHeight(minHeightPx, currentHeightPx, maxHeightPx)
    }
}
