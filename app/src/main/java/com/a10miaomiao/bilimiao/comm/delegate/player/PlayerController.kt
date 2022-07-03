package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class PlayerController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
): DIAware {

    private val statusBarHelper by instance<StatusBarHelper>()
    private val scaffoldApp get() = delegate.scaffoldApp
    private val views get() = delegate.views
    private val danmakuContext = DanmakuContext.create()

    fun initController () {
        views.videoPlayer.isFullHideActionBar = true
        views.videoPlayer.backButton.setOnClickListener {
            if (scaffoldApp.fullScreenPlayer) {
                smallScreen()
            } else {
                delegate.closePlayer()
            }
        }
        views.videoPlayer.setIsTouchWiget(true)
        views.videoPlayer.fullscreenButton.setOnClickListener {
            if (scaffoldApp.fullScreenPlayer) {
                smallScreen()
            } else {
                fullScreen()
            }
        }
        initDanmakuContext()
        views.videoPlayer.danmakuContext = danmakuContext
    }

    fun fullScreen() {
        scaffoldApp.fullScreenPlayer = true
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        statusBarHelper.isShowStatus = false
        statusBarHelper.isShowNavigation = false
    }

    fun smallScreen () {
        scaffoldApp.fullScreenPlayer = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        statusBarHelper.isShowStatus = true
        statusBarHelper.isShowNavigation = true
    }

    private fun initDanmakuContext() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        var scaleTextSize = prefs.getString("danmaku_fontsize", "1")?.toFloatOrNull() ?: 1f
        val danmakuSpeed = prefs.getString("danmaku_speed", "1")?.toFloatOrNull() ?: 1f
        val danmakuShow = prefs.getBoolean("danmaku_show", true)
        val danmakuR2LShow = prefs.getBoolean("danmaku_r2l_show", true)
        val danmakuFTShow = prefs.getBoolean("danmaku_ft_show", true)
        val danmakuFBShow = prefs.getBoolean("danmaku_fb_show", true)
        val danmakuSpecialShow = prefs.getBoolean("danmaku_special_show", true)
        //设置最大显示行数
        var maxLinesPair = mapOf<Int, Int>()
        //设置是否禁止重叠
        val overlappingEnablePair = mapOf(
            BaseDanmaku.TYPE_SCROLL_RL to true,
            BaseDanmaku.TYPE_FIX_TOP to true
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity.isInPictureInPictureMode) {
            scaleTextSize *= 0.6f
            maxLinesPair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to 4,
                BaseDanmaku.TYPE_FIX_TOP to 2,
                BaseDanmaku.TYPE_FIX_BOTTOM to 2
            )
        }
//        else if (isMiniPlayer.value === true) {
//            maxLinesPair = mapOf(
//                BaseDanmaku.TYPE_SCROLL_RL to 5
//            )
//        }
        //设置弹幕样式
        danmakuContext?.apply {
            ftDanmakuVisibility = danmakuFTShow
            fbDanmakuVisibility = danmakuFBShow
            r2LDanmakuVisibility = danmakuR2LShow
            specialDanmakuVisibility = danmakuSpecialShow
//            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
//            isDuplicateMergingEnabled = false
            setScrollSpeedFactor(danmakuSpeed)
            setScaleTextSize(scaleTextSize)
            setMaximumLines(maxLinesPair)
//            preventOverlapping(overlappingEnablePair)
        }
    }




}