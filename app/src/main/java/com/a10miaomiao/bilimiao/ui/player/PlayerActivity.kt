package com.a10miaomiao.bilimiao.ui.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import cn.a10miaomiao.player.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.utils.DebugMiao
import cn.a10miaomiao.player.callback.MediaController
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.PlayurlHelper
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.ThemeUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_player.mCenterLayout
import kotlinx.android.synthetic.main.activity_player.mCenterTv
import kotlinx.android.synthetic.main.activity_player.mController
import kotlinx.android.synthetic.main.activity_player.mDanmaku
import kotlinx.android.synthetic.main.activity_player.mPlayer
import kotlinx.android.synthetic.main.activity_player.mProgressLayout
import kotlinx.android.synthetic.main.activity_player.mText
import kotlinx.android.synthetic.main.fragment_player.*
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class PlayerActivity : AppCompatActivity() {

    companion object {
        /**
         * 普通视频
         */
        fun playVideo(ctx: Context, aid: String, cid: String, title: String) {
            val intent = Intent(ctx, PlayerActivity::class.java)
            intent.putExtra("type", ConstantUtil.VIDEO)
            intent.putExtra("aid", aid)
            intent.putExtra("cid", cid)
            intent.putExtra("title", title)
            ctx.startActivity(intent)
        }

        /**
         * 番剧
         */
        fun playBangumi(ctx: Context, sid: String, epid: String, cid: String, title: String) {
            val intent = Intent(ctx, PlayerActivity::class.java)
            intent.putExtra("type", ConstantUtil.BANGUMI)
            intent.putExtra("sid", sid)
            intent.putExtra("epid", epid)
            intent.putExtra("cid", cid)
            intent.putExtra("title", title)
            ctx.startActivity(intent)
        }
    }

    val type by lazy { intent.extras.getString("type") }
    val aid by lazy { intent.extras.getString("aid") ?: "" }
    val cid by lazy { intent.extras.getString("cid") ?: "" }
    val epid by lazy { intent.extras.getString("epid") ?: "" }
    val sid by lazy { intent.extras.getString("sid") ?: "" }
    val title by lazy { intent.extras.getString("title") }

    private val sources = ArrayList<VideoSource>()
    private var acceptDescription = listOf<String>()
    private var acceptQuality = listOf<Int>()
    private var quality = 64 // 默认[高清 720P]懒得做记忆功能，先不弄
    private var danmakuContext: DanmakuContext? = null
    private var disposable: Disposable? = null

    private val width by lazy {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
    }
    private val height by lazy {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
    }
    private val mAudioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private var lastPosition = 0L //记录播放位置

    lateinit var playerService: PlayerService
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerBinder).getmPlayerService()
            initPlayer()
            initDanmaku()
            loadDanmaku()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this).init()
        setContentView(R.layout.activity_player)

        initController()
        val serviceIntent = Intent(this, PlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)

        // 隐藏导航栏
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = uiOptions
    }

    /**
     * 初始化控制器
     */
    private fun initController() {
        mController.setTitle(title)
        mController.setMediaPlayer(mPlayer)
        mController.setVisibilityChangedEvent(::setSystemUIVisible)
        mController.setDanmakuSwitchEvent {
            if (it) {
                mDanmaku.show()
            } else {
                mDanmaku.hide()
            }
        }
        mController.setVideoBackEvent { finish() }
        mController.setQualityEvent {
            val popupWindow = QualityPopupWindow(this, mController)
            popupWindow.setData(acceptDescription)
            popupWindow.checkItemPosition = acceptQuality.indexOf(quality)
            popupWindow.onCheckItemPositionChanged = this::changedQuality
            popupWindow.show()
        }
        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mController.setProgress()
                    mController.updatePausePlay()
                }
    }

    /**
     * 初始化弹幕引擎
     */
    private fun initDanmaku() {
        showText("初始化弹幕引擎")
        //配置弹幕库
        mDanmaku.enableDanmakuDrawingCache(true)
        //设置最大显示行数
//        val maxLinesPair = mapOf(
//                BaseDanmaku.TYPE_SCROLL_RL to 5
//        )
        //设置是否禁止重叠
        val overlappingEnablePair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to true,
                BaseDanmaku.TYPE_FIX_TOP to true
        )
        //设置弹幕样式
        danmakuContext = DanmakuContext.create().apply {
            //            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
//            isDuplicateMergingEnabled = false
            setScrollSpeedFactor(2.0f)
//            setScaleTextSize(0.8f)
//            setMaximumLines(maxLinesPair)
//            preventOverlapping(overlappingEnablePair)
        }
    }

    /**
     * 初始化播放器
     */
    private fun initPlayer() {
        showText("初始化播放器")
        //配置播放器
        mPlayer.setMediaController(mController)
        //mPlayerView.setMediaBufferingIndicator(mBufferingIndicator)
        mPlayer.requestFocus()
        mPlayer.setOnInfoListener(onInfoListener)
        mPlayer.setOnSeekCompleteListener(onSeekCompleteListener)
        mPlayer.setOnCompletionListener(onCompletionListener)
        mPlayer.setOnControllerEventsListener(onControllerEventsListener)
        mPlayer.setOnGestureEventsListener(onGestureEventsListener)
        playerService.setUserAgent("Bilibili Freedoooooom/MarkII")
        playerService.setVideoPlayerView(mPlayer)
    }

    /**
     * 加载弹幕
     */
    private fun loadDanmaku() {
        showText("装载弹幕资源")
        val url = BiliApiService.getDanmakuList(cid)
        MiaoHttp.get(url, {
            val stream = ByteArrayInputStream(CompressionTools.decompressXML(it.body()!!.bytes()))
            val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            loader.load(stream)
            val parser = BiliDanmukuParser()
            val dataSource = loader.dataSource
            parser.load(dataSource)
            parser
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ parser ->
                    mDanmaku.prepare(parser, danmakuContext)
                    mDanmaku.showFPS(false)
                    mDanmaku.enableDanmakuDrawingCache(false)
                    mDanmaku.setCallback(onDrawHandlerCallback)
                }, {
                    showText("装载弹幕失败")
                })
    }

    /**
     * 加载视频播放地址
     */
    private fun loadPlayurl() {
        showText("读取播放地址")
        val observer = if (type == ConstantUtil.VIDEO)
            PlayurlHelper.getVideoPalyUrl(aid, cid, quality)
        else
            PlayurlHelper.getBangumiUrl(epid, cid, quality)
        observer.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    sources.clear()
                    for (durl in it.durl) {
                        sources += VideoSource().apply {
                            uri = Uri.parse(durl.url.replace("https://", "http://")) // 简单粗暴
                            length = durl.length
                            size = durl.size
                        }
                    }
                    acceptDescription = it.accept_description
                    acceptQuality = it.accept_quality
                    quality = it.quality
                    startPlay()
                }, {
                    showText(it.message ?: "网络错误")
                })
    }

    fun startPlay() {
        hideProgressText()
        playerService.setVideoURI(sources, mapOf())
        if (lastPosition != 0L) {
            mPlayer.seekTo(lastPosition)
        }
    }

    private fun changedQuality(value: String, position: Int) {
        quality = acceptQuality[position]
        loadPlayurl()
    }
    private val onInfoListener = IMediaPlayer.OnInfoListener { mp, what, extra ->
        if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            if (mDanmaku != null && mDanmaku.isPrepared) {
                mDanmaku.pause()
//                if (mBufferingIndicator != null)
//                    mBufferingIndicator.setVisibility(View.VISIBLE);
            }
            showText("缓冲中")
        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (mDanmaku != null && mDanmaku.isPaused) {
//                mDanmaku.resume()
                mDanmaku.start(mPlayer.currentPosition)
            }
            hideProgressText()
//            if (mBufferingIndicator != null)
//                mBufferingIndicator.setVisibility(View.GONE);
        }
        true
    }

    /**
     * 弹幕加载回调
     */
    private val onDrawHandlerCallback = object : DrawHandler.Callback {
        override fun drawingFinished() {

        }

        override fun danmakuShown(danmaku: BaseDanmaku?) {

        }

        override fun prepared() {
            runOnUiThread {
                loadPlayurl()
            }
        }

        override fun updateTimer(timer: DanmakuTimer) {
//                            timer.update(mPlayer.currentPosition)
        }
    }

    /**
     * 视频跳转事件回调
     */
    private val onSeekCompleteListener = IMediaPlayer.OnSeekCompleteListener {
        if (mDanmaku != null && mDanmaku.isPrepared) {
            mDanmaku.seekTo(it.currentPosition)
            if (!mPlayer.isPlaying) {
                mDanmaku.pause()
            }
        }
    }

    /**
     * 视频播放完成事件回调
     */
    private val onCompletionListener = IMediaPlayer.OnCompletionListener {
        if (mDanmaku != null && mDanmaku.isPrepared()) {
//            mDanmakuView.seekTo(0L)
            mDanmaku.pause()
        }
        mPlayer.pause()
    }

    /**
     * 控制条控制状态事件回调
     */
    private val onControllerEventsListener = object : VideoPlayerView.OnControllerEventsListener {
        override fun onVideoPause() {
            if (mDanmaku != null && mDanmaku.isPrepared()) {
                mDanmaku.pause()
            }
        }

        override fun OnVideoResume() {
            if (mDanmaku != null && mDanmaku.isPaused()) {
                mDanmaku.resume()
            }
        }
    }

    /**
     * 手势控制器
     */
    val onGestureEventsListener = object : VideoPlayerView.OnGestureEventsListener {
        var isLeft = true
        var maxVolume = 100
        var volume = 0
        var screenBrightness = 0f
        var num = 0
        var current = 0L
        override fun isLocked() = mController.isLocked

        override fun onDown(e: MotionEvent) {
            isLeft = e.x < width / 2
            maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            screenBrightness = window.attributes.screenBrightness
            num = if (isLeft) {
                (screenBrightness * 100).toInt()
            } else {
                ((volume.toFloat() / maxVolume.toFloat()) * 100).toInt()
            }
            current = mPlayer.currentPosition
        }

        override fun onXScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float): Boolean {
            mController.setDragging(true)
            current -= (distanceX * 100).toLong()
            showCenterText(MyMediaController.generateTime(current))
            mController.setProgress(current)
            return false
        }

        override fun onYScroll(e1: MotionEvent, e2: MotionEvent, distanceY: Float): Boolean {
            num += distanceY.toInt() / 2
            num = if (num > 100) 100 else if (0 > num) 0 else num
            if (isLeft) {
                val lp = window.attributes
                lp.screenBrightness = num / 100f
                window.attributes = lp
                showCenterText("亮度：$num%")
            } else {
                volume = ((num / 100f) * maxVolume).toInt()
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                showCenterText("音量：$num%")
            }
            return false
        }

        override fun onUp(e: MotionEvent?, isXScroll: Boolean) {
            if (isXScroll) {
                mPlayer.seekTo(current)
                mController.setDragging(false)
            }
            hideCenterText()
        }
    }

    private fun showCenterText(text: String) {
        mCenterLayout.visibility = View.VISIBLE
        mCenterTv.text = text
    }

    private fun hideCenterText() {
        mCenterLayout.visibility = View.GONE
    }

    private fun showText(text: String) {
        mText.text = text
        mProgressLayout.visibility = View.VISIBLE
    }

    private fun hideProgressText() {
        mProgressLayout.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (!mController.isLocked)
            finish()
    }

    override fun onResume() {
        super.onResume()
//        if (mDanmaku != null && mDanmaku.isPrepared && mDanmaku.isPaused) {
//            mDanmaku.seekTo(lastPosition)
//        }
//        if (mPlayer != null && !mPlayer.isPlaying) {
//            mPlayer.seekTo(lastPosition)
//        }
//        lastPosition = 0

        if (mDanmaku != null && mDanmaku.isPrepared && mDanmaku.isPaused) {
//            mDanmaku.seekTo(mPlayer.currentPosition)
            mDanmaku.start(mPlayer.currentPosition)
        }
    }

    override fun onPause() {
        super.onPause()
//        if (mPlayer != null) {
//            lastPosition = mPlayer.currentPosition
//            mPlayer.pause()
//        }
        if (mDanmaku != null && mDanmaku.isPrepared) {
            mDanmaku.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPlayer != null && mPlayer.isDrawingCacheEnabled) {
            mPlayer.destroyDrawingCache()
        }
        if (mDanmaku != null && mDanmaku.isPaused) {
            mDanmaku.release()
        }
        playerService.release(false)
        disposable?.dispose()
        disposable = null
    }

    private fun setSystemUIVisible(show: Boolean) {
        val uiFlags = if (show) View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        else View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.systemUiVisibility = uiFlags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or 0x00001000
    }

}