package com.a10miaomiao.bilimiao

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.sheet.BottomSheetDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.material.hidden


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
        bindSingleton { userStore }
        bindSingleton { timeSettingStore }
        bindSingleton { playerDelegate }
    }

    private val windowStore: WindowStore by diViewModel(di)
    private val playerStore: PlayerStore by diViewModel(di)
    private val userStore: UserStore by diViewModel(di)
    private val timeSettingStore: TimeSettingStore by diViewModel(di)

    private val playerDelegate by lazy { PlayerDelegate(this, di) }
    private val bottomSheetDelegate by lazy { BottomSheetDelegate(this, ui) }

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainUi(this)
        setContentView(ui.root)
        playerDelegate.onCreate(savedInstanceState)
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
                setWindowInsets(ui.root.rootWindowInsets)
            }
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

    private fun setMyPageConfig(config: MyPageConfigInfo) {
        ui.mAppBar.setProp {
            title = config.title
            menus = config.menus
        }
    }

    val onBackClick = View.OnClickListener {
        navController.popBackStack()
    }

    fun setWindowInsets (insets: WindowInsets) {
        val left = insets.systemWindowInsetLeft
        val top = insets.systemWindowInsetTop
        val right = insets.stableInsetRight
        val bottom = insets.systemWindowInsetBottom
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
        super.onDestroy()
        playerDelegate.onDestroy()
//        downloadDelegate.onDestroy()
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

    @InternalSplittiesApi
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ui.root.orientation = newConfig.orientation
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