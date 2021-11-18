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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.PlayerStore
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
    FragmentOnAttachListener,
    FragmentManager.OnBackStackChangedListener {

    lateinit var ui: MainUi

    override val di: DI = DI.lazy {
        bindSingleton { this@MainActivity }
        bindSingleton { windowStore }
        bindSingleton { playerStore }
        bindSingleton { playerDelegate }
    }

    private val windowStore: WindowStore by diViewModel(di)
    private val playerStore: PlayerStore by diViewModel(di)

    private val playerDelegate by lazy { PlayerDelegate(this, di) }

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainUi(this)
        setContentView(ui.root)
        playerDelegate.onCreate(savedInstanceState)

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
        MainNavGraph.createGraph(navController)

        navHostFragment.childFragmentManager.addFragmentOnAttachListener(this)
        navHostFragment.childFragmentManager.addOnBackStackChangedListener(this)


        ui.mAppBar.onBackClick = this.onBackClick
        initBottomSheet()
//        ui.root.bottomSheetBehavior?.hidden = true
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        navHostFragment.childFragmentManager.removeFragmentOnAttachListener(this)
        onBackStackChanged()
    }

    override fun onBackStackChanged() {
        val childFragmentManager = navHostFragment.childFragmentManager
        ui.mAppBar.canBack = childFragmentManager.backStackEntryCount > 0
        ui.mAppBar.cleanProp()
        val primaryNavigationFragment = childFragmentManager.primaryNavigationFragment
        if (primaryNavigationFragment is MyPage) {
            val config = primaryNavigationFragment.pageConfig
            config.setConfig = this::setMyPageConfig
            config.notifyConfigChanged()
        }
    }

    private fun setMyPageConfig(config: MyPageConfigInfo) {
        ui.mAppBar.setProp {
            title = config.title
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

    private fun initBottomSheet() {
        ui.root.bottomSheetBehavior?.let { behavior ->
            behavior.isHideable = true
            behavior.isFitToContents = false
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {

//                    if (p1 < 0) {
//                        shadeView.alpha = (p1 + 1) * 0.6f
//                    } else {
//                        shadeView.alpha = 0.6f
//                    }
                }

                override fun onStateChanged(p0: View, p1: Int) {

//                    if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
//                        shadeView.visibility = View.GONE
//                        bottomSheetFragment?.let {
//                            supportFragmentManager.beginTransaction()
//                                .remove(it)
//                                .commit()
//                            bottomSheetFragment = null
//                        }
//                    } else {
//                        shadeView.visibility = View.VISIBLE
//                    }
                }
            })
        }
    }

    override fun onBackPressed() {
        // 上滑菜单未关闭则先关闭上滑菜单
        val behavior = ui.root.bottomSheetBehavior
        if (behavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
            behavior?.state =  BottomSheetBehavior.STATE_HIDDEN
            return
        }
        if (playerDelegate.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}