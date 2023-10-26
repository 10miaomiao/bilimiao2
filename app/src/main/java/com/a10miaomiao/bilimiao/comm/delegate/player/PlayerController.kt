package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.util.Rational
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.PageRoute
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesFragment
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesParam
import com.a10miaomiao.bilimiao.page.setting.VideoSettingFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesParam
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import com.kongzue.dialogx.dialogs.PopTip
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class PlayerController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware, VideoPlayerCallBack, GSYVideoProgressListener {

    private val userStore by instance<UserStore>()
    private val statusBarHelper by instance<StatusBarHelper>()
    private val scaffoldApp get() = delegate.scaffoldApp
    private val views get() = delegate.views
    private val playerSourceInfo get() = delegate.playerSourceInfo
    private val danmakuContext = DanmakuContext.create()

    private var onlyFull = false // 仅全屏播放

    private var preparedRunQueue = mutableListOf<Pair<String, Runnable>>()

    private fun getFullMode(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        return prefs.getString(VideoSettingFragment.PLAYER_FULL_MODE, VideoSettingFragment.KEY_AUTO)!!
    }

    fun initController() = views.videoPlayer.run {
        val that = this@PlayerController
        statusBarHelper = that.statusBarHelper
        isFullHideActionBar = true
        backButton.setOnClickListener { onBackClick() }
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
        sendDanmakuView.setOnClickListener {
            val nav = Navigation.findNavController(
                activity, R.id.nav_bottom_sheet_fragment
            )
            nav.navigateToCompose(PageRoute.Player.sendDanmaku.url())
        }
        danmakuContext = that.danmakuContext
        qualityView.setOnClickListener(that::showQualityPopupMenu)
        speedView.setOnClickListener(that::showSpeedPopupMenu)
        moreBtn.setOnClickListener(that::showMoreMenu)
        setExpandButtonOnClickListener(that::showPagesOrEpisodes)
        videoPlayerCallBack = that
        setGSYVideoProgressListener(that)
        updatePlayerMode(activity.resources.configuration)
    }

    /**
     * 全屏
     */
    fun fullScreen(fullMode: String) {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.FULL
        scaffoldApp.fullScreenPlayer = true
        activity.requestedOrientation = when (fullMode) {
            // 横向全屏(自动旋转)
            VideoSettingFragment.KEY_SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            // 横向全屏(固定方向1)
            VideoSettingFragment.KEY_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // 横向全屏(固定方向2)
            VideoSettingFragment.KEY_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            // 跟随系统：不指定方向
            VideoSettingFragment.KEY_UNSPECIFIED -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            // 跟随视频：竖向视频时为不指定方向，横向视频时候为横向全屏(自动旋转)
            VideoSettingFragment.KEY_AUTO -> {
                if ((playerSourceInfo?.screenProportion ?: 1f) < 1f) {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        statusBarHelper.isShowStatus = views.videoPlayer.topContainer.visibility == View.VISIBLE
        statusBarHelper.isShowNavigation = false
    }

    /**
     * 退出全屏
     */
    fun smallScreen() {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.SMALL_TOP
        updatePlayerMode(activity.resources.configuration)
        scaffoldApp.fullScreenPlayer = false
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        statusBarHelper.isShowStatus = true
        statusBarHelper.isShowNavigation = true
    }

    fun updatePlayerMode(config: Configuration) {
        if (views.videoPlayer.mode != DanmakuVideoPlayer.PlayerMode.FULL) {
            views.videoPlayer.mode = if (config.orientation == ScaffoldView.VERTICAL) {
                DanmakuVideoPlayer.PlayerMode.SMALL_TOP
            } else {
                DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT
            }
        }
    }

    fun initDanmakuContext() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        var scaleTextSize = prefs.getString("danmaku_fontsize", "1")?.toFloatOrNull() ?: 1f
        val danmakuSpeed = prefs.getString("danmaku_speed", "1")?.toFloatOrNull() ?: 1f
        val danmakuTransparent = prefs.getInt("danmaku_transparent", 100)
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
            setDanmakuTransparency(danmakuTransparent / 100f)
//            preventOverlapping(overlappingEnablePair)
        }
        views.videoPlayer.isShowDanmaKu = danmakuShow
    }

    /**
     * 播放器是否默认全屏播放
     */
    fun checkIsPlayerDefaultFull() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        onlyFull = false
        if (scaffoldApp.orientation == ScaffoldView.VERTICAL) {
            val isPlayerVerticalDefaultFull = prefs.getBoolean(VideoSettingFragment.PLAYER_VERTICAL_DEFAULT_FULL, false)
            if (isPlayerVerticalDefaultFull) {
                val fullMode = prefs.getString(VideoSettingFragment.PLAYER_FULL_MODE, VideoSettingFragment.KEY_SENSOR_LANDSCAPE)!!
                fullScreen(fullMode)
                onlyFull = true
            }
        } else {
            val isPlayerHorizontalDefaultFull = prefs.getBoolean(VideoSettingFragment.PLAYER_HORIZONTAL_DEFAULT_FULL, false)
            if (isPlayerHorizontalDefaultFull) {
                val fullMode = prefs.getString(VideoSettingFragment.PLAYER_FULL_MODE, VideoSettingFragment.KEY_SENSOR_LANDSCAPE)!!
                fullScreen(fullMode)
                onlyFull = true
            }
        }
    }

    fun showQualityPopupMenu(view: View) {
        val sourceInfo = delegate.playerSourceInfo ?: return
        val popup = QualityPopupMenu(
            activity = activity,
            anchor = view,
            userStore = userStore,
            list = sourceInfo.acceptList,
            value = delegate.quality
        )
        popup.setOnChangedQualityListener(delegate::changedQuality)
        popup.show()
    }

    fun showSpeedPopupMenu(view: View) {
        val popup = SpeedPopupMenu(
            activity = activity,
            anchor = view,
            value = delegate.speed
        )
        popup.setOnChangedSpeedListener(delegate::changedSpeed)
        popup.show()
    }

    fun showFullModeMenu(view: View) {
        val popupMenu = PopupMenu(activity, view)
        val fullMode = getFullMode()
        val checkMenuId = when (fullMode) {
            VideoSettingFragment.KEY_SENSOR_LANDSCAPE -> R.id.full_mode_sl
            VideoSettingFragment.KEY_LANDSCAPE -> R.id.full_mode_l
            VideoSettingFragment.KEY_REVERSE_LANDSCAPE -> R.id.full_mode_rl
            VideoSettingFragment.KEY_UNSPECIFIED -> R.id.full_mode_u
            VideoSettingFragment.KEY_AUTO -> R.id.full_mode_auto
            else -> R.id.full_mode_sl
        }
        popupMenu.inflate(R.menu.player_full_mode)
        popupMenu.menu.findItem(checkMenuId).isChecked = true
        popupMenu.setOnMenuItemClickListener(this::fullModeMenuItemClick)
        popupMenu.show()
    }

    private fun fullModeMenuItemClick(item: MenuItem): Boolean {
        item.isChecked = true
        val fullMode = when (item.itemId) {
            R.id.full_mode_sl -> VideoSettingFragment.KEY_SENSOR_LANDSCAPE
            R.id.full_mode_l -> VideoSettingFragment.KEY_LANDSCAPE
            R.id.full_mode_rl -> VideoSettingFragment.KEY_REVERSE_LANDSCAPE
            R.id.full_mode_u -> VideoSettingFragment.KEY_UNSPECIFIED
            R.id.full_mode_auto -> VideoSettingFragment.KEY_AUTO
            else -> VideoSettingFragment.KEY_SENSOR_LANDSCAPE
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putString(VideoSettingFragment.PLAYER_FULL_MODE, fullMode).apply()
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

    fun showPagesOrEpisodes(view: View) {
        val playerSource = delegate.playerSource
        val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
        if (playerSource is VideoPlayerSource) {
            val pages = playerSource.pages.map {
                VideoPagesParam.Page(cid = it.cid, part = it.title)
            }
            val args = VideoPagesFragment.createArguments(
                VideoPagesParam(
                    aid = playerSource.aid,
                    pic = playerSource.coverUrl,
                    title = playerSource.title,
                    ownerId = playerSource.ownerId,
                    ownerName = playerSource.ownerName,
                    pages= pages,
                )
            )
            nav.navigate(VideoPagesFragment.actionId, args)
        }
        if (playerSource is BangumiPlayerSource) {
            val episodes = playerSource.episodes.map {
                BangumiPagesParam.Episode(
                    aid = it.aid,
                    cid = it.cid,
                    cover = it.cover,
                    ep_id = it.epid,
                    index = it.index,
                    index_title = it.index_title,
                    badge = it.badge,
                    badge_info = BangumiPagesParam.EpisodeBadgeInfo(
                        bg_color = it.badge_info.bg_color,
                        bg_color_night = it.badge_info.bg_color_night,
                        text = it.badge_info.text,
                    ),
                )
            }
            val args = BangumiPagesFragment.createArguments(
                BangumiPagesParam(
                    sid = playerSource.sid,
                    title = "",
                    episodes= episodes,
                )
            )
            nav.navigate(BangumiPagesFragment.actionId, args)
        }

    }

    private fun moreMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mini_window -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val height = playerSourceInfo?.height
                    val width = playerSourceInfo?.width
                    // 设置宽高比例值
                    var aspectRatio = if (height == null || width == null) {
                        Rational(16, 9)
                    } else {
                        Rational(width, height)
                    }
                    try {
                        delegate.picInPicHelper?.enterPictureInPictureMode(aspectRatio)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        PopTip.show("此设备不支持小窗播放")
                    }
                } else {
                    PopTip.show("小窗播放功能需要安卓8.0及以上版本")
                }
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

    /**
     * 创建弹幕
     * type: 1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕
     */
    fun createDanmaku(type: Int): BaseDanmaku {
        return danmakuContext.mDanmakuFactory.createDanmaku(type, danmakuContext)
    }

    fun onBackClick() {
        if (!scaffoldApp.fullScreenPlayer || onlyFull) {
            delegate.closePlayer()
        }
        smallScreen()
    }

    /**
     * 到准备完成后执行
     */
    fun postPrepared(id: String, action: Runnable) {
        preparedRunQueue.add(Pair(id, action))
    }

    /**
     * 准备完成
     */
    override fun onPrepared() {
        preparedRunQueue.forEach {
            val (id, action) = it
            if (id == delegate.playerSourceId) {
                views.videoPlayer.post(action)
            }
        }
        preparedRunQueue = mutableListOf()
    }

    /**
     * 播放结束
     */
    override fun onAutoCompletion() {
        delegate.historyReport()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val nextPlayerSourceInfo = delegate.playerSource?.next()
        if (nextPlayerSourceInfo is VideoPlayerSource) {
            if (prefs.getBoolean(VideoSettingFragment.PLAYER_AUTO_NEXT_VIDEO, true)) {
                delegate.openPlayer(nextPlayerSourceInfo)
                return
            }
        } else if (nextPlayerSourceInfo is BangumiPlayerSource) {
            if (prefs.getBoolean(VideoSettingFragment.PLAYER_AUTO_NEXT_BANGUMI, true)) {
                delegate.openPlayer(nextPlayerSourceInfo)
                return
            }
        }
        delegate.completionBoxController.show()
    }

    override fun onVideoPause() {
    }

    override fun onVideoResume(isResume: Boolean) {
    }

    override fun setStateAndUi(state: Int) {
        delegate.picInPicHelper?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.isInPictureInPictureMode) {
                try {
                    it.updatePictureInPictureActions(state)
                } catch (e: Exception) { }
            }
        }
        PlayerService.selfInstance?.playerState = state
        if (state >= GSYVideoView.CURRENT_STATE_PAUSE) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onVideoClose() {
        delegate.closePlayer()
    }

    override fun onProgress(
        progress: Long,
        secProgress: Long,
        currentPosition: Long,
        duration: Long
    ) {

        PlayerService.selfInstance?.setProgress(
            duration,
            currentPosition
        )
    }
}