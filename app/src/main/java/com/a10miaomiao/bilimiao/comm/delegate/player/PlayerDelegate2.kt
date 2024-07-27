package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.DisplayCutout
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.helper.PicInPicHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.dialogx.showTop
import com.a10miaomiao.bilimiao.comm.entity.player.SubtitleJsonInfo
import com.a10miaomiao.bilimiao.comm.exception.AreaLimitException
import com.a10miaomiao.bilimiao.comm.exception.DabianException
import com.a10miaomiao.bilimiao.comm.network
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.player.BilimiaoPlayerManager
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.media3.ExoMediaSourceInterceptListener
import com.a10miaomiao.bilimiao.widget.player.media3.ExoSourceManager
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.kongzue.dialogx.dialogs.PopTip
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File
import java.net.UnknownHostException


class PlayerDelegate2(
    private var activity: AppCompatActivity,
    override val di: DI,
) : BasePlayerDelegate, DIAware, ExoMediaSourceInterceptListener {

    val DEFAULT_REFERER = "https://www.bilibili.com/"
    val DEFAULT_USER_AGENT = "Bilibili Freedoooooom/MarkII"

    val views by lazy { PlayerViews(activity) }
    val controller by lazy {
        PlayerController(activity, this, playerCoroutineScope, di)
    }
    val errorMessageBoxController by lazy {
        ErrorMessageBoxController(activity, this, di)
    }
    val areaLimitBoxController by lazy {
        AreaLimitBoxController(activity, this, di)
    }
    val completionBoxController by lazy {
        CompletionBoxController(activity, this, di)
    }
    val scaffoldApp by lazy { activity.getScaffoldView() }

    var picInPicHelper: PicInPicHelper? = null
        private set

    private val userStore by instance<UserStore>()
    private val playerStore by instance<PlayerStore>()
    private val windowStore by instance<WindowStore>()
    private val themeDelegate by instance<ThemeDelegate>()

    var playerSourceInfo: PlayerSourceInfo? = null

    // 未登陆：48[480P 清晰]及以下
    // 已登陆无大会员：80[1080P 高清]及以下
    // 大会员：无限制
    val MAX_QUALITY_NOT_LOGIN = 48 // 48[480P 清晰]
    val MAX_QUALITY_NOT_VIP = 80 // 80[1080P 高清]
    var quality = 64 // 默认[高清 720P]
    var fnval = 4048 // 视频格式: 0:flv,1:mp4,4048:dash

    var speed = 1f // 播放速度
    private var lastPosition = 0L
    private val playerCoroutineScope = PlayerCoroutineScope()

    private var lastReportProgress = 0L // 最后记录的播放位置

    var playerSource: BasePlayerSource? = null
        private set(value) {
            field = value
            if (value != null) {
                playerStore.setPlayerSource(value)
            } else {
                playerStore.clearPlayerInfo()
            }
        }
    val playerSourceId get() = playerSource?.id ?: ""

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                // 播放器后台服务开启
                PlayerService.ACTION_CREATED -> {
                    PlayerService.selfInstance?.videoPlayerView = activity.findViewById(R.id.video_player)
                }
                // 播放器后台服务被杀
                PlayerService.ACTION_DESTROY -> {
                    startPlayerService()
                }
                // 耳机检测
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    //暂停播放
                    if (isPlaying())
                        views.videoPlayer.onVideoPause()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        playerCoroutineScope.onCreate()
        initPlayer()
        val intentFilter = IntentFilter().apply {
            addAction(PlayerService.ACTION_CREATED)
            addAction(PlayerService.ACTION_DESTROY)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_MEDIA_BUTTON)
        }
        registerReceiver(
            activity,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            picInPicHelper = PicInPicHelper(activity, views.videoPlayer)
        }
        controller.initController()
        views.videoPlayer.subtitleLoader = this::loadSubtitleData
        views.videoPlayer.subtitleSourceSelector = controller::getDefaultSubtitle
        //音频焦点冲突时是否释放
        views.videoPlayer.isReleaseWhenLossAudio = true

        // 主题监听
        themeDelegate.observeTheme(activity, Observer {
            val themeColor = activity.config.themeColor
            views.videoPlayer.updateThemeColor(activity, themeColor)
        })
    }

    private fun startPlayerService() {
        try {
            val intent = Intent(activity, PlayerService::class.java)
            activity.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        if (PlayerService.selfInstance == null) {
            startPlayerService()
        }
    }

    override fun onPause() {

    }

    override fun onStart() {
        if (!controller.isBackgroundPlay
            && views.videoPlayer.isInPlayingState) {
            views.videoPlayer.onVideoResume()
        }
    }

    override fun onStop() {
        if (!controller.isBackgroundPlay
            && views.videoPlayer.isInPlayingState) {
            views.videoPlayer.onVideoPause()
        }
    }

    override fun onDestroy() {
        playerCoroutineScope.onDestroy()
        activity.unregisterReceiver(broadcastReceiver)
    }

    override fun onBackPressed(): Boolean {
        if (views.videoPlayer.isLock) {
            return true
        }
        if (scaffoldApp.fullScreenPlayer) {
            controller.onBackClick()
            return true
        }
        return false
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun initPlayer() {
        BilimiaoPlayerManager.initConfig()
        ExoSourceManager.setExoMediaSourceInterceptListener(this)
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun getMediaSource(
        dataSource: String,
        preview: Boolean,
        cacheEnable: Boolean,
        isLooping: Boolean,
        cacheDir: File?
    ): MediaSource? {
        val dataSourceArr = dataSource.split("\n")
        when (dataSourceArr[0]) {
            "[local-merging]" -> {
                // 本地音视频分离
                val localSourceFactory = DefaultDataSource.Factory(activity)
                val videoMedia = MediaItem.fromUri(dataSourceArr[1])
                val audioMedia = MediaItem.fromUri(dataSourceArr[2])
                return MergingMediaSource(
                    ProgressiveMediaSource.Factory(localSourceFactory)
                        .createMediaSource(videoMedia),
                    ProgressiveMediaSource.Factory(localSourceFactory)
                        .createMediaSource(audioMedia)
                )
            }

            "[merging]" -> {
                // 音视频分离
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                val header = getDefaultRequestProperties()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                val videoMedia = MediaItem.fromUri(dataSourceArr[1])
                val audioMedia = MediaItem.fromUri(dataSourceArr[2])
                return MergingMediaSource(
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(videoMedia),
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(audioMedia)
                )
            }

            "[concatenating]" -> {
                // 视频拼接
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                val header = getDefaultRequestProperties()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                return ConcatenatingMediaSource().apply {
                    for (i in 1 until dataSourceArr.size) {
                        addMediaSource(
                            ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(dataSourceArr[i]))
                        )
                    }
                }
            }

            "[dash-mpd]" -> {
                // Create a data source factory.
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                val header = getDefaultRequestProperties()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                // Create a DASH media source pointing to a DASH manifest uri.
                val uri = Uri.parse(dataSourceArr[1])
                val dashStr = dataSourceArr[2]
                val dashManifest =
                    DashManifestParser().parse(uri, dashStr.toByteArray().inputStream())
                val mediaSource = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(dashManifest)
//                    mediaSource.prepareSource()
                return mediaSource
            }
        }
        return null
    }

    @UnstableApi
    override fun getHttpDataSourceFactory(
        userAgent: String,
        listener: TransferListener?,
        connectTimeoutMillis: Int,
        readTimeoutMillis: Int,
        mapHeadData: Map<String, String>,
        allowCrossProtocolRedirects: Boolean
    ): DataSource.Factory? {
        return null
    }

    private fun getDefaultRequestProperties(): Map<String, String> {
        val header = HashMap<String, String>()
        if (playerSource is VideoPlayerSource) {
            header["Referer"] = DEFAULT_REFERER
        }
        header["User-Agent"] = DEFAULT_USER_AGENT
        return header
    }

    internal fun historyReport(currentPosition: Long) {
        if (!userStore.isLogin()) {
            return
        }
        // 5秒记录一次
        if (currentPosition > 0 && currentPosition - lastReportProgress < 5000) {
            return
        }
        lastReportProgress = currentPosition
        activity.lifecycleScope.launch(Dispatchers.IO) {
            playerSource?.historyReport(currentPosition / 1000)
        }
    }

    private fun setThumbImageView(coverUrl: String) {
        views.videoPlayer.thumbImageView = ImageView(activity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            network(coverUrl)
        }
    }

    fun changedSpeed(newSpeed: Float) {
        if (speed != newSpeed) {
            lastPosition = views.videoPlayer.currentPositionWhenPlaying
            speed = newSpeed
            views.videoPlayer.setSpeed(speed, true)
            PopTip.show("已切换到${speed}倍速").showTop()
            playerCoroutineScope.launch(Dispatchers.IO) {
                SettingPreferences.edit(activity) {
                    it[PlayerSpeed] = newSpeed
                }
            }
        }
    }

    fun changedQuality(newQuality: Int) {
        if (quality != newQuality) {
            lastPosition = views.videoPlayer.currentPositionWhenPlaying
            quality = newQuality
            PopTip.show("正在切换清晰度").showTop()
            playerCoroutineScope.launch(Dispatchers.IO) {
                loadPlayerSource(
                    isChangedQuality = true
                )
                SettingPreferences.edit(activity) {
                    it[PlayerQuality] = newQuality
                }
            }
        }
    }

    suspend fun loadPlayerSource(
        isChangedQuality: Boolean = false
    ) {
        val source = playerSource ?: return
        try {
            source.getSubtitles()
            val danmukuParser = source.getDanmakuParser()
            val sourceInfo = source.getPlayerUrl(quality, fnval)
//                DebugMiao.log("playerSourceInfo", sourceInfo.url)
            withContext(Dispatchers.Main) {
                // 设置通知栏控制器
                PlayerService.selfInstance?.setPlayingInfo(
                    PlayerService.PlayingInfo(
                        title = source.title,
                        author = source.ownerName,
                        cover = source.coverUrl,
                        duration = sourceInfo.duration,
                    )
                )
                views.videoPlayer.releaseDanmaku()
                views.videoPlayer.danmakuParser = danmukuParser
                val header = getDefaultRequestProperties()
                views.videoPlayer.setUp(
                    sourceInfo.url,
                    false,
                    null,
                    header,
                    source.title
                )
                if (lastPosition > 0L) {
                    views.videoPlayer.seekOnStart = lastPosition
                    lastPosition = 0L
                } else if (
                    sourceInfo.lastPlayCid == source.id
                    && sourceInfo.lastPlayTime > 0L
                    && sourceInfo.lastPlayTime < sourceInfo.duration - 10000
                ) {
                    views.videoPlayer.seekOnStart = sourceInfo.lastPlayTime
                    lastPosition = 0L
                    val lastTimeStr = NumberUtil.converDuration(sourceInfo.lastPlayTime / 1000)
                    controller.postPrepared(sourceInfo.lastPlayCid) {
                        PopTip.show("自动恢复:$lastTimeStr", "重新开始")
                            .showTop()
                            .showLong()
                            .setButton { dialog, v ->
                                views.videoPlayer.startPlayLogic()
                                false
                            }
                    }
                } else if (sourceInfo.lastPlayCid == source.id) {
                    // 从进度0开始记录播放历史
                    historyReport(0L)
                }
                lastReportProgress = 0L
                views.videoPlayer.startPlayLogic()

                if (isChangedQuality) {
                    if (sourceInfo.quality == quality) {
                        PopTip.show("已切换至【${sourceInfo.description}】").showTop()
                    } else {
                        PopTip.show("清晰度切换失败").showTop()
                    }
                } else {
                    views.videoPlayer.subtitleSourceList = withContext(Dispatchers.IO) {
                        source.getSubtitles().map {
                            DanmakuVideoPlayer.SubtitleSourceInfo(
                                id = it.id,
                                lan = it.lan,
                                lan_doc = it.lan_doc,
                                subtitle_url = it.subtitle_url,
                                ai_status = it.ai_status,
                            )
                        }
                    }
                }
            }
            quality = sourceInfo.quality
            playerSourceInfo = sourceInfo

        } catch (e: DabianException) {
            withContext(Dispatchers.Main) {
                errorMessageBoxController.show("少儿不宜，禁止观看", canRetry = false)
            }
        } catch (e: AreaLimitException) {
            withContext(Dispatchers.Main) {
                (playerSource as? BangumiPlayerSource)?.let {
                    areaLimitBoxController.show(it)
                } ?: errorMessageBoxController.show("抱歉你所在的地区不可观看！")
            }
        } catch (e: UnknownHostException) {
            withContext(Dispatchers.Main) {
                errorMessageBoxController.show("无法连接到御坂网络")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                errorMessageBoxController.show(e.message ?: e.toString())
            }
        }
    }


    /**
     * 加载字幕数据
     */
    private fun loadSubtitleData(subtitleUrl: String) {
        if (subtitleUrl.isBlank()) {
            return
        }
        playerCoroutineScope.launch(Dispatchers.IO) {
            try {
                val res = MiaoHttp.request {
                    url = UrlUtil.autoHttps(subtitleUrl)
                }.awaitCall().gson<SubtitleJsonInfo>()
                views.videoPlayer.subtitleBody = res.body.map {
                    DanmakuVideoPlayer.SubtitleItemInfo(
                        from = (it.from * 1000).toLong(),
                        to = (it.to * 1000).toLong(),
                        content = it.content,
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    PopTip.show(e.message.toString()).showTop()
                }
            }
        }
    }

    /**
     * 记录播放位置
     */
    fun reloadPlayer() {
        lastPosition = views.videoPlayer.currentPositionWhenPlaying
        playerCoroutineScope.launch(Dispatchers.IO) {
            loadPlayerSource()
        }
    }

    override fun openPlayer(source: BasePlayerSource) {
        completionBoxController.hide()
        errorMessageBoxController.hide()
        areaLimitBoxController.hide()
        lastPosition = 0L
        if (playerSource != null) {
            playerCoroutineScope.onDestroy()
            views.videoPlayer.release()
        }
        playerCoroutineScope.onCreate()
        playerCoroutineScope.launch(Dispatchers.IO) {
            SettingPreferences.getData(activity) {
                fnval = it[PlayerFnval] ?: SettingConstants.PLAYER_FNVAL_DASH
                quality = it[PlayerQuality] ?: 64
                if (!userStore.isLogin() && quality > MAX_QUALITY_NOT_LOGIN) {
                    // 未登陆：48[480P 清晰]及以下
                    quality = MAX_QUALITY_NOT_LOGIN
                } else if (!userStore.isVip() && quality > MAX_QUALITY_NOT_VIP) {
                    // 无大会员：80[1080P 高清]及以下
                    quality = MAX_QUALITY_NOT_VIP
                }
                speed = it[PlayerSpeed] ?: 1f
            }
            loadPlayerSource()
            withContext(Dispatchers.Main) {
                views.videoPlayer.setSpeed(speed, true)
                // 播放倍速提示
                if (speed != 1f) {
                    PopTip.show("注意，当前为${speed}倍速").showTop()
                }
            }
        }
        scaffoldApp.showPlayer = true
        playerSource = source
        setThumbImageView(source.coverUrl)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 播放器是否默认全屏播放
        controller.checkIsPlayerDefaultFull()
        if (source is VideoPlayerSource && source.pages.size > 1) {
            views.videoPlayer.setExpandButtonText("分P")
            views.videoPlayer.showExpandButton()
        } else if (source is BangumiPlayerSource && source.episodes.size > 1) {
            views.videoPlayer.setExpandButtonText("剧集")
            views.videoPlayer.showExpandButton()
        } else {
            views.videoPlayer.hideExpandButton()
        }
    }

    override fun closePlayer() {
        scaffoldApp.showPlayer = false
        playerCoroutineScope.onDestroy()
        playerSource = null
        playerSourceInfo = null

        views.videoPlayer.release()
        views.videoPlayer.hideExpandButton()
        lastPosition = 0L

        // 设置通知栏控制器
        PlayerService.selfInstance?.clearPlayingInfo()

        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun isPlaying(): Boolean {
        return views.videoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PLAYING ||
                views.videoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PREPAREING ||
                views.videoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START
    }

    override fun isPause(): Boolean {
        return views.videoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PAUSE
    }

    override fun setWindowInsets(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        displayCutout: DisplayCutout?
    ) {
        views.videoPlayer.setWindowInsets(left, top, right, bottom, displayCutout)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            picInPicHelper?.onPictureInPictureModeChanged(isInPictureInPictureMode)
            if (isInPictureInPictureMode) { // 进入画中画模式，则隐藏其它控件
                // 隐藏视频控制器
                views.videoPlayer.hideController()
                //
                views.videoPlayer.isPicInPicMode = true
                // 视频组件全屏
                scaffoldApp.fullScreenPlayer = true
                // 调整弹幕样式，调小字体，限制行数
            } else {
                scaffoldApp.fullScreenPlayer =
                    views.videoPlayer.mode == DanmakuVideoPlayer.PlayerMode.FULL
                views.videoPlayer.isPicInPicMode = false
            }
            playerCoroutineScope.launch {
                SettingPreferences.getData(activity) {
                    controller.initVideoSetting(it)
                    controller.initDanmakuContext(it)
                }
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        controller.updatePlayerMode(newConfig)
    }

    override fun getSourceIds(): PlayerSourceIds {
        return playerSource?.getSourceIds() ?: PlayerSourceIds()
    }

    override fun currentPosition(): Long {
        return views.videoPlayer.currentPosition
    }

    override fun sendDanmaku(
        type: Int,
        danmakuText: String,
        danmakuTextSize: Float,
        danmakuTextColor: Int,
        danmakuPosition: Long,
    ) {
        val dispDensity = activity.resources.displayMetrics.density
        val danmaku = controller.createDanmaku(type).apply {
            text = danmakuText
            textColor = danmakuTextColor
            textSize = danmakuTextSize * (dispDensity - 0.6f);
            time = danmakuPosition
            borderColor = 0xFFFFFFFF.toInt()
        }
        views.videoPlayer.addDanmaku(danmaku)
        if (!isPlaying()) {
            views.videoPlayer.onVideoResume()
        }
    }

    override fun setProxy(proxyServer: ProxyServerInfo, uposHost: String) {
        playerSource?.let {
            it.proxyServer = proxyServer
            it.uposHost = uposHost
            openPlayer(it)
        }
    }

    fun getVideoRatio(): Float? {
        return playerSourceInfo?.screenProportion
    }

    fun setHoldStatus(isHold: Boolean) {
        views.videoPlayer.setHoldStatus(isHold)
        completionBoxController.setHoldStatus(isHold)
    }
}