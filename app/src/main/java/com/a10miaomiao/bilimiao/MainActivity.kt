package com.a10miaomiao.bilimiao

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SimpleArrayMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.sheet.BottomSheetDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.store.*
import com.baidu.mobstat.StatService


class MainActivity
    : AppCompatActivity(),
    DIAware,
    NavController.OnDestinationChangedListener,
    FragmentOnAttachListener {

    lateinit var ui: MainUi

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        bindSingleton { windowStore }
        bindSingleton { playerStore }
        bindSingleton { downloadStore }
        bindSingleton { userStore }
        bindSingleton { timeSettingStore }
        bindSingleton { filterStore }
        bindSingleton { playerDelegate }
        bindSingleton { themeDelegate }
        bindSingleton { downloadDelegate }
        bindSingleton { statusBarHelper }
        bindSingleton { supportHelper }
    }

    private val windowStore: WindowStore by diViewModel(di)
    private val playerStore: PlayerStore by diViewModel(di)
    private val downloadStore: DownloadStore by diViewModel(di)
    private val userStore: UserStore by diViewModel(di)
    private val timeSettingStore: TimeSettingStore by diViewModel(di)
    private val filterStore: FilterStore by diViewModel(di)

    private val themeDelegate by lazy { ThemeDelegate(this, di) }
    private val downloadDelegate by lazy { DownloadDelegate(this, di) }
    private val playerDelegate by lazy { PlayerDelegate(this, di) }
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
        playerDelegate.onCreate(savedInstanceState)
        downloadDelegate.onCreate(savedInstanceState)
        bottomSheetDelegate.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ui.root.rootWindowInsets?.let {
                setWindowInsets(it)
            }
            ui.root.setOnApplyWindowInsetsListener { v, insets ->
                setWindowInsets(insets)
                insets
            }
            ui.root.onPlayerChanged = {
                statusBarHelper.isLightStatusBar = !it
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
        timeSettingStore.initState()

        ui.mAppBar.onBackClick = this.onBackClick
        ui.mAppBar.onMenuItemClick = {
            val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            if (fragment is MyPage) {
                fragment.onMenuItemClick(it)
            }
        }

        userStore.init(this)

//        lifecycleScope.launch(Dispatchers.IO){
//            val refreshToken = Bilimiao.commApp.loginInfo!!.token_info.refresh_token
//            DebugMiao.log(Bilimiao.commApp.loginInfo)
//            val res = BiliApiService.authApi.refreshToken(refreshToken).awaitCall()
//            DebugMiao.log("oauth2", res.body()?.string())
//        }

        // 百度统计
        StatService.setAuthorizedState(this, false)
        StatService.start(this)

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
    }

    val onBackClick = View.OnClickListener {
        navController.popBackStack()
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
        windowStore.setWindowInsets(
            left, top, right, bottom,
        )
        windowStore.setBottomSheetContentInsets(
            0, config.bottomSheetTitleHeight, 0, bottom
        )
        ui.mBottomSheetLayout.setPadding(left, top, right, 0)
        val showPlayer = ui.root.showPlayer
        if (ui.root.orientation == ScaffoldView.VERTICAL) {
            windowStore.setContentInsets(
                left, if (showPlayer) 0 else top, right, 0,
            )
            ui.mAppBar.setPadding(
                left, 0, right, bottom
            )
            ui.mPlayerLayout.setPadding(
                0, top, 0, 0
            )
        } else {
            windowStore.setContentInsets(
                0, top, if (showPlayer) 0 else right, bottom,
            )
            ui.mAppBar.setPadding(
                left, top, 0, bottom
            )
            ui.mPlayerLayout.setPadding(
                0, 0, right, 0
            )
        }
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
        playerDelegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        playerDelegate.onPause()
    }

    override fun onDestroy() {
        playerDelegate.onDestroy()
        downloadDelegate.onDestroy()
        bottomSheetDelegate.onDestroy()
        navController.removeOnDestinationChangedListener(this)
        navHostFragment.childFragmentManager.removeFragmentOnAttachListener(this)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        playerDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        playerDelegate.onStop()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        playerDelegate.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) { // 进入画中画模式，则隐藏其它控件

        } else {

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ui.root.orientation = newConfig.orientation
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setWindowInsetsAndroidL()
        }
    }

    override fun onBackPressed() {
        if (bottomSheetDelegate.onBackPressed()) {
            return
        }
        if (playerDelegate.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

}