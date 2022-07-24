package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.model.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.model.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.view.network
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.TransferListener
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener
import tv.danmaku.ijk.media.exo2.ExoSourceManager
import java.io.File


class PlayerDelegate2(
    private var activity: AppCompatActivity,
    override val di: DI,
) : BasePlayerDelegate, DIAware, ExoMediaSourceInterceptListener {

    val DEFAULT_REFERER = "https://www.bilibili.com/"
    val DEFAULT_USER_AGENT = "Bilibili Freedoooooom/MarkII"

    val views by lazy { PlayerViews(activity) }
    val controller by lazy {
        PlayerController(activity, this, di)
    }
    val scaffoldApp by lazy { activity.getScaffoldView() }

    private val playerStore by instance<PlayerStore>()

    var playerSourceInfo: PlayerSourceInfo? = null
    var quality = 64 // 默认[高清 720P]懒得做记忆功能，先不弄
    private var lastPosition = 0L
    private val playerCoroutineScope = PlayerCoroutineScope()
    private var playerSource: BasePlayerSource? = null
        set(value) {
            field = value
            if (value != null) {
                playerStore.setPlayerSource(value)
            } else {
                playerStore.clearPlayerInfo()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
//        PlayerFactory.setPlayManager(MyIjkPlayerManager::class.java)
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java) //EXO模式
        ExoSourceManager.setExoMediaSourceInterceptListener(this)
        controller.initController()
        controller.initDanmakuContext()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        quality = prefs.getInt("player_quality", 64)
    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun onStart() {
        playerCoroutineScope.onStart()
    }

    override fun onStop() {
        playerCoroutineScope.onStop()
    }

    override fun onDestroy() {

    }

    override fun onBackPressed(): Boolean {
        if (scaffoldApp.fullScreenPlayer) {
            controller.smallScreen()
            return true
        }
        return false
    }

    override fun getMediaSource(
        dataSource: String,
        preview: Boolean,
        cacheEnable: Boolean,
        isLooping: Boolean,
        cacheDir: File?
    ): MediaSource? {
        val dataSourceArr = dataSource.split("\n")
        if (dataSourceArr.size > 1) {
            val uri = Uri.parse(dataSourceArr[0])
            val dashStr = dataSourceArr[1]
            // Create a data source factory.
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val header = getDefaultRequestProperties()
            dataSourceFactory.setUserAgent(DEFAULT_USER_AGENT)
            dataSourceFactory.setDefaultRequestProperties(header)
            // Create a DASH media source pointing to a DASH manifest uri.
            val dashManifest = DashManifestParser().parse(uri, dashStr.toByteArray().inputStream())
            val mediaSource = DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(dashManifest)
//                    mediaSource.prepareSource()
            return mediaSource
        }
        return null
    }

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

    private fun historyReport() {
        playerCoroutineScope.launch(Dispatchers.IO) {
            playerSource?.historyReport(views.videoPlayer.currentPositionWhenPlaying / 1000)
        }
    }

    private fun setThumbImageView (coverUrl: String) {
        views.videoPlayer.thumbImageView = ImageView(activity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            network(coverUrl)
        }
    }

    fun changedQuality(newQuality: Int) {
        if (quality != newQuality) {
            lastPosition = views.videoPlayer.currentPositionWhenPlaying
            quality = newQuality
            loadPlayerSource()
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
            prefs.edit().putInt("player_quality", 64).apply()
        }
    }

    private fun loadPlayerSource() {
        val source = playerSource ?: return
        playerCoroutineScope.launch(Dispatchers.IO) {
            val danmukuParser = source.getDanmakuParser()
            val sourceInfo = source.getPlayerUrl(quality)
            quality = sourceInfo.quality
            withContext(Dispatchers.Main) {
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
                views.videoPlayer.startPlayLogic()
                if (lastPosition > 0L) {
                    views.videoPlayer.seekTo(lastPosition)
                    lastPosition = 0L
                }
                historyReport()
            }
            playerSourceInfo = sourceInfo
        }
    }

    override fun openPlayer(source: BasePlayerSource){
        lastPosition = 0L
        scaffoldApp.showPlayer = true
        playerCoroutineScope.onStart()
        playerSource = source
        setThumbImageView(source.coverUrl)
        loadPlayerSource()
    }

    override fun closePlayer() {
        scaffoldApp.showPlayer = false
        playerCoroutineScope.onStop()
        historyReport()
        playerSource = null
        views.videoPlayer.release()
        lastPosition = 0L
    }

    override fun isPlaying(): Boolean {
        return views.videoPlayer.isInPlayingState
    }

    override fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        views.videoPlayer.setWindowInsets(left, top, right, bottom)
    }

    override fun updateDanmukuSetting() {
        controller.initDanmakuContext()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
    }

}