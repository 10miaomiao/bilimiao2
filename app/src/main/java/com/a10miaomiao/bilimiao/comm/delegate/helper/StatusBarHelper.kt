package com.a10miaomiao.bilimiao.comm.delegate.helper

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate

class StatusBarHelper(
    val activity: Activity,
) {

    var isShowNavigation = true
        set(value) {
            field = value
            update()
        }
    var isShowStatus = true
        set(value) {
            field = value
            update()
        }
    var isLightStatusBar = true
        set(value) {
            field = value
            update()
        }

    init {
        // 全透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.run {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = Color.TRANSPARENT
            }
        }
    }

    fun update () {
        var uiFlags = if (isShowStatus) {
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        uiFlags = uiFlags or 0x00001000
        if (!isShowNavigation) {
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        if (isLightStatusBar && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        activity.window.decorView.systemUiVisibility = uiFlags
    }

    fun getStatusBarHeight (): Int {
        var statusBarHeight = 0
        //获取status_bar_height资源的ID
        val resourceId: Int = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

}