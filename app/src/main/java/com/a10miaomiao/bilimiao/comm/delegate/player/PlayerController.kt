package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.util.Rational
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.dialogx.showTop
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesFragment
import com.a10miaomiao.bilimiao.page.bangumi.BangumiPagesParam
import com.a10miaomiao.bilimiao.page.video.VideoPagesFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesParam
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.kongzue.dialogx.dialogs.PopTip
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class PlayerController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    private val scope: CoroutineScope,
    override val di: DI,
) : DIAware, VideoPlayerCallBack, GSYVideoProgressListener {

    private val userStore by instance<UserStore>()
    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()
    private val statusBarHelper by instance<StatusBarHelper>()
    private val scaffoldApp get() = delegate.scaffoldApp
    private val views get() = delegate.views
    private val playerSourceInfo get() = delegate.playerSourceInfo
    private val danmakuContext = DanmakuContext.create()

    private var onlyFull = false // 仅全屏播放
    private var showSubtitle = false // 默认显示字幕
    private var showAiSubtitle = true // 默认显示AI字幕

    private var preparedRunQueue = mutableListOf<Pair<String, Runnable>>()
    private fun currentDanmakuMode(): SettingPreferences.Danmaku {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.isInPictureInPictureMode
        ) {
            return SettingPreferences.DanmakuPipMode
        }
        return when (views.videoPlayer.mode) {
            DanmakuVideoPlayer.PlayerMode.SMALL_TOP -> SettingPreferences.DanmakuSmallMode
            DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT -> SettingPreferences.DanmakuSmallMode
            DanmakuVideoPlayer.PlayerMode.FULL -> SettingPreferences.DanmakuFullMode
        }
    }

    private fun getFullMode(preferences: Preferences): Int {
        return preferences[SettingPreferences.PlayerFullMode]
            ?: SettingConstants.PLAYER_FULL_MODE_AUTO
    }

    fun initController() = views.videoPlayer.run {
        val that = this@PlayerController
        statusBarHelper = that.statusBarHelper
        isFullHideActionBar = true
        backButton.setOnClickListener { onBackClick() }
        setIsTouchWiget(true)
        fullscreenButton.setOnClickListener {
            scope.launch {
                if (scaffoldApp.fullScreenPlayer) {
                    smallScreen()
                } else {
                    val fullMode = SettingPreferences.mapData(activity) {
                        getFullMode(it)
                    }
                    fullScreen(fullMode)
                }
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
        setSendDanmakuButtonOnLongClickListener {
            danmakuSwitchClick(it)
            true
        }
        serHoldUpButtonOnClickListener(that::holdUpPlayer)
        videoPlayerCallBack = that
        setGSYVideoProgressListener(that)
        updatePlayerMode(activity.resources.configuration)
        scope.launch {
            initPlayerSetting()
        }

        // 无障碍适配
        contentDescription = "播放窗口"
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                when (eventType) {
                    AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
                        showController()
                    }
                }
            }
        }
    }

    /**
     * 全屏
     */
    fun fullScreen(fullMode: Int) {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.FULL
        scaffoldApp.fullScreenPlayer = true
        activity.requestedOrientation = when (fullMode) {
            // 横向全屏(自动旋转)
            SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            // 横向全屏(固定方向1)
            SettingConstants.PLAYER_FULL_MODE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // 横向全屏(固定方向2)
            SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            // 跟随系统：不指定方向
            SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            // 跟随视频：竖向视频时为不指定方向，横向视频时候为横向全屏(自动旋转)
            SettingConstants.PLAYER_FULL_MODE_AUTO -> {
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

        scope.launch {
            SettingPreferences.getData(activity) {
                initVideoSetting(it)
                initDanmakuContext(it)
            }
        }
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

        scope.launch {
            SettingPreferences.getData(activity) {
                initVideoSetting(it)
                initDanmakuContext(it)
            }
        }
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

    private suspend fun initPlayerSetting() {
        SettingPreferences.getData(activity) {
            GSYVideoType.setShowType(
                it[PlayerScreenType] ?: GSYVideoType.SCREEN_TYPE_DEFAULT
            )
            if (it[DanmakuSysFont] != true) {
                danmakuContext.setTypeface(
                    Typeface.createFromAsset(
                        activity.assets,
                        "fonts/danmaku.ttf"
                    )
                )
            }
        }
        SettingPreferences.run {
            activity.dataStore.data.collect {
                initVideoSetting(it)
                initDanmakuContext(it)
            }
        }
    }

    fun initDanmakuContext(
        preferences: Preferences
    ) {
        val danmakuMode = currentDanmakuMode().let {
            if (preferences[it.enable] == true) {
                it
            } else {
                SettingPreferences.DanmakuDefault
            }
        }
        val danmakuShow = (preferences[SettingPreferences.DanmakuEnable] ?: true) &&
                (preferences[danmakuMode.show] ?: true)
        views.videoPlayer.isShowDanmaku = danmakuShow

        // 滚动弹幕显示
        val danmakuR2LShow = preferences[danmakuMode.r2lShow] ?: true
        // 顶部弹幕显示
        val danmakuFTShow = preferences[danmakuMode.ftShow] ?: true
        // 底部弹幕显示
        val danmakuFBShow = preferences[danmakuMode.fbShow] ?: true
        // 高级弹幕显示
        val danmakuSpecialShow = preferences[danmakuMode.specialShow] ?: true
        // 字体大小
        var scaleTextSize = preferences[danmakuMode.fontSize] ?: 1f
        // 弹幕速度
        val danmakuSpeed = preferences[danmakuMode.speed] ?: 1f
        // 字体不透明度
        val danmakuOpacity = preferences[danmakuMode.opacity] ?: 1f

        // 滚动弹幕最大行数
        val danmakuR2LMaxLine = preferences[danmakuMode.r2lMaxLine].let {
            if (it == null || it == 0) null else it
        }
        // 顶部弹幕最大行数
        val danmakuFTMaxLine = preferences[danmakuMode.ftMaxLine].let {
            if (it == null || it == 0) null else it
        }
        // 底部弹幕最大行数
        val danmakuFBMaxLine = preferences[danmakuMode.fbMaxLine].let {
            if (it == null || it == 0) null else it
        }
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
            setScrollSpeedFactor(1 / danmakuSpeed)
            setScaleTextSize(scaleTextSize)
            setMaximumLines(maxLinesPair)
            setDanmakuTransparency(danmakuOpacity)
        }
    }

    private fun danmakuSwitchClick(view: View) {
        scope.launch {
            val danmakuMode = currentDanmakuMode()
            val isEnable = SettingPreferences.mapData(activity) {
                it[DanmakuEnable] ?: true
            }
            if (isEnable) {
                val show = !views.videoPlayer.isShowDanmaku
                views.videoPlayer.isShowDanmaku = show
                SettingPreferences.edit(activity) {
                    it[DanmakuDefault.show] = show
                    it[danmakuMode.show] = show
                }
            } else {
                PopTip.show("弹幕功能已关闭，请手动打开", "打开")
                    .showTop()
                    .setButton { _, _ ->
                        scope.launch {
                            SettingPreferences.edit(activity) {
                                it[DanmakuEnable] = true
                                it[DanmakuDefault.show] = true
                                it[danmakuMode.show] = true
                            }
                        }
                        views.videoPlayer.isShowDanmaku = true
                        false
                    }
            }
        }
    }

    fun initVideoSetting(preferences: Preferences) {
        val show = SettingPreferences.run {
            preferences[PlayerBottomProgressBarShow] ?: 0
        }
        views.videoPlayer.showBottomProgressBarInSmallMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL != 0
        )
        views.videoPlayer.showBottomProgressBarInFullMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL != 0
        )
        views.videoPlayer.showBottomProgressBarInPipMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP != 0
        )
        views.videoPlayer.enabledAudioFocus = SettingPreferences.run {
            preferences[PlayerAudioFocus] ?: true
        }
        showSubtitle = preferences[SettingPreferences.PlayerSubtitleShow] ?: true
        showAiSubtitle = preferences[SettingPreferences.PlayerAiSubtitleShow] ?: false
    }

    /**
     * 播放器是否默认全屏播放
     */
    fun checkIsPlayerDefaultFull() = scope.launch {
        onlyFull = false
        val (openMode, fullMode) = SettingPreferences.mapData(activity)  {
            val openMode = it[PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
            openMode to getFullMode(it)
        }
        if (scaffoldApp.orientation == ScaffoldView.VERTICAL
            && openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN != 0) {
            fullScreen(fullMode)
            onlyFull = true
        } else if (scaffoldApp.orientation == ScaffoldView.HORIZONTAL
            && openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE != 0
        ){
            fullScreen(fullMode)
            onlyFull = true
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
        scope.launch {
            val speedValueSets = SettingPreferences.mapData(activity) {
                it[PlayerSpeedValues] ?: SettingConstants.PLAYER_SPEED_SETS
            }
            val popup = SpeedPopupMenu(
                activity = activity,
                anchor = view,
                value = delegate.speed,
                list = speedValueSets.map { it.toFloat() },
            )
            popup.setOnChangedSpeedListener(delegate::changedSpeed)
            popup.show()
        }
    }

    fun showFullModeMenu(view: View) {
        val fullModeMenuItemClick = this::fullModeMenuItemClick
        scope.launch {
            val popupMenu = PopupMenu(activity, view)
            val fullMode = SettingPreferences.mapData(activity) {
                it[PlayerFullMode] ?: SettingConstants.PLAYER_FULL_MODE_AUTO
            }
            val checkMenuId = when (fullMode) {
                SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE -> R.id.full_mode_sl
                SettingConstants.PLAYER_FULL_MODE_LANDSCAPE -> R.id.full_mode_l
                SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE -> R.id.full_mode_rl
                SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED -> R.id.full_mode_u
                SettingConstants.PLAYER_FULL_MODE_AUTO -> R.id.full_mode_auto
                else -> SettingConstants.PLAYER_FULL_MODE_AUTO
            }
            popupMenu.inflate(R.menu.player_full_mode)
            popupMenu.menu.findItem(checkMenuId).isChecked = true
            popupMenu.setOnMenuItemClickListener(fullModeMenuItemClick)
            popupMenu.show()
        }
    }

    private fun fullModeMenuItemClick(item: MenuItem): Boolean {
        item.isChecked = true
        val fullMode = when (item.itemId) {
            R.id.full_mode_sl -> SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE
            R.id.full_mode_l -> SettingConstants.PLAYER_FULL_MODE_LANDSCAPE
            R.id.full_mode_rl -> SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE
            R.id.full_mode_u -> SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED
            R.id.full_mode_auto -> SettingConstants.PLAYER_FULL_MODE_AUTO
            else -> SettingConstants.PLAYER_FULL_MODE_AUTO
        }
        if (scaffoldApp.fullScreenPlayer) {
            fullScreen(fullMode)
        }
        scope.launch {
            SettingPreferences.edit(activity) {
                it[PlayerFullMode] = fullMode
            }
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
                nav.navigateToCompose(VideoSettingPage())
            }

            R.id.danmuku_setting -> {
                val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
                val tabName = if (scaffoldApp.fullScreenPlayer){
                    SettingPreferences.DanmakuFullMode.name
                } else {
                    SettingPreferences.DanmakuSmallMode.name
                }
                nav.navigateToCompose(DanmakuDisplaySettingPage()) {
                    name set tabName
                }
            }

            R.id.scale_1,
            R.id.scale_2,
            R.id.scale_3,
            R.id.scale_4,
            R.id.scale_5 -> {
                val type = when (item.itemId) {
                    R.id.scale_1 -> GSYVideoType.SCREEN_TYPE_DEFAULT
                    R.id.scale_2 -> GSYVideoType.SCREEN_TYPE_16_9
                    R.id.scale_3 -> GSYVideoType.SCREEN_TYPE_4_3
                    R.id.scale_4 -> GSYVideoType.SCREEN_TYPE_FULL
                    R.id.scale_5 -> GSYVideoType.SCREEN_MATCH_FULL
                    else -> GSYVideoType.SCREEN_TYPE_DEFAULT
                }
                GSYVideoType.setShowType(type)
                views.videoPlayer.updateTextureViewShowType()
                scope.launch {
                    SettingPreferences.edit(activity) {
                        it[PlayerScreenType] = type
                    }
                }
            }
        }
        return true
    }

    /**
     * 获取默认字幕
     */
    fun getDefaultSubtitle(
        list: List<DanmakuVideoPlayer.SubtitleSourceInfo>
    ): DanmakuVideoPlayer.SubtitleSourceInfo? {
        if (showSubtitle) {
            return list.find { showAiSubtitle || it.ai_status == 0 }
        }
        return null
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
        scope.launch {
            val currentPlayerSourceInfo = delegate.playerSource ?: return@launch
            val nextPlayerSourceInfo = currentPlayerSourceInfo.next()
            val (order, orderRandom) = SettingPreferences.mapData(activity) {
                val order = it[PlayerOrder] ?: SettingConstants.PLAYER_ORDER_DEFAULT
                val orderRandom = it[PlayerOrderRandom] ?: false
                order to orderRandom
            }
            // 循环播放
            val isLoop = order and SettingConstants.PLAYER_ORDER_LOOP != 0
            if (nextPlayerSourceInfo is VideoPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_P != 0) {
                // 自动播放下一P
                delegate.openPlayer(nextPlayerSourceInfo)
                return@launch
            } else if (nextPlayerSourceInfo is BangumiPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_EPISODE != 0) {
                // 自动播放下一集
                delegate.openPlayer(nextPlayerSourceInfo)
                return@launch
            }
            if (order and SettingConstants.PLAYER_ORDER_NEXT_VIDEO != 0) {
                // 自动下一个视频
                val nextVideo = playerStore.nextVideo(
                    orderRandom, isLoop
                )
                if (nextVideo != null) {
                    delegate.openPlayer(nextVideo.toVideoPlayerSource())
                    return@launch
                }
            }
            if (isLoop) {
                // 单个视频循环
                delegate.openPlayer(currentPlayerSourceInfo)
            } else {
                delegate.completionBoxController.show()
            }
        }
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
        scaffoldApp.animatePlayerHeight(scaffoldApp.smallModePlayerMaxHeight)
    }

    private var lastRecordedPosition = 0L
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

        //定时关闭
        val autoStopDuration = playerStore.autoStopDuration
        if (autoStopDuration != 0) {
            val passedTime = (currentPosition - lastRecordedPosition) / 1000
            if (passedTime in 0L..5L) {
                var remainTimeNew = autoStopDuration - passedTime.toInt()
                if (remainTimeNew <= 0) {
                    //时间被消耗完，暂停。
                    remainTimeNew = 0
                    delegate.views.videoPlayer.onVideoPause()
                }
                playerStore.setAutoStopDuration(remainTimeNew)
            }
            lastRecordedPosition = currentPosition
        }
    }
}