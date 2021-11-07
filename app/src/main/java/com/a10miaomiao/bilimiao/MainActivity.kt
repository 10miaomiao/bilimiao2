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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.experimental.InternalSplittiesApi


class MainActivity
    : AppCompatActivity(),
    DIAware,
    NavController.OnDestinationChangedListener{

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

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        MainNavGraph.createGraph(navHostFragment.navController)
        navController = navHostFragment.navController
        navHostFragment.navController.addOnDestinationChangedListener(this)

        ui.mAppBar.onBackClick = this.onBackClick
    }

    @SuppressLint("RestrictedApi")
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        ui.mAppBar.canBack = controller.backStack.size > 2
        ui.mAppBar.cleanProp()
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

}