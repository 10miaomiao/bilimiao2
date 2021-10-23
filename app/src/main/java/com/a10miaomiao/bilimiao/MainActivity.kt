package com.a10miaomiao.bilimiao

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.comm.*
import splitties.experimental.InternalSplittiesApi
import splitties.views.bottomPadding

class MainActivity : AppCompatActivity() {

    lateinit var ui: MainUi

    @InternalSplittiesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainUi(this)
        setContentView(ui.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ui.root.rootWindowInsets?.let {
                ui.mAppBar.bottomPadding = it.systemWindowInsetBottom
            }
            ui.root.setOnApplyWindowInsetsListener { v, insets ->
                if (ui.root.orientation == ScaffoldView.VERTICAL) {
                    ui.mAppBar.setPadding(
                        insets.systemWindowInsetLeft,
                        0,
                        insets.systemWindowInsetRight,
                        insets.systemWindowInsetBottom
                    )
                    ui.mPlayerLayout.setPadding(
                        0,
                        insets.systemWindowInsetTop,
                        0,
                        0
                    )
                } else {
                    ui.mAppBar.setPadding(
                        insets.systemWindowInsetLeft,
                        insets.systemWindowInsetTop,
                        0,
                        insets.systemWindowInsetBottom
                    )
                    ui.mPlayerLayout.setPadding(
                        0,
                        0,
                        insets.systemWindowInsetRight,
                        0
                    )
                }
                insets
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            fullScreenUseStatus()
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        MainNavGraph.createGraph(navHostFragment.navController)

    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun fullScreenUseStatus() {
        val attributes = window.attributes
        attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = attributes
    }

    @InternalSplittiesApi
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ui.root.orientation = newConfig.orientation
    }


}