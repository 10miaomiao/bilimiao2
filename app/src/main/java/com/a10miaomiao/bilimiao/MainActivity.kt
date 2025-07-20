package com.a10miaomiao.bilimiao

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.DisplayCutout
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import com.a10miaomiao.bilimiao.activity.SearchActivity
import com.a10miaomiao.bilimiao.comm.BiliGeetestUtilImpl
import com.a10miaomiao.bilimiao.comm.BilimiaoStatService
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate2
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPopupMenu
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.a10miaomiao.bilimiao.comm.navigation.openSearch
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCConfig
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.MainBackPopupMenu
import com.a10miaomiao.bilimiao.page.start.StartFragment
import com.a10miaomiao.bilimiao.service.PlaybackService
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.PlayerBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.MoreExecutors
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.DynamicScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.views.backgroundColor


class MainActivity
    : AppCompatActivity(),
    DIAware {

    private var mainUi: MainUi? = null
    private val ui get() = mainUi!!

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        store.loadStoreModules(this)
        bindSingleton { basePlayerDelegate }
        bindSingleton { themeDelegate }
        bindSingleton { statusBarHelper }
        bindSingleton { supportHelper }
        bindSingleton { biliGeetestUtil }
    }

    private val store by lazy { Store(this, di) }
    private val themeDelegate by lazy { ThemeDelegate(this, di) }
    private val basePlayerDelegate: BasePlayerDelegate by lazy { PlayerDelegate2(this, di) }
    private val statusBarHelper by lazy { StatusBarHelper(this) }
    private val supportHelper by lazy { SupportHelper(this) }
    private val biliGeetestUtil: BiliGeetestUtil by lazy { BiliGeetestUtilImpl(this, lifecycle) }

    private lateinit var leftFragment: StartFragment
    private lateinit var navHostFragment: ComposeFragment

    private var subNavHostFragment: ComposeFragment? = null

    val currentNav: ComposeFragment
        get() = if (ui.root.focusOnMain) navHostFragment else subNavHostFragment ?: navHostFragment
    val anotherNav: ComposeFragment
        get() = if (!ui.root.focusOnMain) navHostFragment else subNavHostFragment ?: navHostFragment

    // 指示器，指示新页面该出现的地方
    val pointerNav: ComposeFragment get() {
        return if (ui.root.subContentShown) {
            if (ui.root.pointerExchanged == ui.root.contentExchanged) {
                navHostFragment
            } else {
                subNavHostFragment ?: navHostFragment
            }
        } else {
            currentNav
        }
    }


    var pageConfig: MyPageConfigInfo? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeDelegate.onCreate(savedInstanceState)

        // 统计服务
        BilimiaoStatService.setAuthorizedState(this, false)
        BilimiaoStatService.start(this)

        // 安卓13开始手动申请通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //2、申请权限: 参数二：权限的数组；参数三：请求码
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        store.onCreate(savedInstanceState)

        lifecycleScope.launch {
            store.appStore.stateFlow.mapNotNull {
                it.theme
            }.flowOn(Dispatchers.Main).collect {
                if (mainUi == null) {
                    initRootView(savedInstanceState)
                }
                val color = it.color.toInt()
                var bgColor = if (it.appBarType == 0) {
                    val hct = Hct.fromInt(color)
                    val isDark = when(it.darkMode) {
                        0 -> themeDelegate.isSystemInDark()
                        1 -> false
                        else -> true
                    }
                    val tone = if (isDark) 20.0 else 90.0
                    Hct.from(hct.hue, 10.0, tone).toInt()
                } else {
                    config.blockBackgroundColor
                }
                bgColor = (bgColor and 0x00FFFFFF) or (0xF8000000).toInt()
                ui.mAppBar.updateTheme(color, bgColor)
            }
        }
    }

    private fun initRootView(savedInstanceState: Bundle?) {
        mainUi = MainUi(this)
        setContentView(ui.root)
        basePlayerDelegate.onCreate(savedInstanceState)
        ui.root.showPlayer = basePlayerDelegate.isPlaying()
        ui.root.playerDelegate = basePlayerDelegate as PlayerDelegate2
        ui.root.onDrawerStateChanged = ::onDrawerStateChanged
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ui.root.rootWindowInsets?.let {
                setWindowInsets(it)
            }
            ui.root.setOnApplyWindowInsetsListener { v, insets ->
                setWindowInsets(insets)
                insets
            }
            ui.root.onPlayerChanged = {
                statusBarHelper.isLightStatusBar =
                    !it || (ui.root.orientation == ScaffoldView.HORIZONTAL && !ui.root.fullScreenPlayer)
                setWindowInsets(ui.root.rootWindowInsets)
            }
        } else {
            setWindowInsetsAndroidL()
        }

        initNavController()
        initAppBar()
        initSettingPreferences()
        initViewFocusable()
    }

    private fun initNavController() {
        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as ComposeFragment
        navHostFragment.pageConfig.setConfig = this::notifyConfigChanged
        if (findViewById<View?>(R.id.nav_host_fragment_sub) != null) {
            val _subNavHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_sub) as ComposeFragment
            _subNavHostFragment.pageConfig.setConfig = this::notifyConfigChanged
            subNavHostFragment = _subNavHostFragment
        }

        (supportFragmentManager.findFragmentByTag(getString(R.string.tag_left_fragment)) as? StartFragment)?.let {
            leftFragment = it
            ui.root.drawerFragment = it
//            if (leftFragment.isVisible) {
//                supportFragmentManager.beginTransaction()
//                    .hide(leftFragment)
//                    .commit()
//            }
        }

        intent.data?.let {
            navHostFragment.navigateByUri(it)
        }
    }

    private fun initAppBar() {
        ui.mAppBar.onBackClick = this.onBackClick
        ui.mAppBar.onOpenMenuClick = this.onOpenMenuClick
        ui.mAppBar.onBackLongClick = this.onBackLongClick
        ui.mAppBar.onMenuItemClick = {
            if (it.prop.action == MenuActions.search) {
                openSearch(it)
            } else {
                val fragment = currentNav
                if (fragment is MyPage) {
                    val childMenu = it.prop.childMenu
                    if (childMenu != null) {
                        val myPopupMenu = MyPopupMenu(
                            activity = this,
                            myPage = fragment,
                            myPageMenu = childMenu,
                            anchorView = it,
                        )
                        myPopupMenu.show()
                    } else {
                        fragment.onMenuItemClick(it, it.prop)
                    }
                }
            }
        }
        ui.mAppBar.onPointerClick = this.onPointerClick
        ui.mAppBar.onPointerLongClick = this.onPointerLongClick
        ui.mAppBar.onExchangeClick = this.onExchangeClick
        ui.mAppBar.onExchangeLongClick = this.onExchangeLongClick
    }

    fun initViewFocusable() {
        ui.root.isFocusable = true
        ui.root.appBar?.isFocusable = true
        ui.root.content?.isFocusable = true
        ui.root.subContent?.isFocusable = true
        ui.root.isFocusableInTouchMode = true
        ui.root.appBar?.isFocusableInTouchMode = true
        ui.root.content?.isFocusableInTouchMode = true
        ui.root.subContent?.isFocusableInTouchMode = true

        ui.root.content?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) changeFocus(true)
        }
        ui.root.subContent?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) changeFocus(false)
        }
    }

    private fun initSettingPreferences() = lifecycleScope.launch {
        SettingPreferences.run {
            val rootView = ui.root
            dataStore.data.collect {
                val playerSmallShowArea = it[PlayerSmallShowArea] ?: 480
                val playerHoldShowArea = it[PlayerSmallShowArea] ?: 130
                val contentDefaultSplit = (it[FlagContentSplit] ?: 35) / 100f
                if (playerSmallShowArea != rootView.playerSmallShowArea
                    || playerHoldShowArea != rootView.playerHoldShowArea
                    || contentDefaultSplit != rootView.contentDefaultSplit) {
                    rootView.playerSmallShowArea = playerSmallShowArea
                    rootView.playerHoldShowArea = playerHoldShowArea
                    rootView.contentDefaultSplit = contentDefaultSplit
                    rootView.updateLayout()
                }
                rootView.fullScreenDraggable = it[PlayerSmallDraggable] ?: false
                rootView.contentAnimationDuration = it[FlagContentAnimationDuration] ?: 0
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let {
            navHostFragment.navigateByUri(it)
        }
    }

    //焦点改变时提示页面标题
    private fun changeFocus(focusOnMain: Boolean) {
        if (ui.root.focusOnMain != focusOnMain) {
            ui.root.focusOnMain = focusOnMain
            //双内容区时自动切换指示器
            if (ui.root.pointerAutoChange && ui.root.subContentShown) {
                ui.root.pointerExchanged = !ui.root.pointerExchanged
            }
            notifyFocusChanged()
        }
    }

    fun notifyFocusChanged() {
//        ui.mAppBar.canBack =
//            currentNav.navController.currentDestination?.id != MainNavGraph.dest.main
        ui.mAppBar.showPointer = ui.root.subContentShown
        ui.mAppBar.pointerOrientation = ui.root.pointerExchanged
        notifyConfigChanged()
    }
    fun notifyConfigChanged(){
        setMyPageConfig(currentNav.pageConfig.configInfo)
    }


    fun setMyPageConfig(config: MyPageConfigInfo) {
        if (config.title.isNotBlank()) {
            pageConfig = config
            ui.mAppBar.canBack =  config.menu?.checkable != true
            ui.mAppBar.setProp {
                title = config.title
                menus = config.getMenuItems()
                isNavigationMenu = config.menu?.checkable == true
                navigationKey = config.menu?.checkedKey ?: 0
            }
            ui.root.slideUpBottomAppBar()
        }
        leftFragment.setConfig(config.search)
    }

    private fun goBackHome(): Boolean {
        currentNav.goBackHome()
        return true
    }

    private val onBackClick = View.OnClickListener {
        onBackPressed()
    }

    private val onOpenMenuClick = View.OnClickListener {
        ui.root.openDrawer()
    }

    private val onBackLongClick = View.OnLongClickListener {
        if (ui.root.showPlayer) {
            MainBackPopupMenu(
                this@MainActivity,
                it,
                basePlayerDelegate
            ).show()
            true
        } else {
            goBackHome()
        }
    }
    private val onPointerClick = View.OnClickListener {
        ui.root.pointerExchanged = !ui.root.pointerExchanged
        notifyFocusChanged()
    }
    private val onPointerLongClick = View.OnLongClickListener {
        ui.root.pointerAutoChange = !ui.root.pointerAutoChange
        true
    }
    private val onExchangeClick = View.OnClickListener {
        if (!ui.root.subContentShown) {
            //单内容区，将焦点给到另一区域
            changeFocus(!ui.root.focusOnMain)
            ui.root.updateLayout(true)
        } else {
            //双内容区，互换
            ui.root.contentExchanged = !ui.root.contentExchanged
        }
        //指示器不锁定时，交换一次方向
        if (ui.root.pointerAutoChange) {
            ui.root.pointerExchanged = !ui.root.pointerExchanged
            notifyFocusChanged()
        }
    }
    private val onExchangeLongClick = View.OnLongClickListener {
        //长按强制全屏
        ui.root.showSubContent = !ui.root.showSubContent
        ui.root.updateLayout(true)
        notifyFocusChanged()
        //小窗行为跟随
        if (!ui.root.subContentShown) {
            ui.root.playerBehavior?.holdUpPlayer()
        } else {
            ui.root.playerBehavior?.holdDownPlayer()
        }
        true
    }

    fun onDrawerStateChanged(state: Int) {
        // 太麻烦
//        supportFragmentManager.beginTransaction().also {
//            if (state == AppBarBehaviorDelegate.STATE_COLLAPSED
//                && leftFragment.isVisible) {
//                leftFragment.hideSoftInput()
//                it.hide(leftFragment)
//            } else if (leftFragment.isHidden) {
//                it.show(leftFragment)
//            }
//        }.commit()
    }

    fun searchSelfPage(keyword: String) {
        currentNav.onSearchSelfPage(this, keyword)
    }

    fun openBottomSheet(page: ComposePage) {
        navHostFragment.openBottomSheet(page)
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
        var displayCutout: DisplayCutout? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            displayCutout = insets.displayCutout
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
        windowStore.setWindowInsets(
            left, top, right, bottom,
        )
        windowStore.setBottomSheetContentInsets(
            0, config.bottomSheetTitleHeight, 0, 0
        )
        val playerLP = ui.mPlayerLayout.layoutParams
        if (playerLP is ScaffoldView.LayoutParams) {
            val behavior = playerLP.behavior
            if (behavior is PlayerBehavior) {
                behavior.setWindowInsets(left, top, right, bottom)
            }
        }
        ui.mAppBar.setWindowInsets(left, top, right, bottom)
        val showPlayer = ui.root.showPlayer
        val fullScreenPlayer = ui.root.fullScreenPlayer
        if (ui.root.orientation == ScaffoldView.VERTICAL) {
            if (showPlayer) {
                windowStore.setContentInsets(
                    left,
                    0,
                    right,
                    top + bottom + config.appBarTitleHeight + ui.root.smallModePlayerMinHeight,
                )
            } else {
                windowStore.setContentInsets(
                    left, top, right, bottom + config.appBarTitleHeight,
                )
            }
            windowStore.setBottomAppBarHeight(config.appBarMenuHeight)
            ui.mContainerView.setPadding(0, 0, 0, 0)
            ui.mSubContainerView.setPadding(0, 0, 0, 0)
            ui.mPlayerLayout.setPadding(
                0, if (fullScreenPlayer) 0 else top, 0, 0
            )
        } else {
            windowStore.setContentInsets(
                0, top, right, bottom,
            )
            windowStore.setBottomAppBarHeight(0)
            ui.mContainerView.setPadding(left, 0, 0, 0)
            ui.mSubContainerView.setPadding(0, 0, 0, 0)
            ui.mPlayerLayout.setPadding(
                0, 0, 0, 0
            )
        }
        basePlayerDelegate.setWindowInsets(left, top, right, bottom, displayCutout)
        ui.root.statusBarHeight = top
        ui.root.updateLayout(false)
    }

    override fun onResume() {
        super.onResume()
        basePlayerDelegate.onResume()

        // 百度移动统计埋点
        BilimiaoStatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        basePlayerDelegate.onPause()

        // 百度移动统计埋点
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
        miaoLogger() debug "onKeyUp: $keyCode"
        when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> {
                val videoPlayer = mainUi?.mVideoPlayerView
                if (videoPlayer != null && mainUi?.root?.showPlayer == true) {
                    if (videoPlayer.isInPlayingState) {
                        videoPlayer.onVideoPause()
                    } else {
                        videoPlayer.onVideoResume()
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                val videoPlayer = mainUi?.mVideoPlayerView
                if (videoPlayer != null && mainUi?.root?.showPlayer == true) {
                    videoPlayer.seekTo(videoPlayer.currentPosition - 5000)
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                val videoPlayer = mainUi?.mVideoPlayerView
                if (videoPlayer != null && mainUi?.root?.showPlayer == true) {
                    videoPlayer.seekTo(videoPlayer.currentPosition + 5000)
                }
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                if (mainUi?.root?.showPlayer == true) {
                    basePlayerDelegate.onBackPressed()
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SearchActivity.REQUEST_CODE -> {
                val arguments = data?.extras ?: Bundle()
                if (arguments.containsKey(SearchActivity.KEY_URL)) {
                    val pageUrl = arguments.getString(SearchActivity.KEY_URL)!!
                    pointerNav.navigateByUri(Uri.parse(pageUrl))
                    return
                }
                val mode = arguments.getInt(SearchActivity.KEY_MODE)
                val keyword = arguments.getString(SearchActivity.KEY_KEYWORD, "")
                if (mode == 0) {
                    pointerNav.navigate(SearchResultPage(
                        keyword = keyword
                    ))
                } else {
                    searchSelfPage(keyword)
                }
            }
        }
    }

    /**
     * 通知权限设置界面跳转
     */
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
        } catch (e: Exception) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    /**
     * 通知权限授权提示
     */
    private fun showNotificationPermissionTips() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("请求授权”通知权限“")
            setMessage("从Android13开始，需要您授予通知权限，在您向该应用授予该权限之前，该应用都将无法发送通知。\n受影响的功能：通知栏播放器控制器、下载进度通知")
            setCancelable(false)
            setPositiveButton("去授权") { dialog, _ ->
                jumpNotificationSetting()
            }
            setNegativeButton("拒绝", null)
        }.show()
    }

    /**
     * 判断授权的方法  授权成功直接调用写入方法  这是监听的回调
     * 参数  上下文   授权结果的数组   申请授权的数组
     */
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
        if (isInPictureInPictureMode) { // 进入画中画模式，则隐藏其它控件

        } else {

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        basePlayerDelegate.onConfigurationChanged(newConfig)
        ui.root.orientation = newConfig.orientation
        statusBarHelper.isLightStatusBar =
            !ui.root.showPlayer || (ui.root.orientation == ScaffoldView.HORIZONTAL && !ui.root.fullScreenPlayer)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setWindowInsetsAndroidL()
        }
    }

    private fun onHostNavBack(): Boolean {
//        if (ui.mAppBar.canBack) {
            currentNav.onBackPressed()
            return true
//        } else {
//            return false
//        }
    }

    override fun onBackPressed() {
        if (leftFragment.onBackPressed()) {
            return
        }
        if (ui.root.isDrawerOpen()) {
            ui.root.closeDrawer()
            return
        }
        if (ui.root.fullScreenPlayer && basePlayerDelegate.onBackPressed()) {
            return
        }
        if (onHostNavBack()) {
            return
        }
        super.onBackPressed()
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration: Configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }

}