package com.a10miaomiao.bilimiao.ui.player

import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import cn.a10miaomiao.player.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.netword.PlayurlHelper
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player.*
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import okhttp3.FormBody
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment() {

    companion object {
        /**
         * 普通视频
         */
        fun newVideoPlayerInstance(aid: String, cid: String, title: String): PlayerFragment {
            val fragment = PlayerFragment()
            val bundle = Bundle()
            bundle.putString("type", ConstantUtil.VIDEO)
            bundle.putString("aid", aid)
            bundle.putString("cid", cid)
            bundle.putString("title", title)
            fragment.arguments = bundle
            return fragment
        }

        /**
         * 番剧
         */
        fun newBangumiPlayerInstance(sid: String, epid: String, cid: String, title: String): PlayerFragment {
            val fragment = PlayerFragment()
            val bundle = Bundle()
            bundle.putString("type", ConstantUtil.BANGUMI)
            bundle.putString("sid", sid)
            bundle.putString("epid", epid)
            bundle.putString("cid", cid)
            bundle.putString("title", title)
            return fragment
        }
    }

    val type by lazy { arguments!!.getString("type") }
    val aid by lazy { arguments!!.getString("aid") ?: "" }
    val cid by lazy { arguments!!.getString("cid") ?: "" }
    val epid by lazy { arguments!!.getString("epid") ?: "" }
    val sid by lazy { arguments!!.getString("sid") ?: "" }
    val title by lazy { arguments!!.getString("title") }

    private val sources = ArrayList<VideoSource>()
    private var acceptDescription = listOf<String>()
    private var acceptQuality = listOf<Int>()
    private var quality = 64 // 默认[高清 720P]懒得做记忆功能，先不弄
    private var danmakuContext: DanmakuContext? = null
    private var disposable: Disposable? = null
    private var historyDisposable: Disposable? = null // 记录历史记录

    private val width by lazy {
        (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
    }
    private val height by lazy {
        (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
    }
    private val mAudioManager by lazy {
        context!!.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
    }

    private var lastPosition = 0L //记录播放位置
    lateinit var playerService: PlayerService
    private var isMini = true

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isMini = VideoInfoFragment.instance.isMiniPlayer.value!!
        VideoInfoFragment.instance.isMiniPlayer.observe(this, Observer {
            isMini = it!!
            mPlayer?.setVideoLayout()
            setPlayerMediaController()
        })
        initController()
        val serviceIntent = Intent(context, PlayerService::class.java)
        context!!.startService(serviceIntent)
        context!!.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)

        // 隐藏导航栏
        setSystemUIVisible(true)
        mController.hide()
        mController.setHeaderLayoutPadding(0, getStatusBarHeight(), 0, 0)
    }

    /**
     * 初始化媒体控制器
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
        mController.setVideoBackEvent {
            VideoInfoFragment.instance.isMiniPlayer.value = true
        }
        mController.setQualityEvent {
            val popupWindow = QualityPopupWindow(context!!, mController)
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
                    mMiniController.setProgress()
                    mMiniController.updatePausePlay()
                }
        historyDisposable = Observable.interval(10, TimeUnit.SECONDS)
                .subscribe {
                    historyReport()
                }

        mMiniController.setTitle("av$aid")
        mMiniController.setMediaPlayer(mPlayer)
        mMiniController.setBackOnClick(View.OnClickListener { MainActivity.of(context!!).pop() })
        mMiniController.setZoomOnClick(View.OnClickListener {
            VideoInfoFragment.instance.isMiniPlayer.value = false
        })
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
        //mPlayerView.setMediaBufferingIndicator(mBufferingIndicator)
        mPlayer.requestFocus()
        mPlayer.setOnInfoListener(onInfoListener)
        mPlayer.setOnSeekCompleteListener(onSeekCompleteListener)
        mPlayer.setOnCompletionListener(onCompletionListener)
        mPlayer.setOnControllerEventsListener(onControllerEventsListener)
        mPlayer.setOnGestureEventsListener(onGestureEventsListener)
        playerService.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36")
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

    /**
     * 记录历史进度
     */
    fun historyReport(){
        val url = "https://api.bilibili.com/x/v2/history/report"
        val realtimeProgress = (mPlayer.currentPosition / 1000).toString()  // 秒数
        val params = ApiHelper.createParams(
                "aid" to aid,
                "cid" to cid,
                "progress" to realtimeProgress,
                "realtime" to realtimeProgress,
                "type" to "3"
        )
        MiaoHttp.postString(url) {
            body = FormBody.Builder().apply {
                params.keys.forEach { add(it, params[it]) }
            }.build()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    DebugMiao.log(it)
                }
    }

    private fun startPlay() {
        hideProgressText()
        lastPosition = mPlayer?.currentPosition ?: 0
        playerService.setVideoURI(sources, mapOf(
                "Referer" to "https://www.bilibili.com/video/av$aid",
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
        ))
        if (lastPosition != 0L) {
            mPlayer.seekTo(lastPosition)
        }
        historyReport()
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
            activity!!.runOnUiThread {
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
            screenBrightness = activity!!.window.attributes.screenBrightness
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
                val lp = activity!!.window.attributes
                lp.screenBrightness = num / 100f
                activity!!.window.attributes = lp
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

    override fun onResume() {
        super.onResume()
//        if (mDanmaku != null && mDanmaku.isPrepared && mDanmaku.isPaused) {
//            mDanmaku.seekTo(lastPosition)
//        }
//        if (mPlayer != null && !mPlayer.isPlaying) {
//            mPlayer.seekTo(lastPosition)
//        }
//        lastPosition = 0
        if (mPlayer != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!prefs.getBoolean("player_background", true)) {
                mPlayer.start()
            }
            if (mDanmaku != null
                    && mDanmaku.isPrepared
                    && mDanmaku.isPaused
                    && mPlayer.isPlaying) {
                mDanmaku.start(mPlayer.currentPosition)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mPlayer != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!prefs.getBoolean("player_background", true)) {
                mPlayer.pause()
            }
        }
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
        activity!!.window.decorView.systemUiVisibility = 0x00001000
        playerService.release(false)
        disposable?.dispose()
        disposable = null
        historyDisposable?.dispose()
        historyDisposable = null

        MainActivity.of(context!!).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setSystemUIVisible(show: Boolean) {
        if (isMini) { // 竖屏情况
            activity!!.window.decorView.systemUiVisibility = 0x00001000
        } else {
            val uiFlags = if (show) View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            else View.SYSTEM_UI_FLAG_FULLSCREEN
            activity!!.window.decorView.systemUiVisibility = uiFlags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or 0x00001000
        }
    }

    private fun setPlayerMediaController() {
        if (isMini) {
            mPlayer.setMediaController(mMiniController)
            mController.visibility = View.GONE
        } else {
            mPlayer.setMediaController(mController)
            mMiniController.visibility = View.GONE
        }

    }
}