package com.a10miaomiao.bilimiao.delegate

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadFlieHelper
import cn.a10miaomiao.player.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.netword.PlayurlHelper
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.behavior.HeaderBehavior
import com.a10miaomiao.bilimiao.ui.player.QualityPopupWindow
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_palyer.*
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import okhttp3.FormBody
import org.jetbrains.anko.dip
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class VideoPlayerDelegate(
        private var activity: AppCompatActivity
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = VideoPlayerDelegate::class.simpleName

    // 播放器player_background
    lateinit var playerService: PlayerService
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerBinder).getmPlayerService()
            initPlayer()
            initDanmaku()
            initController()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    // 视频信息
    private var type = ""
    private var aid = ""
    private var cid = ""
    private var epid = ""
    private var sid = ""
    private var title = ""
    private var localEntry: BiliVideoEntry? = null

    private var quality = 64 // 默认[高清 720P]懒得做记忆功能，先不弄

    // 播放器信息
    private var sources = ArrayList<VideoSource>()
    private var acceptDescription = listOf<String>()
    private var acceptQuality = listOf<Int>()
    private var lastPosition = 0L //记录播放位置

    private var danmakuContext: DanmakuContext? = null
    private var disposable: Disposable? = null

    private val width by lazy {
        (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
    }
    private val height by lazy {
        (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
    }
    private val mAudioManager by lazy {
        activity.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
    }
    val haederBehavior by lazy {
        HeaderBehavior.from(activity.headerVideoBox)
    }
    private val playerStore by lazy {
        Store.from(activity).playerStore
    }

    // 组件
    private val mRoot = activity.mRoot
    private val mController = activity.mController
    private val mPlayer = activity.mPlayer
    private val mDanmaku = activity.mDanmaku
    private val mCenterLayout = activity.mCenterLayout
    private val mCenterTv = activity.mCenterTv
    private val mText = activity.mText
    private val mProgressLayout = activity.mProgressLayout
    private val mMiniController = activity.mMiniController
    private val mVideoTitleText = activity.videoTitleText
    private val mSizeWatcher = activity.mSizeWatcher
    private val mErrorLayout = activity.mErrorLayout
    private val mErrorTv = activity.mErrorTv
    private val mErrorTryPlayTv = activity.mErrorTryPlayTv
    private val mErrorClosePlayTv = activity.mErrorClosePlayTv

    private var mPicInPicHelper: PicInPicHelper? = null

    val isMiniPlayer = MutableLiveData<Boolean>()

    // 加载
    private var loadDanmakuDisposable: Disposable? = null
    private var loadPlayurlDisposable: Disposable? = null
    private var historyDisposable: Disposable? = null

    fun onCreate(savedInstanceState: Bundle?) {
        val serviceIntent = Intent(activity, PlayerService::class.java)
        activity.startService(serviceIntent)
        activity.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPicInPicHelper = PicInPicHelper(activity, mPlayer)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        quality = prefs.getInt("player_quality", 64)
    }

    /**
     * 初始化控制器
     */
    private fun initController() {
        mErrorTryPlayTv.setOnClickListener {
            loadDanmaku()
            hideError()
        }
        mErrorClosePlayTv.setOnClickListener {
            haederBehavior.hide()
            stopPlay()
        }
        mRoot.setOnClickListener {
            val mediaController = mPlayer.mediaController
            if (mediaController.isShowing) {
                mediaController.hide()
            } else {
                mediaController.show()
            }
        }
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
            isMiniPlayer.value = true
        }
        mController.setQualityEvent {
            val popupWindow = QualityPopupWindow(activity, mController)
            popupWindow.setData(acceptDescription)
            popupWindow.checkItemPosition = acceptQuality.indexOf(quality)
            popupWindow.onCheckItemPositionChanged = this::changedQuality
            popupWindow.show()
        }

        mMiniController.setMediaPlayer(mPlayer)
        mMiniController.setBackOnClick(View.OnClickListener {
            haederBehavior.hide()
            stopPlay()
        })
        mMiniController.setZoomOnClick(View.OnClickListener {
            isMiniPlayer.value = false
        })
        val statusBarHeight = activity.getStatusBarHeight()
        mMiniController.setPadding(0, statusBarHeight, 0, 0)
        mVideoTitleText.layoutParams.height = statusBarHeight

        mMiniController.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.mini_window -> {
                    mPicInPicHelper?.enterPictureInPictureMode()
                }
            }
            true
        })

        mSizeWatcher.onSizeChangedListener = {
            mPlayer.post { mPlayer.requestLayout() }
            mPlayer.setVideoLayout()
        }

        MainActivity.of(activity)
                .themeUtil
                .observeTheme(activity, Observer {
                    val themeColor = activity.config.themeColor
                    mController.updateColor(themeColor)
                    mMiniController.updateColor(themeColor)
                    activity.mProgressBar.indeterminateTintList = ColorStateList.valueOf(themeColor)
                })

        isMiniPlayer.observe(activity, Observer {
            setPlayerMediaController(it!!)
            activity.apply {
                if (it == null || it) {
                    headerVideoBox.layoutParams.height = dip(240)
                    rootContainer.visibility = View.VISIBLE
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    setSystemUIVisible(true)
                } else {
                    headerVideoBox.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    rootContainer.visibility = View.GONE
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    setSystemUIVisible(false)
                }
            }
            initDanmakuContext()
            mPlayer?.setVideoLayout()
        })
        isMiniPlayer.value = true
    }

    /**
     * 初始化弹幕引擎
     */
    private fun initDanmaku() {
        showText("初始化弹幕引擎")
        mDanmaku.enableDanmakuDrawingCache(true)
        danmakuContext = DanmakuContext.create()
        initDanmakuContext()
    }

    private fun initDanmakuContext() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        var scaleTextSize = prefs.getString("danmaku_fontsize", "1").toFloatOrNull() ?: 1f
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
                    BaseDanmaku.TYPE_FIX_BOTTOM to 0
            )
        } else if (isMiniPlayer.value === true) {
            maxLinesPair = mapOf(
                    BaseDanmaku.TYPE_SCROLL_RL to 5
            )
        }
        //设置弹幕样式
        danmakuContext?.apply {
//            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
//            isDuplicateMergingEnabled = false
//            setScrollSpeedFactor(2.0f)
            setScaleTextSize(scaleTextSize)
            setMaximumLines(maxLinesPair)
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
        mPlayer.setOnErrorListener(onErrorListener)
        playerService.setUserAgent("Bilibili Freedoooooom/MarkII")
        playerService.setVideoPlayerView(mPlayer)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if ("danmaku" in key ?: "") {
            initDanmakuContext()
        }
    }

    fun playBangumi(sid: String, epid: String, cid: String, title: String) {
        stopPlay()
        haederBehavior.show()
        this.type = ConstantUtil.BANGUMI
        this.sid = sid
        this.epid = epid
        this.cid = cid
        this.title = title
        mVideoTitleText.text = "正在播放：${title}"
        loadDanmaku()
        playerStore.setBangumiPlayerInfo(sid, epid, cid, title)
    }

    fun playVideo(aid: String, cid: String, title: String) {
        stopPlay()
        haederBehavior.show()
        this.type = ConstantUtil.VIDEO
        this.aid = aid
        this.cid = cid
        this.title = title
        mVideoTitleText.text = "正在播放：${title}"
        loadDanmaku()
        playerStore.setVideoPlayerInfo(aid, cid, title)
        historyReport()
    }

    fun playLocalVideo(biliVideo: BiliVideoEntry) {
        stopPlay()
        haederBehavior.show()
        this.localEntry = biliVideo
        this.type = ConstantUtil.LOCAL_VIDEO
        this.aid = biliVideo.avid.toString()
        this.cid = biliVideo.page_data.cid.toString()
        this.title = biliVideo.title
        mVideoTitleText.text = "正在播放：${biliVideo.title}"
        loadDanmaku()
        playerStore.setVideoPlayerInfo(aid, cid, title)
//        historyReport()
    }

    /**
     * 记录历史进度
     */
    fun historyReport() {
        if (type != ConstantUtil.VIDEO) {
            return
        }
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

    /**
     * 加载弹幕
     */
    private fun loadDanmaku() {
        hideError()
        mController.setTitle(title)
        showText("装载弹幕资源")
        getBiliDanmukuStream().map {
            val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            loader.load(it)
            val parser = BiliDanmukuParser()
            val dataSource = loader.dataSource
            parser.load(dataSource)
            parser
        }.apply {
            subscribeOn(Schedulers.io())
            observeOn(AndroidSchedulers.mainThread())
            loadDanmakuDisposable = subscribe({ parser ->
                mDanmaku.prepare(parser, danmakuContext)
                mDanmaku.showFPS(false)
                mDanmaku.enableDanmakuDrawingCache(false)
                mDanmaku.setCallback(onDrawHandlerCallback)
            }, {
                showText("装载弹幕失败")
            })
        }
    }

    private fun getBiliDanmukuStream(): Observable<InputStream> {
        val url = BiliApiService.getDanmakuList(cid)
        if (type === ConstantUtil.LOCAL_VIDEO) {
            return Observable.create {
                val localEntry = this.localEntry
                if (localEntry == null) {
                    it.onError(Exception("ERROR: NOT FILE"))
                } else {
                    val pageDir = DownloadFlieHelper.getDownloadFileDir(activity, localEntry)
                    val danmakuXMLFile = File(pageDir, "danmaku.xml")
                    it.onNext(danmakuXMLFile.inputStream())
                    it.onComplete()
                }
            }
        }
        return MiaoHttp.get(url, {
            ByteArrayInputStream(CompressionTools.decompressXML(it.body()!!.bytes()))
        })
    }

    /**
     * 加载视频播放地址
     */
    private fun loadPlayurl() {
        showText("读取播放地址")
        val observer = if (type == ConstantUtil.VIDEO || type == ConstantUtil.BANGUMI)
            getNetwordSources()
        else
            getLocalSources()
        loadPlayurlDisposable = observer.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    sources = it
                    startPlay()
                }, {
                    showError(it.message ?: "无法连接到御坂网络")
                })
    }

    private fun getNetwordSources(): Observable<ArrayList<VideoSource>> {
        val observer = if (type == ConstantUtil.VIDEO)
            PlayurlHelper.getVideoPalyUrl(aid, cid, quality)
        else
            PlayurlHelper.getBangumiUrl(epid, cid, quality)
        return observer.map {
            acceptDescription = it.accept_description
            acceptQuality = it.accept_quality
            quality = it.quality
            ArrayList<VideoSource>().apply {
                for (durl in it.durl) {
                    this += VideoSource().apply {
                        uri = Uri.parse(durl.url)
                        length = durl.length
                        size = durl.size
                    }
                }
            }
        }
    }

    private fun getLocalSources(): Observable<ArrayList<VideoSource>> {
        return Observable.create {
            val localEntry = this.localEntry
            if (localEntry == null) {
                it.onError(Exception("ERROR: NOT FILE"))
            } else {
                val videoDir = DownloadFlieHelper.getVideoPageFileDir(activity, localEntry)
                val pageData = DownloadFlieHelper.getVideoPage(activity, localEntry)
                val videoFlie = File(
                        videoDir, "0" + "." + pageData.format
                )
                val sources = arrayListOf(
                        VideoSource().apply {
                            uri = Uri.fromFile(videoFlie)
                            length = pageData.segment_list[0].duration
                            size = pageData.segment_list[0].bytes
                        }
                )

                acceptDescription = listOf("本地")
                acceptQuality = listOf(pageData.quality)
                quality = pageData.quality
                it.onNext(sources)
                it.onComplete()
            }
        }
    }

    fun startPlay() {
        hideProgressText()
        lastPosition = mPlayer?.currentPosition ?: 0
        playerService.setVideoURI(sources, if (type == ConstantUtil.VIDEO) mapOf(
                "Referer" to "https://www.bilibili.com/video/av$aid",
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
        ) else mapOf())
        if (lastPosition != 0L) {
            mPlayer.seekTo(lastPosition)
        }
//        historyReport()
        disposable = Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mController.setProgress()
                    mController.updatePausePlay()
                    mMiniController.setProgress()
                    mMiniController.updatePausePlay()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (activity.isInPictureInPictureMode) {
                            mPicInPicHelper?.updatePictureInPictureActions()
                        }
                    }
                }
        historyDisposable = Observable.interval(10, TimeUnit.SECONDS)
                .subscribe {
                    historyReport()
                }
    }

    fun stopPlay() {
        mVideoTitleText.text = ""
        if (mPlayer != null && mPlayer.isDrawingCacheEnabled) {
            mPlayer.destroyDrawingCache()
        }
        if (mDanmaku != null) {
            mDanmaku.release()
        }
        playerService.release(false)
        loadPlayurlDisposable?.dispose()
        loadPlayurlDisposable = null
        loadDanmakuDisposable?.dispose()
        loadDanmakuDisposable = null
        disposable?.dispose()
        disposable = null
        historyDisposable?.dispose()
        historyDisposable = null
        playerStore.clearPlayerInfo()


        this.type = ""
        this.sid = ""
        this.epid = ""
        this.cid = ""
        this.title = ""
        this.sid = ""
        this.epid = ""
    }

    private fun changedQuality(value: String, position: Int) {
        quality = acceptQuality[position]
        loadPlayurl()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putInt("player_quality", quality).apply()
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

    private val onErrorListener = IMediaPlayer.OnErrorListener { iMediaPlayer, framework_err, impl_err ->
        val message = if (framework_err == IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            cn.a10miaomiao.player.R.string.video_error_text_invalid_progressive_playback
        } else {
            cn.a10miaomiao.player.R.string.video_error_text_unknown
        }
        showError(activity.resources.getString(message))
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
            activity.runOnUiThread {
                loadPlayurl()
            }
        }

        override fun updateTimer(timer: DanmakuTimer) {
            if (abs(timer.currMillisecond - mPlayer.currentPosition) > 2000L) {
                mDanmaku.start(mPlayer.currentPosition)
            }
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
            screenBrightness = activity.window.attributes.screenBrightness
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
                val lp = activity.window.attributes
                lp.screenBrightness = num / 100f
                activity.window.attributes = lp
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

    private fun showError(message: String) {
        mErrorLayout.visibility = View.VISIBLE
        mErrorTv.text = message
    }

    private fun hideError() {
        mErrorLayout.visibility = View.GONE
    }

    fun onBackPressed(): Boolean {
        if (mController.isLocked) {
            return true
        }
        if (isMiniPlayer.value == false) {
            isMiniPlayer.value = true
            return true
        }
        return false
    }


    fun onResume() {

    }

    fun onPause() {

    }

    fun onStart() {
        if (mPlayer != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
            if (!prefs.getBoolean("player_background", true)) {
                mPlayer.start()
            }
            if (mDanmaku != null && mPlayer.isPlaying) {
                mDanmaku.start(mPlayer.currentPosition)
            }
        }
    }

    fun onStop() {
        if (mPlayer != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
            if (!prefs.getBoolean("player_background", true)) {
                mPlayer.pause()
            }
        }
        if (mDanmaku != null && mDanmaku.isPrepared) {
            mDanmaku.pause()
        }
    }

    fun onDestroy() {
        activity.unbindService(mConnection)
        stopPlay()
    }

    fun setVideoLayout() {
        mPlayer?.setVideoLayout()
    }

    private fun setPlayerMediaController(isMini: Boolean) {
        if (isMini) {
            mPlayer.setMediaController(mMiniController)
            mController.visibility = View.GONE
        } else {
            mPlayer.setMediaController(mController)
            mMiniController.visibility = View.GONE
        }
    }

    private fun setSystemUIVisible(show: Boolean) {
        val uiFlags = (if (show) {
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }) or 0x00001000
        if (isMiniPlayer.value == false) {
            activity.window.decorView.systemUiVisibility = uiFlags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        } else {
            activity.window.decorView.systemUiVisibility = uiFlags
        }
        // 控制按钮不被刘海遮住
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MainActivity.of(activity).windowInsets?.let {
                mController.post {
                    mController.setDisplayCutout(it.displayCutout)
                }
            }
        }
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        mPicInPicHelper?.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) { // 进入画中画模式，则隐藏其它控件
            // 隐藏视频控制器
            mMiniController.visibility = View.GONE
            // 视频组件全屏
            activity.headerVideoBox.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            // 调整弹幕样式，调小字体，限制行数
            initDanmakuContext()
        } else {
            activity.headerVideoBox.layoutParams.height = activity.dip(240)
            initDanmakuContext()
        }
    }

    /**
     * 是否为当前aid
     */
    fun isAid (aid: String): Boolean {
        return this.aid == aid
    }

}