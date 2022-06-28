package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.model.LocalVideoPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.model.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
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

    private val scaffoldApp by lazy { activity.getScaffoldView() }
    private val views by lazy { PlayerViews(activity) }

    private val playerStore by instance<PlayerStore>()

    private var quality = 64 // 默认[高清 720P]懒得做记忆功能，先不弄
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

    override fun openPlayer(source: BasePlayerSource){
        scaffoldApp.showPlayer = true
        playerCoroutineScope.onStart()
        playerSource = source
        playerCoroutineScope.launch(Dispatchers.IO) {
            val playerUrl = source.getPlayerUrl(quality)
            DebugMiao.log("playerUrl", playerUrl)
            withContext(Dispatchers.Main) {
                val header = getDefaultRequestProperties()
                views.videoPlayer.setUp(
                    playerUrl,
                    false,
                    null,
                    header,
                    source.title
                )
                views.videoPlayer.startPlayLogic()
            }
        }
    }

    override fun updateDanmukuSetting() {
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
    }

}