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
import androidx.compose.ui.platform.ComposeView
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cn.a10miaomiao.bilimiao.compose.MainActivityComposeHost
import cn.a10miaomiao.bilimiao.compose.MainActivityComposeNavigator
import cn.a10miaomiao.bilimiao.compose.StartViewWrapper
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.ComposeHostBridge
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import com.a10miaomiao.bilimiao.comm.BiliGeetestUtilImpl
import com.a10miaomiao.bilimiao.comm.BilimiaoStatService
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate2
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerViews
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.a10miaomiao.bilimiao.comm.scanner.BilimiaoScanner
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlaybackService
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.scaffold.PlayerHostState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.materialkolor.hct.Hct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class MainActivity : AppCompatActivity(), DIAware {

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        store.loadStoreModules(this)
        bindSingleton { startViewWrapper }
        bindSingleton { basePlayerDelegate }
        bindSingleton { themeDelegate }
        bindSingleton { statusBarHelper }
        bindSingleton { supportHelper }
        bindSingleton { biliGeetestUtil }
    }

    private val store by lazy { Store(this, di) }
    private val themeDelegate by lazy { ThemeDelegate(this, di) }
    private val statusBarHelper by lazy { StatusBarHelper(this) }
    private val supportHelper by lazy { SupportHelper(this) }
    private val biliGeetestUtil: BiliGeetestUtil by lazy { BiliGeetestUtilImpl(this, lifecycle) }

    private val playerHostState by lazy { DirectComposePlayerHostState() }
    private val messageDialogState = cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState()
    private val bottomSheetState = BottomSheetState()
    private val pageConfigState = PageConfigState()
    private val emitter = SharedFlowEmitter()
    private val composeNavigator = MainActivityComposeNavigator(
        launchUrl = ::launchWebBrowser,
        onClose = { finish() },
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
            bindSingleton { bottomSheetState }
        }
    }
    private var pageConfig: MyPageConfigInfo? = null
    private var pendingDeepLink: Uri? = null

    private lateinit var playerLayout: FrameLayout
    private lateinit var videoPlayerView: DanmakuVideoPlayer

    private val playerViews = object : PlayerViews {
        override val videoPlayer: DanmakuVideoPlayer
            get() = videoPlayerView

        override fun <T : View> findViewById(id: Int): T {
            return playerLayout.findViewById(id)!!
        }
    }

    private val startViewWrapper by lazy {
        StartViewWrapper(
            this,
            this::startMenuNavigate,
            this::startMenuNavigateUrl,
            this::startMenuDismissRequest,
            this::startMenuOpenScanner,
        )
    }
    private val basePlayerDelegate: BasePlayerDelegate by lazy {
        PlayerDelegate2(this, playerViews, playerHostState, di)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        super.onCreate(savedInstanceState)
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
                if (!playerHostState.fullScreenPlayer) {
                    requestedOrientation = when (it) {
                        true -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        }
        lifecycleScope.launch {
            pageConfigState.collectConfig {
                setMyPageConfig(
                    MyPageConfigInfo(
                        title = it.title,
                        menu = it.menu,
                        search = it.search,
                    )
                )
            }
        }
    }

    private fun initRootView(savedInstanceState: Bundle?) {
        createPlayerViews()
        setupPlayerViewInWrapper()
        basePlayerDelegate.onCreate(savedInstanceState)
        playerHostState.showPlayer = basePlayerDelegate.isPlaying()

        val rootComposeView = ComposeView(this).apply {
            setContent {
                val appState = store.appStore.stateFlow.collectAsState().value
                val windowState = store.windowStore.stateFlow.collectAsState().value
                MainActivityComposeHost(
                    navigator = composeNavigator,
                    hostDi = composeHostDi,
                    startViewWrapper = startViewWrapper,
                    appState = appState,
                    windowState = windowState,
                    bottomAppBarHeightDp = store.windowStore.bottomAppBarHeightDp,
                    pageConfigState = pageConfigState,
                    emitter = emitter,
                    messageDialogState = messageDialogState,
                    bottomSheetState = bottomSheetState,
                    containerView = playerHostState.contentContainerView,
                    bottomSheetContainerView = playerHostState.bottomSheetContainerView,
                    appBarBackgroundColor = appBarBackgroundColor,
                    onBackClick = ::onBackPressed,
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

    private fun createPlayerViews() {
        videoPlayerView = PlayerViewKeeper.keepPlayerView?.apply {
            try {
                (parent as? ViewGroup)?.removeAllViews()
                val contextField = View::class.java.getDeclaredField("mContext")
                contextField.isAccessible = true
                if (contextField.get(this) is Context) {
                    contextField.set(this, this@MainActivity)
                }
            } catch (_: Exception) {
            }
        } ?: layoutInflater.inflate(R.layout.include_palyer2, null, false) as DanmakuVideoPlayer
        PlayerViewKeeper.keepPlayerView = videoPlayerView

        playerLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(videoPlayerView, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ))
            addView(layoutInflater.inflate(R.layout.include_completion_box, this, false))
            addView(layoutInflater.inflate(R.layout.include_error_message_box, this, false))
            addView(layoutInflater.inflate(R.layout.include_area_limit_box, this, false))
            addView(layoutInflater.inflate(R.layout.include_player_loading, this, false))
        }
    }

    private fun setupPlayerViewInWrapper() {
        startViewWrapper.playerView = playerLayout
        startViewWrapper.setShowPlayer(playerHostState.showPlayer)
        startViewWrapper.setOrientation(playerHostState.orientation)
        startViewWrapper.setFullScreenPlayer(playerHostState.fullScreenPlayer)
        startViewWrapper.setSmallModePlayerMinHeight(playerHostState.smallModePlayerMinHeight)
        startViewWrapper.setSmallModePlayerCurrentHeight(playerHostState.smallModePlayerCurrentHeight)
        startViewWrapper.setPlayerSmallShowArea(
            playerHostState.playerSmallShowArea,
            playerHostState.playerSmallShowArea / 16 * 9,
        )
        startViewWrapper.setPlayerVideoRatio(playerHostState.playerVideoRatio)
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

    private fun setMyPageConfig(config: MyPageConfigInfo) {
        if (config.title.isBlank()) {
            return
        }
        pageConfig = config
        val searchConfig = config.search
        startViewWrapper.setPageSearchMethod(
            if (searchConfig?.name.isNullOrBlank()) {
                null
            } else {
                object : cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod {
                    override val name: String
                        get() = searchConfig?.name ?: ""

                    override fun onSearch(keyword: String) {
                        searchSelfPage(keyword)
                    }
                }
            }
        )
    }

    fun searchSelfPage(keyword: String) {
        pageConfigState.onSearchSelfPage(this, keyword)
    }

    fun openBottomSheet(page: ComposePage) {
        bottomSheetState.open(page)
    }

    fun goBackHome() {
        composeNavigator.goBackHome()
    }

    private fun startMenuNavigate(page: ComposePage) {
        startViewWrapper.closeDrawer()
        composeNavigator.navigate(page)
    }

    private fun startMenuNavigateUrl(url: String) {
        startViewWrapper.closeDrawer()
        val uri = Uri.parse(url)
        if (!composeNavigator.navigateByUri(uri)) {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun startMenuDismissRequest() {
        startViewWrapper.closeDrawer()
    }

    fun openSearchDialog() {
        val searchConfig = pageConfig?.search
        val keyword = searchConfig?.keyword ?: ""
        val mode = if (searchConfig?.name.isNullOrBlank()) 0 else 1
        startViewWrapper.openSearchDialog(keyword, mode, false)
        startViewWrapper.openDrawer()
    }

    private fun startMenuOpenScanner(callback: (result: String) -> Unit): Boolean {
        startViewWrapper.closeDrawer()
        return BilimiaoScanner.openScanner(
            this,
            themeDelegate.themeColor.toInt(),
            callback,
        )
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
        val windowStore = store.windowStore
        windowStore.setWindowInsets(left, top, right, bottom)
        windowStore.setBottomSheetContentInsets(0, config.bottomSheetTitleHeight, 0, 0)
        if (playerHostState.orientation == PlayerHostState.VERTICAL) {
            if (playerHostState.showPlayer) {
                windowStore.setContentInsets(
                    left,
                    0,
                    right,
                    top + bottom + config.appBarTitleHeight + playerHostState.smallModePlayerMinHeight,
                )
            } else {
                windowStore.setContentInsets(
                    left,
                    top,
                    right,
                    bottom + config.appBarTitleHeight,
                )
            }
            windowStore.setBottomAppBarHeight(config.appBarMenuHeight)
        } else {
            windowStore.setContentInsets(left, top, right, bottom)
            windowStore.setBottomAppBarHeight(0)
        }
        playerHostState.updateSmallModePlayerMaxHeight()
        basePlayerDelegate.setWindowInsets(left, top, right, bottom, displayCutout)
        updateStatusBarStyle()
    }

    private fun updateStatusBarStyle() {
        statusBarHelper.isLightStatusBar =
            !playerHostState.showPlayer || (playerHostState.orientation == PlayerHostState.HORIZONTAL && !playerHostState.fullScreenPlayer)
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
        when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> {
                if (playerHostState.showPlayer) {
                    if (videoPlayerView.isInPlayingState) {
                        videoPlayerView.onVideoPause()
                    } else {
                        videoPlayerView.onVideoResume()
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (playerHostState.showPlayer) {
                    videoPlayerView.seekTo(videoPlayerView.currentPosition - 5000)
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (playerHostState.showPlayer) {
                    videoPlayerView.seekTo(videoPlayerView.currentPosition + 5000)
                }
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                if (playerHostState.showPlayer) {
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
        basePlayerDelegate.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        playerHostState.orientation = newConfig.orientation
        startViewWrapper.setOrientation(playerHostState.orientation)
        playerHostState.updateSmallModePlayerMaxHeight()
        basePlayerDelegate.onConfigurationChanged(newConfig)
        updateStatusBarStyle()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setWindowInsetsAndroidL()
        } else {
            findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
        }
    }

    override fun onBackPressed() {
        if (startViewWrapper.isDrawerOpen()) {
            if (startViewWrapper.showSearchDialog) {
                startViewWrapper.closeSearchDialog()
                return
            }
            startViewWrapper.closeDrawer()
            return
        }
        if (playerHostState.fullScreenPlayer && basePlayerDelegate.onBackPressed()) {
            return
        }
        composeNavigator.popBackStack()
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

    private inner class DirectComposePlayerHostState : PlayerHostState {
        val contentContainerView = FrameLayout(this@MainActivity)
        val bottomSheetContainerView = FrameLayout(this@MainActivity).apply {
            tag = "bottomSheet"
        }

        override var showPlayer: Boolean = false
            set(value) {
                field = value
                startViewWrapper.setShowPlayer(value)
                updateStatusBarStyle()
                findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
            }

        override var fullScreenPlayer: Boolean = false
            set(value) {
                field = value
                startViewWrapper.setFullScreenPlayer(value)
                updateStatusBarStyle()
                findViewById<View>(android.R.id.content).rootWindowInsets?.let(::setWindowInsets)
            }

        override var orientation: Int = resources.configuration.orientation
            set(value) {
                field = value
                startViewWrapper.setOrientation(value)
                updateStatusBarStyle()
            }

        val smallModePlayerMinHeight: Int = (200 * resources.displayMetrics.density).toInt()
        var smallModePlayerCurrentHeight: Int = smallModePlayerMinHeight
            set(value) {
                field = value
                startViewWrapper.setSmallModePlayerCurrentHeight(value)
            }
        override var smallModePlayerMaxHeight: Int = smallModePlayerMinHeight
        var playerSmallShowArea: Int = 480
            set(value) {
                field = value
                startViewWrapper.setPlayerSmallShowArea(value, value / 16 * 9)
            }
        var playerVideoRatio: Float = 16f / 9f
            set(value) {
                field = value
                startViewWrapper.setPlayerVideoRatio(value)
                updateSmallModePlayerMaxHeight()
            }

        override fun animatePlayerHeight(target: Int) {
            if (orientation != PlayerHostState.VERTICAL || smallModePlayerCurrentHeight == target) {
                return
            }
            val animator = android.animation.ValueAnimator.ofInt(smallModePlayerCurrentHeight, target)
            animator.duration = 200
            animator.addUpdateListener {
                smallModePlayerCurrentHeight = it.animatedValue as Int
            }
            animator.start()
        }

        override fun holdUpPlayer() {
        }

        fun updateSmallModePlayerMaxHeight() {
            val metrics = resources.displayMetrics
            val maxHeightByRatio = (metrics.widthPixels / playerVideoRatio).toInt()
            smallModePlayerMaxHeight = minOf(maxHeightByRatio, metrics.heightPixels / 2)
            if (smallModePlayerCurrentHeight > smallModePlayerMaxHeight) {
                smallModePlayerCurrentHeight = smallModePlayerMaxHeight
            }
            startViewWrapper.setSmallModePlayerMinHeight(smallModePlayerMinHeight)
            startViewWrapper.setSmallModePlayerCurrentHeight(smallModePlayerCurrentHeight)
        }
    }

    private object PlayerViewKeeper {
        var keepPlayerView: DanmakuVideoPlayer? = null
    }
}
