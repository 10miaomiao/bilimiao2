package com.a10miaomiao.bilimiao

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.sheet.BottomSheetDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate2
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.store.*
import com.a10miaomiao.bilimiao.widget.comm.behavior.PlayerBehavior
import com.baidu.mobstat.StatService
import splitties.dimensions.dip


class MainActivity
    : AppCompatActivity(),
    DIAware,
    NavController.OnDestinationChangedListener,
    FragmentOnAttachListener {

    lateinit var ui: MainUi

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        store.loadStoreModules(this)
        bindSingleton { basePlayerDelegate }
        bindSingleton { themeDelegate }
        bindSingleton { downloadDelegate }
        bindSingleton { statusBarHelper }
        bindSingleton { supportHelper }
    }

    private val store by lazy { Store(this, di) }
    private val themeDelegate by lazy { ThemeDelegate(this, di) }
    private val downloadDelegate by lazy { DownloadDelegate(this, di) }
    private val basePlayerDelegate: BasePlayerDelegate by lazy { PlayerDelegate2(this, di) }
    private val bottomSheetDelegate by lazy { BottomSheetDelegate(this, ui) }
    private val statusBarHelper by lazy { StatusBarHelper(this) }
    private val supportHelper by lazy { SupportHelper(this) }

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeDelegate.onCreate(savedInstanceState)
        ui = MainUi(this)
        setContentView(ui.root)
        basePlayerDelegate.onCreate(savedInstanceState)
        downloadDelegate.onCreate(savedInstanceState)
        bottomSheetDelegate.onCreate(savedInstanceState)
        store.onCreate(savedInstanceState)
        ui.root.showPlayer = basePlayerDelegate.isPlaying()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ui.root.rootWindowInsets?.let {
                setWindowInsets(it)
            }
            ui.root.setOnApplyWindowInsetsListener { v, insets ->
                setWindowInsets(insets)
                insets
            }
            ui.root.onPlayerChanged = {
                statusBarHelper.isLightStatusBar = !it || (ui.root.orientation == ScaffoldView.HORIZONTAL && !ui.root.fullScreenPlayer)
                setWindowInsets(ui.root.rootWindowInsets)
            }
        } else {
            setWindowInsetsAndroidL()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            fullScreenUseStatus()
        }

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        MainNavGraph.createGraph(navController, MainNavGraph.dest.home)
        navController.addOnDestinationChangedListener(this)
        navHostFragment.childFragmentManager.addFragmentOnAttachListener(this)

        ui.mAppBar.onBackClick = this.onBackClick
        ui.mAppBar.onMenuItemClick = {
            val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            if (fragment is MyPage) {
                fragment.onMenuItemClick(it, it.prop)
            }
        }

