package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.util.Rational
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.dialogx.showTop
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesFragment
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesParam
import com.a10miaomiao.bilimiao.page.setting.DanmakuSettingFragment
import com.a10miaomiao.bilimiao.page.setting.VideoSettingFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesParam
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import com.kongzue.dialogx.dialogs.PopTip
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

    private fun getFullMode(): String {
        return prefs.getString(
            VideoSettingFragment.PLAYER_FULL_MODE,
            VideoSettingFragment.KEY_AUTO
        )!!
    }

    private fun getScreenType(): Int {
        return prefs.getInt(
            VideoSettingFragment.PLAYER_SCREEN_TYPE,
            GSYVideoType.SCREEN_TYPE_DEFAULT
        )
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
        danmakuContext = that.danmakuContext

        qualityView.setOnClickListener(that::showQualityPopupMenu)
        speedView.setOnClickListener(that::showSpeedPopupMenu)
        moreBtn.setOnClickListener(that::showMoreMenu)
        setDanmakuSwitchOnClickListener(that::danmakuSwitchClick)
        setExpandButtonOnClickListener(that::showPagesOrEpisodes)
        setSendDanmakuButtonOnClickListener(that::showSendDanmakuPage)
        serHoldUpButtonOnClickListener(that::holdUpPlayer)
        videoPlayerCallBack = that
        setGSYVideoProgressListener(that)
        updatePlayerMode(activity.resources.configuration)

        GSYVideoType.setShowType(getScreenType())
        if (!prefs.getBoolean(DanmakuSettingFragment.KEY_DANMAKU_SYS_FONT, false)) {
            that.danmakuContext.setTypeface(
                Typeface.createFromAsset(
                    activity.assets,
                    "fonts/danmaku.ttf"
                )
            )
        }
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

        initDanmakuContext()
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

        initDanmakuContext()
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
        val mode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.isInPictureInPictureMode
        ) {
            DanmakuSettingFragment.MODE_PIC_IN_PIC
        } else when (views.videoPlayer.mode) {
            DanmakuVideoPlayer.PlayerMode.SMALL_TOP -> DanmakuSettingFragment.MODE_SMALL
            DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT -> DanmakuSettingFragment.MODE_SMALL
            DanmakuVideoPlayer.PlayerMode.FULL -> DanmakuSettingFragment.MODE_FULL
        }

        val danmakuShow = prefs.getBoolean(DanmakuSettingFragment.KEY_DANMAKU_SHOW, true) &&
                prefs.getBoolean(
                    DanmakuSettingFragment.generateKey(
                        DanmakuSettingFragment.KEY_DANMAKU_SHOW, mode
                    ),
                    true
                )
        views.videoPlayer.isShowDanmaKu = danmakuShow

        // 滚动弹幕显示
        val danmakuR2LShow = prefs.getBoolean(
            DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_R2L_SHOW, mode),
            true
        )
        // 顶部弹幕显示
        val danmakuFTShow = prefs.getBoolean(
            DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_FT_SHOW, mode),
            true
        )
        // 底部弹幕显示
        val danmakuFBShow = prefs.getBoolean(
            DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_FB_SHOW, mode),
            true
        )
        // 高级弹幕显示
        val danmakuSpecialShow = prefs.getBoolean(
            DanmakuSettingFragment.generateKey(
                DanmakuSettingFragment.KEY_DANMAKU_SPECIAL_SHOW,
                mode
            ),
            true
        )
        // 字体大小
        var scaleTextSize = prefs.getString(
            DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_FONTSIZE, mode),
            "1"
        )?.toFloatOrNull() ?: 1f
        // 弹幕速度
        val danmakuSpeed = prefs.getString(
            DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_SPEED, mode),
            "1"
        )?.toFloatOrNull() ?: 1f
        // 字体透明度
        val danmakuTransparent = prefs.getInt(
            DanmakuSettingFragment.generateKey(
                DanmakuSettingFragment.KEY_DANMAKU_TRANSPARENT,
                mode
            ),
            100
        )

        // 滚动弹幕最大行数
        val danmakuR2LMaxLine = prefs.getInt(
            DanmakuSettingFragment.generateKey(
                DanmakuSettingFragment.KEY_DANMAKU_R2L_MAX_LINE,
                mode
            ), 0
        ).let { if (it > 0) it else null }
        // 顶部弹幕最大行数
        val danmakuFTMaxLine = prefs.getInt(
            DanmakuSettingFragment.generateKey(
                DanmakuSettingFragment.KEY_DANMAKU_FT_MAX_LINE,
                mode
            ),0
        ).let { if (it > 0) it else null }
        // 底部弹幕最大行数
        val danmakuFBMaxLine = prefs.getInt(
            DanmakuSettingFragment.generateKey(
                DanmakuSettingFragment.KEY_DANMAKU_FB_MAX_LINE,
                mode
            ),0
        ).let { if (it > 0) it else null }
        // 设置最大显示行数
        val maxLinesPair = mapOf(
            BaseDanmaku.TYPE_SCROLL_RL to danmakuR2LMaxLine,
            BaseDanmaku.TYPE_FIX_TOP to danmakuFTMaxLine,
            BaseDanmaku.TYPE_FIX_BOTTOM to danmakuFBMaxLine,
        )

        //设置弹幕样式
        danmakuContext?.apply {

            ftDanmakuVisibility = danmakuFTShow
            fbDanmakuVisibility = danmakuFBShow
            r2LDanmakuVisibility = danmakuR2LShow
            specialDanmakuVisibility = danmakuSpecialShow
            setScrollSpeedFactor(danmakuSpeed)
            setScaleTextSize(scaleTextSize)
            setMaximumLines(maxLinesPair)
            setDanmakuTransparency(danmakuTransparent / 100f)
        }
    }

    private fun danmakuSwitchClick(view: View) {
        val show = !views.videoPlayer.isShowDanmaKu
        if (show) {
            val mode = when (views.videoPlayer.mode) {
                DanmakuVideoPlayer.PlayerMode.SMALL_TOP -> DanmakuSettingFragment.MODE_SMALL
                DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT -> DanmakuSettingFragment.MODE_SMALL
                DanmakuVideoPlayer.PlayerMode.FULL -> DanmakuSettingFragment.MODE_FULL
            }
            val showInCurMode = prefs.getBoolean(
                DanmakuSettingFragment.generateKey(
                    DanmakuSettingFragment.KEY_DANMAKU_SHOW, mode
                ),
                true
            )
            if (showInCurMode) {
                views.videoPlayer.isShowDanmaKu = true
                DanmakuSettingFragment.setDanmaKuShow(activity, true)
            } else {
                PopTip.show("当前模式的弹幕已关闭，请手动打开", "打开")
                    .showTop()
                    .setButton{_, _ ->
                        prefs.edit().also {
                            it.putBoolean(
                                DanmakuSettingFragment.generateKey(DanmakuSettingFragment.KEY_DANMAKU_SHOW, mode),
                                true,
                            )
                            it.putBoolean(
                                DanmakuSettingFragment.KEY_DANMAKU_SHOW,
                                true,
                            )
                        }.apply()
                        views.videoPlayer.isShowDanmaKu = true
                        false
                    }
            }
        } else {
            views.videoPlayer.isShowDanmaKu = false
            DanmakuSettingFragment.setDanmaKuShow(activity, false)
        }
    }

    fun initVideoPlayerSetting() {
        views.videoPlayer.showBottomProgressBarInFullMode = prefs.getBoolean(
            VideoSettingFragment.PLAYER_FULL_SHOW_BOTTOM_PROGRESS_BAR,
            true
        )
        views.videoPlayer.showBottomProgressBarInSmallMode = prefs.getBoolean(
            VideoSettingFragment.PLAYER_SMALL_SHOW_BOTTOM_PROGRESS_BAR,
            true
        )
    }

    /**
     * 播放器是否默认全屏播放
     */
    fun checkIsPlayerDefaultFull() {
        onlyFull = false
        if (scaffoldApp.orientation == ScaffoldView.VERTICAL) {
            val isPlayerVerticalDefaultFull =
                prefs.getBoolean(VideoSettingFragment.PLAYER_VERTICAL_DEFAULT_FULL, false)
            if (isPlayerVerticalDefaultFull) {
                val fullMode = prefs.getString(
                    VideoSettingFragment.PLAYER_FULL_MODE,
                    VideoSettingFragment.KEY_SENSOR_LANDSCAPE
                )!!
                fullScreen(fullMode)
                onlyFull = true
            }
        } else {
            val isPlayerHorizontalDefaultFull =
                prefs.getBoolean(VideoSettingFragment.PLAYER_HORIZONTAL_DEFAULT_FULL, false)
            if (isPlayerHorizontalDefaultFull) {
                val fullMode = prefs.getString(
                    VideoSettingFragment.PLAYER_FULL_MODE,
                    VideoSettingFragment.KEY_SENSOR_LANDSCAPE
                )!!
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
        val checkMenuId = when (GSYVideoType.getShowType()) {
            GSYVideoType.SCREEN_TYPE_DEFAULT -> R.id.scale_1
            GSYVideoType.SCREEN_TYPE_16_9 -> R.id.scale_2
            GSYVideoType.SCREEN_TYPE_4_3 -> R.id.scale_3
            GSYVideoType.SCREEN_TYPE_FULL -> R.id.scale_4
            GSYVideoType.SCREEN_MATCH_FULL -> R.id.scale_5
            else -> R.id.scale_1
        }
        popupMenu.menu.findItem(checkMenuId).isChecked = true
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
                    pages = pages,
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
                    episodes = episodes,
                )
            )
            nav.navigate(BangumiPagesFragment.actionId, args)
        }

    }

    private fun showSendDanmakuPage(view: View) {
        if (!userStore.isLogin()) {
            PopTip.show("请先登录")
            return
        }
        if (
            views.videoPlayer.mode == DanmakuVideoPlayer.PlayerMode.FULL
            && delegate.isPlaying()
        ) {
            views.videoPlayer.onVideoPause()
            views.videoPlayer.hideController()
        }
        val nav = Navigation.findNavController(
            activity, R.id.nav_bottom_sheet_fragment
        )
        nav.navigateToCompose(SendDanmakuPage())
    }

    fun holdUpPlayer(view: View) {
        scaffoldApp.holdUpPlayer()
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
                nav.navigate(VideoSettingFragment.actionId)
            }
            R.id.danmuku_setting -> {
                val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
                val tabIndex = if (scaffoldApp.fullScreenPlayer) 1 else 0
                val args = DanmakuSettingFragment.createArguments(tabIndex)
                nav.navigate(DanmakuSettingFragment.actionId, args)
            }
            R.id.scale_1,
            R.id.scale_2,
            R.id.scale_3,
            R.id.scale_4,
            R.id.scale_5 -> {
                val type = when(item.itemId) {
                    R.id.scale_1 -> GSYVideoType.SCREEN_TYPE_DEFAULT
                    R.id.scale_2 -> GSYVideoType.SCREEN_TYPE_16_9
                    R.id.scale_3 -> GSYVideoType.SCREEN_TYPE_4_3
                    R.id.scale_4 -> GSYVideoType.SCREEN_TYPE_FULL
                    R.id.scale_5 -> GSYVideoType.SCREEN_MATCH_FULL
                    else -> GSYVideoType.SCREEN_TYPE_DEFAULT
                }
                GSYVideoType.setShowType(type)
                views.videoPlayer.updateTextureViewShowType()
                PreferenceManager.getDefaultSharedPreferences(activity).edit {
                    putInt(VideoSettingFragment.PLAYER_SCREEN_TYPE, type)
                }
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
        delegate.historyReport(views.videoPlayer.currentPosition)
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
                } catch (e: Exception) {
                }
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

    override fun onClickUiToggle(e: MotionEvent?) {
        if (scaffoldApp.playerViewSizeStatus != ScaffoldView.PlayerViewSizeStatus.NORMAL) {
            scaffoldApp.playerViewSizeStatus = ScaffoldView.PlayerViewSizeStatus.NORMAL
        }
    }

    override fun onProgress(
        progress: Long,
        secProgress: Long,
        currentPosition: Long,
        duration: Long
    ) {
        delegate.historyReport(currentPosition)
        PlayerService.selfInstance?.setProgress(
            duration,
            currentPosition
        )
    }
}