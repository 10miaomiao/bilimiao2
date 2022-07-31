package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.util.Rational
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class PlayerController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
): DIAware, VideoPlayerCallBack {

    private val statusBarHelper by instance<StatusBarHelper>()
    private val scaffoldApp get() = delegate.scaffoldApp
    private val views get() = delegate.views
    private val danmakuContext = DanmakuContext.create()

    private fun getFullMode(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        return prefs.getString("player_full_mode", "SENSOR_LANDSCAPE")!!
    }

    fun initController () = views.videoPlayer.run {
        val that = this@PlayerController
        statusBarHelper = that.statusBarHelper
        isFullHideActionBar = true
        backButton.setOnClickListener {
            if (scaffoldApp.fullScreenPlayer) {
                smallScreen()
            } else {
                delegate.closePlayer()
            }
        }
        setIsTouchWiget(true)
        fullscreenButton.setOnClickListener {
            if (scaffoldApp.fullScreenPlayer) {
                smallScreen()
            } else {
                val fullMode = getFullMode()
                fullScreen(fullMode)
            }
        }
        fullscreenButton.setOnLongClickListener {
            showFullModeMenu(it)
            true
        }
        danmakuContext = that.danmakuContext
        qualityView.setOnClickListener(that::showQualityPopupMenu)
        moreBtn.setOnClickListener(that::showMoreMenu)
        videoPlayerCallBack = that
    }

    fun fullScreen(fullMode: String) {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.FULL
        scaffoldApp.fullScreenPlayer = true
        activity.requestedOrientation = when (fullMode) {
            "SENSOR_LANDSCAPE" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            "LANDSCAPE" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            "REVERSE_LANDSCAPE" -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            "UNSPECIFIED" -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        statusBarHelper.isShowStatus = views.videoPlayer.topContainer.visibility == View.VISIBLE
        statusBarHelper.isShowNavigation = false
    }

    fun smallScreen () {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.SMALL
        scaffoldApp.fullScreenPlayer = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        statusBarHelper.isShowStatus = true
        statusBarHelper.isShowNavigation = true
    }

    fun initDanmakuContext() {
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
        views.videoPlayer.isShowDanmaKu = danmakuShow
    }

    fun showQualityPopupMenu(view: View) {
        val sourceInfo = delegate.playerSourceInfo ?: return
        val popup = QualityPopupMenu(
            activity = activity,
            anchor = view,
            list = sourceInfo.acceptList,
            value = delegate.quality
        )
        popup.setOnChangedQualityListener(delegate::changedQuality)
        popup.show()
    }

    fun showFullModeMenu(view: View) {
        val popupMenu = PopupMenu(activity, view)
        val fullMode = getFullMode()
        val checkMenuId = when(fullMode) {
            "SENSOR_LANDSCAPE" -> R.id.full_mode_sl
            "LANDSCAPE" -> R.id.full_mode_l
            "REVERSE_LANDSCAPE" -> R.id.full_mode_rl
            "UNSPECIFIED" -> R.id.full_mode_u
            else -> R.id.full_mode_sl
        }
        popupMenu.inflate(R.menu.player_full_mode)
        popupMenu.menu.findItem(checkMenuId).isChecked = true
        popupMenu.setOnMenuItemClickListener(this::fullModeMenuItemClick)
        popupMenu.show()
    }

    private fun fullModeMenuItemClick(item: MenuItem): Boolean {
        item.isChecked = true
        val fullMode = when(item.itemId) {
            R.id.full_mode_sl -> "SENSOR_LANDSCAPE"
            R.id.full_mode_l -> "LANDSCAPE"
            R.id.full_mode_rl -> "REVERSE_LANDSCAPE"
            R.id.full_mode_u -> "UNSPECIFIED"
            else -> "SENSOR_LANDSCAPE"
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putString("player_full_mode", fullMode).apply()
        if (scaffoldApp.fullScreenPlayer) {
            fullScreen(fullMode)
        }
        return true
    }

    fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.inflate(R.menu.player_top_more)
        popupMenu.setOnMenuItemClickListener(this::moreMenuItemClick)
        popupMenu.show()
    }

    private fun moreMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mini_window -> {
                // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
                var aspectRatio = Rational(16, 9)
                // TODO: 自适应视频画面比例
                delegate.picInPicHelper?.enterPictureInPictureMode(aspectRatio)
            }
            R.id.video_setting -> {
                val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
                nav.navigate(Uri.parse("bilimiao://setting/video"))
            }
            R.id.danmuku_setting -> {
                val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
                nav.navigate(Uri.parse("bilimiao://setting/danmaku"))
            }
        }
        return true
    }

    override fun onVideoPause() {
    }

    override fun onVideoResume(isResume: Boolean) {
    }

    override fun setStateAndUi(state: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            delegate.picInPicHelper?.updatePictureInPictureActions(state)
        }
    }
}