//        lifecycleScope.launch(Dispatchers.IO){
//            val refreshToken = Bilimiao.commApp.loginInfo!!.token_info.refresh_token
//            DebugMiao.log(Bilimiao.commApp.loginInfo)
//            val res = BiliApiService.authApi.refreshToken(refreshToken).awaitCall()
//            DebugMiao.log("oauth2", res.body()?.string())
//        }

        // 百度统计
        StatService.setAuthorizedState(this, false)
        StatService.start(this)

        navController.handleDeepLink(intent)

        val intentFilter = IntentFilter().apply {
            addAction(PlayerService.ACTION_CREATED)
        }
        registerReceiver(broadcastReceiver, intentFilter)
        if (PlayerService.selfInstance == null) {
            startService(Intent(this, PlayerService::class.java))
        }

    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.miao_fragment_open_enter)
                .setExitAnim(R.anim.miao_fragment_open_exit)
                .setPopEnterAnim(R.anim.miao_fragment_close_enter)
                .setPopExitAnim(R.anim.miao_fragment_close_exit)
                .build()
            try {
                navController.navigate(uri, navOptions)
                true
            } catch (e: IllegalArgumentException) {
            }
        }
    }


    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment is MyPage) {
            val config = fragment.pageConfig
            config.setConfig = this::setMyPageConfig
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        ui.mAppBar.canBack = destination.id != MainNavGraph.dest.home
        ui.mAppBar.cleanProp()
    }

    fun setMyPageConfig(config: MyPageConfigInfo) {
        ui.mAppBar.setProp {
            title = config.title
            menus = config.menus
        }
        ui.setNavigationTitle(config.title)
    }

    val onBackClick = View.OnClickListener {
        onBackPressed()
    }
    
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                PlayerService.ACTION_CREATED -> {
                    PlayerService.selfInstance?.videoPlayerView = findViewById(R.id.video_player)
                }
            }
        }
    }

    fun setWindowInsetsAndroidL() {
        val rectangle = Rect()
        val displayMetrics = DisplayMetrics()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val top = statusBarHelper.getStatusBarHeight()
        val bottom = displayMetrics.heightPixels - rectangle.bottom - rectangle.top
        val right = displayMetrics.widthPixels - rectangle.right
        setWindowInsets(0, top, right, bottom)
    }
    fun setWindowInsets (insets: WindowInsets) {
        val left = insets.systemWindowInsetLeft
        val top = insets.systemWindowInsetTop
        val right = insets.stableInsetRight
        val bottom = insets.systemWindowInsetBottom
        setWindowInsets(left, top, right, bottom)
    }
    fun setWindowInsets (left: Int, top: Int, right: Int, bottom: Int) {
        val windowStore = store.windowStore
        windowStore.setWindowInsets(
            left, top, right, bottom,
        )
        windowStore.setBottomSheetContentInsets(
            0, config.bottomSheetTitleHeight, 0, bottom
        )
        val playerLP = ui.mPlayerLayout.layoutParams
        if (playerLP is ScaffoldView.LayoutParams) {
            val behavior = playerLP.behavior
            if (behavior is PlayerBehavior) {
                behavior.setWindowInsets(left, top, right, bottom)
            }
        }
        ui.mBottomSheetLayout.setPadding(left, top, right, 0)
        val showPlayer = ui.root.showPlayer
        val fullScreenPlayer = ui.root.fullScreenPlayer
        if (ui.root.orientation == ScaffoldView.VERTICAL) {
            windowStore.setContentInsets(
                left, if (showPlayer) 0 else top, right, 0,
            )
            ui.mAppBar.setPadding(
                left, 0, right, bottom
            )
            ui.mPlayerLayout.setPadding(
                0, if (fullScreenPlayer) 0 else top, 0, 0
            )
        } else {
//            windowStore.setContentInsets(
//                0, top, if (showPlayer) 0 else right, bottom,
//            )
            windowStore.setContentInsets(
                0, top, right, bottom,
            )
            ui.mAppBar.setPadding(
                left, top, 0, bottom
            )
            ui.mPlayerLayout.setPadding(
                0, 0, 0, 0
            )
        }
        ui.leftNavigationView.setPadding(
            left, if (showPlayer) 0 else top, 0, 0,
        )
        basePlayerDelegate.setWindowInsets(left, top, right, bottom)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun fullScreenUseStatus() {
        val attributes = window.attributes
        attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = attributes
    }

    override fun onResume() {
        super.onResume()
        basePlayerDelegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        basePlayerDelegate.onPause()
    }

    override fun onDestroy() {
        basePlayerDelegate.onDestroy()
        downloadDelegate.onDestroy()
        bottomSheetDelegate.onDestroy()
        store.onDestroy()
        navController.removeOnDestinationChangedListener(this)
        navHostFragment.childFragmentManager.removeFragmentOnAttachListener(this)
        unregisterReceiver(broadcastReceiver)
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

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
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
        statusBarHelper.isLightStatusBar = !ui.root.showPlayer || (ui.root.orientation == ScaffoldView.HORIZONTAL && !ui.root.fullScreenPlayer)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setWindowInsetsAndroidL()
        }
    }

    override fun onBackPressed() {
        if (bottomSheetDelegate.onBackPressed()) {
            return
        }
        if (basePlayerDelegate.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

}