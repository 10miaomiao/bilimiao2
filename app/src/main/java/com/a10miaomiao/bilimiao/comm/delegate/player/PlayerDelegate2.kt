package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.BroadcastReceiver
import android.content.ComponentName
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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.player.BilimiaoPlayerManager
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlaybackService
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.media3.ExoMediaSourceInterceptListener
import com.a10miaomiao.bilimiao.widget.player.media3.ExoSourceManager
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.google.common.util.concurrent.MoreExecutors
import com.kongzue.dialogx.dialogs.PopTip
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
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
    val loadingBoxController by lazy {
        LoadingBoxController(activity, this)
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
    private var lastBackPressedTime = 0L

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
            val themeColor = it.toInt()
            views.videoPlayer.updateThemeColor(activity, themeColor)
        })

        if (isPlaying()) {
            loadingBoxController.hideLoading()
            areaLimitBoxController.hide()
            errorMessageBoxController.hide()
            completionBoxController.hide()
        }
    }

    private fun startPlaybackService() {
        val instance = PlaybackService.instance
        if (instance == null) {
            val sessionToken = SessionToken(
                activity,
                ComponentName(activity, PlaybackService::class.java)
            )
            val mediaControllerFuture = MediaController.Builder(activity, sessionToken).buildAsync()
            mediaControllerFuture.addListener( {
                PlaybackService.instance?.setPlayerDelegate(this@PlayerDelegate2)
            }, MoreExecutors.directExecutor())
        } else {
            instance.setPlayerDelegate(this)
        }
    }

    override fun onResume() {
        startPlaybackService()
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
        if (scaffoldApp.showPlayer) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressedTime > 2000) {
                PopTip.show("再按一次退出播放")
                lastBackPressedTime = now
            } else {
                closePlayer()
                lastBackPressedTime = 0
            }
            return true
        }
        return false
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun initPlayer() {
        BilimiaoPlayerManager.initConfig()
        GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
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
        val mediaMetadata = getMediaMetadata(dataSource)
        val header = playerSourceInfo?.header ?: emptyMap()
        return when (dataSourceArr[0]) {
            "[local-merging]" -> {
                // 本地音视频分离
                val localSourceFactory = DefaultDataSource.Factory(activity)
                val videoMedia = MediaItem.Builder().apply {
                    setUri(dataSourceArr[1])
                    mediaMetadata?.let(::setMediaMetadata)
                }.build()
                val audioMedia = MediaItem.Builder().apply {
                    setUri(dataSourceArr[2])
                    mediaMetadata?.let(::setMediaMetadata)
                }.build()
                MergingMediaSource(
                    ProgressiveMediaSource.Factory(localSourceFactory)
                        .createMediaSource(videoMedia),
                    ProgressiveMediaSource.Factory(localSourceFactory)
                        .createMediaSource(audioMedia)
                )
            }

            "[merging]" -> {
                // 音视频分离
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                val videoMedia = MediaItem.Builder().apply {
                    setUri(dataSourceArr[1])
                    mediaMetadata?.let(::setMediaMetadata)
                }.build()
                val audioMedia = MediaItem.Builder().apply {
                    setUri(dataSourceArr[2])
                    mediaMetadata?.let(::setMediaMetadata)
                }.build()
                MergingMediaSource(
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(videoMedia),
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(audioMedia)
                )
            }

            "[concatenating]" -> {
                // 视频拼接
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                ConcatenatingMediaSource().apply {
                    for (i in 1 until dataSourceArr.size) {
                        val mediaItem = MediaItem.Builder().apply {
                            setUri(dataSourceArr[i])
                            mediaMetadata?.let(::setMediaMetadata)
                        }.build()
                        addMediaSource(
                            ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem)
                        )
                    }
                }
            }

            "[dash-mpd]" -> {
                // Create a data source factory.
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
                dataSourceFactory.setDefaultRequestProperties(header)
                // Create a DASH media source pointing to a DASH manifest uri.
                val uri = Uri.parse(dataSourceArr[1])
                val dashStr = dataSourceArr[2]
                val dashManifest =
                    DashManifestParser().parse(uri, dashStr.toByteArray().inputStream())
                val mediaSource = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(dashManifest)
                mediaMetadata?.let {
                    mediaSource.updateMediaItem(
                        MediaItem.Builder()
                            .setMediaMetadata(it)
                            .build()
                    )
                }
                mediaSource
            }
            else -> {
                return null
            }
        }
    }

    override fun getMediaMetadata(dataSource: String): MediaMetadata? {
        return playerSource?.let {
            val artworkUri = Uri.parse(UrlUtil.autoHttps(it.coverUrl))
            val metaData = MediaMetadata.Builder()
                .setTitle(it.title)
                .setArtworkUri(artworkUri)
                .setAlbumTitle(it.ownerName)
                .build()
            return metaData
        } ?: null
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

    internal fun historyReport(currentPosition: Long) {
//        if (!userStore.isLogin()) {
//            return
//        }
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
            playerCoroutineScope.launch(Dispatchers.Main) {
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
            loadingBoxController.print("装载弹幕数据...")
            val danmukuParser = withContext(Dispatchers.IO) {
                source.getDanmakuParser()
            }
            loadingBoxController.println("成功")
            loadingBoxController.print("获取视频信息...")
            val sourceInfo = withContext(Dispatchers.IO) {
                source.getPlayerUrl(quality, fnval)
            }
            quality = sourceInfo.quality
            playerSourceInfo = sourceInfo
            loadingBoxController.print("成功")
            views.videoPlayer.releaseDanmaku()
            views.videoPlayer.danmakuParser = danmukuParser
            views.videoPlayer.setUp(
                sourceInfo.url,
                false,
                null,
                sourceInfo.header,
                source.title
            )
            loadingBoxController.hideLoading()
            if (lastPosition > 0L) {
                views.videoPlayer.seekOnStart = lastPosition
                lastPosition = 0L
            } else if (
                sourceInfo.lastPlayCid == source.id
                && !source.isLoop // 循环的视频不恢复播放
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
            views.videoPlayer.requestLayout()

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
        } catch (e: DabianException) {
            errorMessageBoxController.show("少儿不宜，禁止观看", canRetry = false)
        } catch (e: AreaLimitException) {
            (playerSource as? BangumiPlayerSource)?.let {
                areaLimitBoxController.show(it)
            } ?: errorMessageBoxController.show("抱歉你所在的地区不可观看！")
        } catch (e: UnknownHostException) {
            errorMessageBoxController.show("无法连接到御坂网络")
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessageBoxController.show(e.message ?: e.toString())
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
                }.awaitCall().json<SubtitleJsonInfo>()
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
        playerCoroutineScope.launch(Dispatchers.Main) {
            playerSource?.defaultPlayerSource?.let {
                it.lastPlayCid = ""
                it.lastPlayTime = 0L
            }
            loadPlayerSource()
        }
    }

    override fun openPlayer(source: BasePlayerSource) {
        loadingBoxController.showLoading(source.title, source.coverUrl)
        loadingBoxController.print("初始化播放器...")
        completionBoxController.hide()
        errorMessageBoxController.hide()
        areaLimitBoxController.hide()
        lastPosition = 0L
        if (playerSource != null) {
            views.videoPlayer.release()
            playerCoroutineScope.onDestroy()
            playerSource = null
        }
        playerCoroutineScope.onCreate()
        playerSource = source
        scaffoldApp.showPlayer = true
        setThumbImageView(source.coverUrl)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        loadingBoxController.println("成功")
        playerCoroutineScope.launch(Dispatchers.Main) {
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
            views.videoPlayer.setSpeed(speed, true)
            // 播放倍速提示
            if (speed != 1f) {
                PopTip.show("注意，当前为${speed}倍速").showTop()
            }
        }
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

    override fun isOpened(): Boolean {
        return scaffoldApp.showPlayer
    }

    override fun setWindowInsets(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        displayCutout: DisplayCutout?
    ) {
        views.videoPlayer.setWindowInsets(left, top, right, bottom, displayCutout)
        if (views.videoPlayer.mode == DanmakuVideoPlayer.PlayerMode.FULL) {
            loadingBoxController.setWindowInsets(left, top, right, bottom)
        } else if (views.videoPlayer.mode == DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT) {
            loadingBoxController.setWindowInsets(0, 0, 0, 0)
        } else {
            loadingBoxController.setWindowInsets(left, 0, right, 0)
        }
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
        if (scaffoldApp.orientation != newConfig.orientation) {
            controller.onChangedScreenOrientation(newConfig.orientation)
        }
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
        return (playerSourceInfo ?: playerSource?.defaultPlayerSource)?.screenProportion
    }

    fun setHoldStatus(isHold: Boolean) {
        views.videoPlayer.setHoldStatus(isHold)
        completionBoxController.setHoldStatus(isHold)
    }
}