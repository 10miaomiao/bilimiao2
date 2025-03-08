package com.a10miaomiao.bilimiao.widget.player

import android.app.Activity
import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.AttributeSet
import android.view.DisplayCutout
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import splitties.dimensions.dip
import splitties.views.backgroundColor
import kotlin.math.min


class DanmakuVideoPlayer : StandardGSYVideoPlayer {

    enum class PlayerMode {
        SMALL_TOP,
        SMALL_FLOAT,
        FULL,
    }

    // 弹幕组件
    private val mDanmakuView: DanmakuView by lazy { findViewById(R.id.danmaku_view) }

    // 根布局组件
    private val mRootLayout: RelativeLayout by lazy { findViewById(R.id.root_layout) }

    // 小窗顶部拖动横条
    private val mDragBarLayout: FrameLayout by lazy { findViewById(R.id.layout_drag_bar) }
    private val mDragBar: View by lazy { findViewById(R.id.drag_bar) }
    private val mHoldUpBtn: View by lazy { findViewById(R.id.hold_up) }

    // 顶栏更多按钮
    private val mMoreBtn: View by lazy { findViewById(R.id.more) }

    // 底栏布局
    private val mBottomLayout: LinearLayout by lazy { findViewById(R.id.layout_bottom) }

    // 全屏时底栏布局
    private val mFullModeBottomContainer: ViewGroup by lazy { findViewById(R.id.layout_full_mode_bottom) }

    // 底栏播放按钮
    private val mButtomPlay: ImageView by lazy { findViewById(R.id.buttom_play) }

    // 底部字幕
    private val mBottomSubtitleTV: TextView by lazy { findViewById(R.id.bottom_subtitle) }

    // 字幕开关
    private val mSubtitleSwitch: ViewGroup by lazy { findViewById(R.id.subtitle_switch) }

    // 字幕开关图标
    private val mSubtitleSwitchIV: ImageView by lazy { findViewById(R.id.subtitle_switch_icon) }

    // 字幕开关文字
    private val mSubtitleSwitchTV: TextView by lazy { findViewById(R.id.subtitle_switch_text) }

    // 弹幕开关
    private val mDanmakuSwitch: ViewGroup by lazy { findViewById(R.id.danmaku_switch) }

    // 弹幕开关图标
    private val mDanmakuSwitchIV: ImageView by lazy { findViewById(R.id.danmaku_switch_icon) }

    // 弹幕开关文字
    private val mDanmakuSwitchTV: TextView by lazy { findViewById(R.id.danmaku_switch_text) }

    private val mMiniSendDanmakuIV: ImageView by lazy { findViewById(R.id.send_danmaku_mini) }
    private val mSendDanmakuTV: TextView by lazy { findViewById(R.id.send_danmaku) }

    // 清晰度
    private val mQuality: ViewGroup by lazy { findViewById(R.id.quality) }

    // 清晰度文字
    private val mQualityTV: TextView by lazy { findViewById(R.id.quality_text) }

    // 倍速
    private val mPlaySpeed: ViewGroup by lazy { findViewById(R.id.play_speed) }

    // 倍速文字名称
    private val mPlaySpeedName: TextView by lazy { findViewById(R.id.play_speed_name) }

    // 倍速文字值
    private val mPlaySpeedValue: TextView by lazy { findViewById(R.id.play_speed_value) }

    // 锁定按钮
    private val mLock: ViewGroup by lazy { findViewById(R.id.lock) }

    // 锁定时控制容器
    private val mLockContainer: ViewGroup by lazy { findViewById(R.id.layout_lock_screen) }

    // 左边解锁按钮
    private val mUnlockLeftIV: ImageView by lazy { findViewById(R.id.unlock_left) }

    // 右边解锁按钮
    private val mUnlockRightIV: ImageView by lazy { findViewById(R.id.unlock_right) }

    // 倍数播放提示
    private val mSpeedTips: LinearLayout by lazy { findViewById(R.id.speed_tips) }

    // 倍数播放提示图标
    private val mSpeedTipsIV: ImageView by lazy { findViewById(R.id.speed_tips_icon) }

    // 拓展按钮布局
    private val mExpandBtnLayout: LinearLayout by lazy { findViewById(R.id.expand_btn_layout) }

    // 拓展按钮文本
    private val mExpandBtnTV: TextView by lazy { findViewById(R.id.expand_btn_text) }

    // 弹幕时间与播放器时间同步
    private val mDanmakuTime = object : DanmakuTimer() {
        private var lastTime = 0L
        override fun currMillisecond(): Long {
            lastTime = try {
                gsyVideoManager.currentPosition
            } catch (e: Exception) {
                0L
            }
            return lastTime
        }

        override fun update(curr: Long): Long {
            lastInterval = curr - lastTime
            return lastInterval
        }
    }

    private var mDisplayCutout: DisplayCutout? = null

    private var mFocusRequest: AudioFocusRequest? = null

    private val mAudioFocusHandler = Handler(Looper.getMainLooper())

    // 字幕源列表
    var subtitleSourceList = emptyList<SubtitleSourceInfo>()
        set(value) {
            field = value
            updateSubtitleSourceList()
        }

    // 当前选中字幕
    var currentSubtitleSource: SubtitleSourceInfo? = null
        set(value) {
            field = value
            updateCurrentSubtitleSource()
        }

    // 当前模式
    var mode = PlayerMode.SMALL_TOP
        set(value) {
            field = value
            updateMode()
        }

    // 是否处于画中画模式
    var isPicInPicMode = false

    var isHoldUp = false

    // 是否显示当面
    var isShowDanmaku = true
        set(value) {
            field = value
            resolveDanmakuShow()
        }

    // 弹幕开始位置
    var danmakuStartSeekPosition: Long = -1
    var danmakuParser: BaseDanmakuParser? = null
        set(value) {
            if (value != null) {
                value.timer = mDanmakuTime
            }
            field = value
        }
    var danmakuContext: DanmakuContext? = null

    // 状态栏
    var statusBarHelper: StatusBarHelper? = null

    // 播放回调
    var videoPlayerCallBack: VideoPlayerCallBack? = null

    // 加载字幕
    var subtitleLoader: ((url: String) -> Unit)? = null

    // 字幕源选择
    var subtitleSourceSelector: ((list: List<SubtitleSourceInfo>) -> SubtitleSourceInfo?)? = null

    var subtitleBody: List<SubtitleItemInfo> = emptyList()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                postDelayed(subtitleTask, 0)
            }
        }

    private var subtitleIndex = 0

    val isAutoCompletion get() = currentState == CURRENT_STATE_AUTO_COMPLETE
    val currentPosition get() = try {
        gsyVideoManager.currentPosition
    } catch (e: Exception) {
        0L
    }

    // 供外部访问
    val topContainer: ViewGroup get() = mTopContainer
    val qualityView: View get() = mQuality
    val speedView: View get() = mPlaySpeed
    val speedValueTextView: View get() = mPlaySpeedValue
    val moreBtn: View get() = mMoreBtn

    // 是否处于锁定状态
    var isLock: Boolean = false
        set(value) {
            field = value
            if (value) {
                hideAllWidget()
                mLockContainer.visibility = VISIBLE
            } else {
                mLockContainer.visibility = GONE
            }
        }
    // 全屏状态下显示底部进度条
    var showBottomProgressBarInFullMode = true
    // 小屏状态下显示底部进度条
    var showBottomProgressBarInSmallMode = true
    // 画中画状态下显示底部进度条
    var showBottomProgressBarInPipMode = true
    // 占用音频焦点
    var enabledAudioFocus = true

    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag) {
        initView()
    }

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_danmaku_palyer
    }

    private fun initView() {
        mSeekRatio = 200f
        isShowDragProgressTextOnSeekBar = true
        enlargeImageRes = R.drawable.ic_player_portrait_fullscreen
        shrinkImageRes = R.drawable.ic_player_portrait_fullscreen
        setDialogVolumeProgressBar(context)
        setDialogProgressBar(context)
        initDanmakuContext()
        mButtomPlay.setOnClickListener {
            clickStartIcon()
        }
        mSubtitleSwitch.setOnClickListener {
            val menus = mutableListOf<CheckPopupMenu.MenuItemInfo<SubtitleSourceInfo?>>()
            menus.addAll(subtitleSourceList.map {
                CheckPopupMenu.MenuItemInfo(it.lan_doc, it)
            })
            menus.add(CheckPopupMenu.MenuItemInfo("关闭字幕", null))
            val pm = CheckPopupMenu(
                context = context,
                anchor = it,
                menus = menus,
                value = currentSubtitleSource,
            )
            pm.onMenuItemClick = {
                currentSubtitleSource = it.value
            }
            pm.show()
        }
        mBottomSubtitleTV.setTextColor(Color.parseColor("#FFFFFF"))
        mBottomSubtitleTV.backgroundColor = Color.parseColor("#66000000")

        val lockClickListener = OnLockClickListener()
        mLock.setOnClickListener(lockClickListener)
        mLockContainer.setOnClickListener(lockClickListener)
        mUnlockLeftIV.setOnClickListener(lockClickListener)
        mUnlockRightIV.setOnClickListener(lockClickListener)

        //android 版本 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attribute = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setWillPauseWhenDucked(true)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener, mAudioFocusHandler)
                .setAudioAttributes(attribute)
                .build()
        }
    }

    private fun updateMode() {
        when (mode) {
            PlayerMode.SMALL_TOP, PlayerMode.SMALL_FLOAT -> {
                mFullModeBottomContainer.visibility = GONE
                mPlaySpeedName.visibility = GONE
                mMiniSendDanmakuIV.visibility = VISIBLE
                mSendDanmakuTV.visibility = GONE
                mBackButton.setImageResource(R.drawable.ic_close_white_24dp)
                if (mode == PlayerMode.SMALL_FLOAT) {
                    mDragBarLayout.visibility = mTopContainer.visibility
                } else {
                    mDragBarLayout.visibility = GONE
                }
                updateDanmakuMargin()
            }
            PlayerMode.FULL -> {
                mFullModeBottomContainer.visibility = VISIBLE
                mPlaySpeedName.visibility = VISIBLE
                mMiniSendDanmakuIV.visibility = GONE
                mSendDanmakuTV.visibility = VISIBLE
                mBackButton.setImageResource(R.drawable.ic_arrow_back_white_24dp)
                mDragBarLayout.visibility = GONE
                updateDanmakuMargin()
            }
        }
    }

    /**
     * 竖屏全屏时，防止挖孔屏挡住弹幕
     */
    private fun updateDanmakuMargin() {
        val danmakuViewLP = mDanmakuView.layoutParams as MarginLayoutParams
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            && mode == PlayerMode.FULL
            && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            danmakuViewLP.topMargin = mDisplayCutout?.safeInsetTop ?: 0
        } else {
            danmakuViewLP.topMargin = 0
        }
    }

    private fun updateSubtitleSourceList() {
        if (subtitleSourceList.isEmpty()) {
            setViewShowState(mSubtitleSwitch, GONE)
            currentSubtitleSource = null
        } else {
            setViewShowState(mSubtitleSwitch, VISIBLE)
            currentSubtitleSource = subtitleSourceSelector?.invoke(subtitleSourceList)
        }
    }

    private fun updateCurrentSubtitleSource() {
        subtitleBody = emptyList()
        mBottomSubtitleTV.visibility = GONE
        if (currentSubtitleSource == null) {
            mSubtitleSwitchIV.setImageResource(R.drawable.bili_player_subtitle_is_closed)
            mSubtitleSwitchTV.text = "字幕关"
        } else {
            mSubtitleSwitchIV.setImageResource(R.drawable.bili_player_subtitle_is_open)
            mSubtitleSwitchTV.text = currentSubtitleSource?.lan_doc ?: "字幕开"
            subtitleLoader?.invoke(currentSubtitleSource?.subtitle_url ?: "")
        }
    }

    private var touchSurfaceDownTime = Long.MAX_VALUE
    private var isSpeedPlaying = false
    private var lastSpeed = 0f  // init an invalid value

    private val longClickControlTask = Runnable {
        if (System.currentTimeMillis() - touchSurfaceDownTime >= 500
            && mCurrentState == CURRENT_STATE_PLAYING
            && !mChangePosition && !mChangeVolume && !mBrightness) {
            startLongClickSpeedPlay()
        }
    }

    /**
     * 开始长按倍数播放
     */
    private fun startLongClickSpeedPlay() {
        isSpeedPlaying = true
        lastSpeed = speed
        speed *= 2
        mSpeedTips.visibility = View.VISIBLE
        mTouchingProgressBar = false
        (mSpeedTipsIV.drawable as? AnimationDrawable)?.start()
        // 震动反馈
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * 停止长按倍数播放
     */
    private fun stopLongClickSpeedPlay() {
        mSpeedTips.visibility = View.GONE
        isSpeedPlaying = false
        speed = lastSpeed
        (mSpeedTipsIV.drawable as? AnimationDrawable)?.stop()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            when(event.action){
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP-> {
                    //触控被拦截不触发长按倍速
                    removeCallbacks(longClickControlTask)
                    touchSurfaceDownTime = Long.MAX_VALUE
                    if (isSpeedPlaying) {
                        stopLongClickSpeedPlay()
                    }
                }
            }
        }
        return super.onTouch(v, event)
    }

    override fun touchSurfaceDown(x: Float, y: Float) {
        super.touchSurfaceDown(x, y)
        val curWidth = measuredWidth
        val curHeight = measuredHeight
        val edgeSize = context.dip(80).let {
            min(min(curWidth, curHeight), it) / 2
        }
        if (x.toInt() in edgeSize..(curWidth - edgeSize)
            && y.toInt() in edgeSize..(curHeight - edgeSize)) {
            // 屏幕边缘不触发长按倍数
            touchSurfaceDownTime = System.currentTimeMillis()
            postDelayed(longClickControlTask, 500)
        }
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (isSpeedPlaying) {
            mChangePosition=false
            return
        }
        if (mDownY<context.dip(25)){
            //顶部防误触
            mChangePosition=false
            return
        }
        var curHeight = 0
        if (activityContext != null) {
            curHeight =
                if (CommonUtil.getCurrentScreenLand(activityContext as Activity)) mScreenWidth else mScreenHeight
        }
        if (mChangePosition) {
            if (mDownPosition == 0L) {
                mDownPosition = currentPosition
            }
            //
            val totalTimeDuration = duration
            val offsetPosition = deltaX / context.dip(1) * mSeekRatio
            mSeekTimePosition = (mDownPosition + offsetPosition).toLong()
            if (mSeekTimePosition < 0) {
                mSeekTimePosition = 0
            }
            if (mSeekTimePosition > totalTimeDuration) mSeekTimePosition = totalTimeDuration
            val seekTime = CommonUtil.stringForTime(mSeekTimePosition)
            val totalTime = CommonUtil.stringForTime(totalTimeDuration)
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration)
        } else if (mChangeVolume) {
            val deltaY = -deltaY
            val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val deltaV = (max * deltaY * 3 / curHeight).toInt()
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0)
            val volumePercent =
                (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / curHeight).toInt()
            showVolumeDialog(-deltaY, volumePercent)
        } else if (mBrightness) {
            if (Math.abs(deltaY) > mThreshold) {
                val percent = -deltaY / curHeight
                onBrightnessSlide(percent)
                mDownY = y
            }
        }
    }

    // end

//    override fun setProgressAndTime(
//        progress: Long,
//        secProgress: Long,
//        currentTime: Long,
//        totalTime: Long,
//        forceChange: Boolean
//    ) {
//        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
//        setBottomSubtitleText(currentTime)
//    }

    override fun startProgressTimer() {
        super.startProgressTimer()
        if (subtitleBody.isNotEmpty()) {
            postDelayed(subtitleTask, 100)
        }
    }

    override fun cancelProgressTimer() {
        super.cancelProgressTimer()
        removeCallbacks(subtitleTask)
    }

    var subtitleTask: Runnable = object : Runnable {
        override fun run() {
            if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
                setBottomSubtitleText()
            }
            if (mPostProgress) {
                postDelayed(this, 100)
            }
        }
    }

    private fun setBottomSubtitleText() {
        if (subtitleBody.isEmpty()) return
        val currentTime = currentPositionWhenPlaying
        // 读取上一次索引位置，顺便检查是否在范围内
        var index = if (subtitleIndex < 0) {
            0
        } else if (subtitleIndex < subtitleBody.size) {
            subtitleIndex
        } else {
            subtitleBody.size - 1
        }
        while (index in subtitleBody.indices) {
            val item = subtitleBody[index] // 索引位置字幕信息
            if (item.from > currentTime) {
                // 字幕开始时间大于当前时间
                if (index != 0 && currentTime > subtitleBody[index - 1].to) {
                    // 上一个字幕结束时间小于当前时间
                    mBottomSubtitleTV.visibility = GONE
                    break
                } else {
                    index--
                }
            } else if (item.to < currentTime) {
                // 字幕结束时间小于当前时间
                index++
            } else {
                subtitleIndex = index // 保存当前索引
                mBottomSubtitleTV.text = item.content // 设置字幕内容
                mBottomSubtitleTV.visibility = VISIBLE
                break
            }
        }
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        super.onClickUiToggle(e)
        videoPlayerCallBack?.onClickUiToggle(e)
    }

    override fun hideAllWidget() {
        super.hideAllWidget()
        if (isPicInPicMode) {
            if (showBottomProgressBarInPipMode) {
                setViewShowState(mBottomProgressBar, VISIBLE)
            } else {
                setViewShowState(mBottomProgressBar, INVISIBLE)
            }
        } else {
            if (mode == PlayerMode.FULL && showBottomProgressBarInFullMode) {
                setViewShowState(mBottomProgressBar, VISIBLE)
            } else if ((mode == PlayerMode.SMALL_FLOAT || mode == PlayerMode.SMALL_TOP)
                && showBottomProgressBarInSmallMode) {
                setViewShowState(mBottomProgressBar, VISIBLE)
            } else {
                setViewShowState(mBottomProgressBar, INVISIBLE)
            }
        }
    }

    private fun showAllWidget() {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
        } else {
            if (mIfCurrentIsFullscreen && !mSurfaceErrorPlay
                && mCurrentState == CURRENT_STATE_ERROR) {
                changeUiToPlayingShow()
            } else if (mCurrentState == CURRENT_STATE_PREPAREING) {
                changeUiToPreparingShow()
            } else if (mCurrentState == CURRENT_STATE_PLAYING) {
                changeUiToPlayingShow()
            } else if (mCurrentState == CURRENT_STATE_PAUSE) {
                changeUiToPauseShow()
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                changeUiToCompleteShow()
            } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START
                && mBottomContainer != null) {
                changeUiToPlayingBufferingShow()
            }
        }
    }

    override fun setStateAndUi(state: Int) {
        super.setStateAndUi(state)
        val playBtnImageRes = if (state == CURRENT_STATE_PLAYING) {
            R.drawable.bili_player_play_can_pause
        } else {
            R.drawable.bili_player_play_can_play
        }
        mButtomPlay.setImageResource(playBtnImageRes)
        videoPlayerCallBack?.setStateAndUi(state)
    }

    override fun setViewShowState(view: View, visibility: Int) {
        if (isPicInPicMode) {
            if (view.id == mStartButton.id || view.id == mBottomProgressBar.id) {
                view.visibility = visibility
            }
        } else if (isHoldUp){
            if (view.id == mBottomProgressBar.id) {
                view.visibility = visibility
            } else {
                view.visibility = GONE
            }
        } else {
            super.setViewShowState(view, visibility)
            if (view.id == mBottomLayout.id) {
                mBottomSubtitleTV.translationY =
                    if (visibility == VISIBLE) 0f else dip(40).toFloat()
                when (mode) {
                    PlayerMode.SMALL_FLOAT -> {
                        mDragBarLayout.visibility = visibility
                    }
                    PlayerMode.SMALL_TOP -> {
                        mDragBarLayout.visibility = View.GONE
                    }
                    PlayerMode.FULL -> {
                        statusBarHelper?.isShowStatus = visibility == View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (
            id == com.shuyu.gsyvideoplayer.R.id.surface_container
            && event.action == MotionEvent.ACTION_CANCEL
            ) {
            if (mHideKey && mShowVKey) {
                return true
            }
            touchSurfaceUp()
        }
        return super.onTouchEvent(event)
    }

    override fun startPrepare() {
        // super.startPrepare()
        // 重写方法，加入音频焦点开关
        this.gsyVideoManager.listener()?.onCompletion()

        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared")
            mVideoAllCallBack.onStartPrepared(mOriginUrl, *arrayOf(mTitle, this))
        }

        this.gsyVideoManager.setListener(this)
        this.gsyVideoManager.playTag = mPlayTag
        this.gsyVideoManager.playPosition = mPlayPosition

        // AudioManager.requestAudioFocus(onAudioFocusChangeListener, 3, 2)
        if (enabledAudioFocus) {
            requestAudioFocus()
        }

        try {
            (mContext as? Activity)?.window
                ?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (var2: java.lang.Exception) {
            var2.printStackTrace()
        }

        mBackUpPlayingBufferState = -1
        this.gsyVideoManager.prepare(
            mUrl,  mMapHeadData ?: hashMapOf(),
            mLooping, mSpeed, mCache, mCachePath, mOverrideExtension
        )
        setStateAndUi(CURRENT_STATE_PREPAREING)
    }

    override fun onPrepared() {
        super.onPrepared()
        onPrepareDanmaku(this)
        videoPlayerCallBack?.onPrepared()
    }

    override fun onAutoCompletion() {
        super.onAutoCompletion()
        videoPlayerCallBack?.onAutoCompletion()
        releaseDanmaku()
    }

    override fun onVideoPause() {
        super.onVideoPause()
        danmakuOnPause()
        videoPlayerCallBack?.onVideoPause()
        if (enabledAudioFocus) {
            abandonAudioFocus()
        }
    }

    override fun onVideoResume(isResume: Boolean) {
        super.onVideoResume(isResume)
        danmakuOnResume()
        videoPlayerCallBack?.onVideoResume(isResume)
        if (enabledAudioFocus) {
            requestAudioFocus()
        }
    }

    override fun onVideoResume() {
        onVideoResume(true)
    }

    override fun clickStartIcon() {
        super.clickStartIcon()
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            danmakuOnResume()
            if (enabledAudioFocus) {
                requestAudioFocus()
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            danmakuOnPause()
            if (enabledAudioFocus) {
                abandonAudioFocus()
            }
        }
    }

    override fun onCompletion() {
        if (enabledAudioFocus) {
            abandonAudioFocus()
        }
        super.onCompletion()
        releaseDanmaku()
    }

    override fun release() {
        releaseDanmaku()
        gsyVideoManager?.player?.stop()
        super.release()
    }

    fun closeVideo() {
        videoPlayerCallBack?.onVideoClose()
    }

    fun releaseDanmaku() {
        mDanmakuView.release()
    }

    private fun initDanmakuContext() {
        mDanmakuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {}
            override fun drawingFinished() {}
            override fun danmakuShown(danmaku: BaseDanmaku) {}
            override fun prepared() {
                mDanmakuView.start()
                if (danmakuStartSeekPosition != -1L) {
                    resolveDanmakuSeek(
                        this@DanmakuVideoPlayer,
                        danmakuStartSeekPosition
                    )
                    danmakuStartSeekPosition = -1
                }
                resolveDanmakuShow()
            }
        })
        mDanmakuView.enableDanmakuDrawingCache(true)
    }

    protected fun danmakuOnPause() {
        if (mDanmakuView != null && mDanmakuView.isPrepared) {
            mDanmakuView.pause()
        }
    }

    protected fun danmakuOnResume() {
        if (mDanmakuView != null && mDanmakuView.isPrepared) {
            mDanmakuView.start(currentPositionWhenPlaying)
        }
    }

    /**
     * 开始播放弹幕
     */
    private fun onPrepareDanmaku(gsyVideoPlayer: DanmakuVideoPlayer) {
        if (danmakuParser != null) {
            mDanmakuView.prepare(danmakuParser, danmakuContext)
        }
    }

    /**
     * 弹幕的显示与关闭
     */
    private fun resolveDanmakuShow() {
        post {
            mDanmakuView.show()
            if (isShowDanmaku) {
                if (!mDanmakuView.isShown) {
                    mDanmakuView.show()
                }
                mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_open)
                mDanmakuSwitchTV.text = "弹幕开"
                mMiniSendDanmakuIV.alpha = 1f
            } else {
                if (mDanmakuView.isShown) {
                    mDanmakuView.hide()
                }
                mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_closed)
                mDanmakuSwitchTV.text = "弹幕关"
                mMiniSendDanmakuIV.alpha = 0.5f
            }
        }
    }

    /**
     * 弹幕偏移
     */
    private fun resolveDanmakuSeek(gsyVideoPlayer: DanmakuVideoPlayer, time: Long) {
        if (mHadPlay && mDanmakuView.isPrepared) {
            mDanmakuView.seekTo(time)
        }
    }

    /**
     * 添加弹幕
     */
    fun addDanmaku(danmaku: BaseDanmaku) {
        mDanmakuView.addDanmaku(danmaku)
    }

    /**
     * 控制器拓展按钮
     */
    fun setDanmakuSwitchOnClickListener(l: OnClickListener) {
        mDanmakuSwitch.setOnClickListener {
            startDismissControlViewTimer()
            l.onClick(it)
        }
    }

    fun setExpandButtonText(text: String) {
        mExpandBtnTV.text = text
    }
    fun showExpandButton() {
        mExpandBtnLayout.visibility = View.VISIBLE
    }
    fun hideExpandButton() {
        mExpandBtnLayout.visibility = View.GONE
    }
    fun setExpandButtonOnClickListener(l: OnClickListener) {
        mExpandBtnLayout.setOnClickListener(l)
    }

    fun setSendDanmakuButtonOnClickListener(l: OnClickListener) {
        mMiniSendDanmakuIV.setOnClickListener(l)
        mSendDanmakuTV.setOnClickListener(l)
    }

    fun setSendDanmakuButtonOnLongClickListener(l: OnLongClickListener) {
        mMiniSendDanmakuIV.setOnLongClickListener(l)
    }

    fun serHoldUpButtonOnClickListener(l: OnClickListener) {
        mHoldUpBtn.setOnClickListener(l)
    }

    private var mDialogOffsetText: TextView? = null
    override fun showProgressDialog(
        deltaX: Float,
        seekTime: String?,
        seekTimePosition: Long,
        totalTime: String,
        totalTimeDuration: Long
    ) {
        if (mProgressDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(
                R.layout.layout_video_progress_dialog, null
            )
            mDialogProgressBar = localView.findViewById(progressDialogProgressId)
            if (mDialogProgressBarDrawable != null) {
                mDialogProgressBar.progressDrawable = mDialogProgressBarDrawable
            }
            mDialogProgressBar = localView.findViewById(progressDialogProgressId)
            if (mDialogProgressBarDrawable != null) {
                mDialogProgressBar.progressDrawable = mDialogProgressBarDrawable
            }
            mDialogSeekTime = localView.findViewById(progressDialogCurrentDurationTextId)
            mDialogTotalTime = localView.findViewById(progressDialogAllDurationTextId)
            mDialogIcon = localView.findViewById(progressDialogImageId)
            mDialogOffsetText = localView.findViewById(R.id.tv_offset)

            mProgressDialog = Dialog(activityContext, R.style.video_style_dialog_progress)
            mProgressDialog.setContentView(localView)
            mProgressDialog.window!!.addFlags(Window.FEATURE_ACTION_BAR)
            mProgressDialog.window!!.addFlags(32)
            mProgressDialog.window!!.addFlags(16)
            mProgressDialog.window!!.setLayout(width, height)
            if (mDialogProgressNormalColor != -11 && mDialogTotalTime != null) {
                mDialogTotalTime.setTextColor(mDialogProgressNormalColor)
            }
            if (mDialogProgressHighLightColor != -11 && mDialogSeekTime != null) {
                mDialogSeekTime.setTextColor(mDialogProgressHighLightColor)
            }
            val localLayoutParams = mProgressDialog.window!!
                .attributes
            localLayoutParams.gravity = Gravity.TOP
            localLayoutParams.width = width
            localLayoutParams.height = height
            val location = IntArray(2)
            getLocationOnScreen(location)
            localLayoutParams.x = location[0]
            localLayoutParams.y = location[1]
            mProgressDialog.window!!.attributes = localLayoutParams
        }
        if (!mProgressDialog.isShowing) {
            mProgressDialog.show()
        }
        if (mDialogSeekTime != null) {
            mDialogSeekTime.text = seekTime
        }
        if (mDialogTotalTime != null) {
            mDialogTotalTime.text = " / $totalTime"
        }
        if (totalTimeDuration > 0) if (mDialogProgressBar != null) {
            mDialogProgressBar.progress = (seekTimePosition * 100 / totalTimeDuration).toInt()
        }
        val offset = ((mSeekTimePosition - currentPositionWhenPlaying) / 1000.0).toInt()
        mDialogOffsetText?.text = if (offset > 0) "+${offset}s" else "${offset}s"
        if (deltaX > 0) {
            if (mDialogIcon != null) {
                mDialogIcon.setBackgroundResource(com.shuyu.gsyvideoplayer.R.drawable.video_forward_icon)
            }
        } else {
            if (mDialogIcon != null) {
                mDialogIcon.setBackgroundResource(com.shuyu.gsyvideoplayer.R.drawable.video_backward_icon)
            }
        }
    }

    override fun showBrightnessDialog(percent: Float) {
        if (mBrightnessDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(
                brightnessLayoutId, null
            )
            mBrightnessDialogTv = localView.findViewById(brightnessTextId)
            mBrightnessDialog = Dialog(activityContext, R.style.video_style_dialog_progress)
            mBrightnessDialog.setContentView(localView)
            mBrightnessDialog.window!!.run {
                addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                decorView.systemUiVisibility = SYSTEM_UI_FLAG_HIDE_NAVIGATION
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val localLayoutParams = mBrightnessDialog.window!!
                .attributes
            localLayoutParams.gravity = Gravity.TOP or Gravity.END
            localLayoutParams.width = width
            localLayoutParams.height = height
            val location = IntArray(2)
            getLocationOnScreen(location)
            localLayoutParams.x = location[0]
            localLayoutParams.y = location[1]
            // 针对异型屏适配
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                localLayoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            mBrightnessDialog.window!!.attributes = localLayoutParams
        }
        super.showBrightnessDialog(percent)
    }

    override fun setSpeed(speed: Float, soundTouch: Boolean) {
        super.setSpeed(speed, soundTouch)
        mPlaySpeedValue.text = "x$speed"
    }


    /**
     * 暂时失去AudioFocus，但是可以继续播放，不过要在降低音量
     */
    override fun onLossTransientCanDuck() {
        // 暂时失去AudioFocus，但是可以继续播放，不过要在降低音量
        miaoLogger() debug "onLossTransientCanDuck"
    }

    /**
     * 暂时失去Audio Focus，并会很快再次获得
     */
    override fun onLossTransientAudio() {
        // 暂时失去Audio Focus，并会很快再次获得。必须停止Audio的播放
        miaoLogger() debug "onLossTransientAudio"
        if (enabledAudioFocus) {
            Handler(Looper.getMainLooper()).post {
                onVideoPause()
            }
        }
    }

    /**
     * 获得了Audio Focus
     */
    override fun onGankAudio() {
        miaoLogger() debug "onGankAudio"
        if (enabledAudioFocus) {
            Handler(Looper.getMainLooper()).post {
                if (currentState == GSYVideoView.CURRENT_STATE_PAUSE) {
                    onVideoResume()
                }
            }
        }
    }

    /**
     * 失去了Audio Focus，并将会持续很长的时间
     */
    override fun onLossAudio() {
        miaoLogger() debug "onLossAudio"
        if (enabledAudioFocus) {
            Handler(Looper.getMainLooper()).post {
               onVideoPause()
            }
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest?.let {
                mAudioManager.requestAudioFocus(it)
                return
            }
        }
        mAudioManager.requestAudioFocus(
            onAudioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest?.let {
                mAudioManager.abandonAudioFocusRequest(it)
                return
            }
        }
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener)
    }

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int, displayCutout: DisplayCutout?) {
        if (mode == PlayerMode.FULL) {
            mTopContainer.setPadding(left, top, right, 0)
            mBottomContainer.setPadding(left, 0, right, 0)
            mLockContainer.setPadding(left, 0, right, 0)
        } else {
            if (mode == PlayerMode.SMALL_FLOAT) {
                mTopContainer.setPadding(0, dip(24), 0, 0)
            } else {
                mTopContainer.setPadding(0, 0, 0, 0)
            }
            mBottomContainer.setPadding(0, 0, 0, 0)
            mLockContainer.setPadding(0, 0, 0, 0)
        }
        mDisplayCutout = displayCutout
        updateDanmakuMargin()
    }

    fun showController() {
        showAllWidget()
        cancelDismissControlViewTimer()
    }

    fun hideController() {
        hideAllWidget()
        cancelDismissControlViewTimer()
    }

    fun showSmallDargBar() {
        if (mode == PlayerMode.SMALL_FLOAT) {
            mDragBarLayout.visibility = VISIBLE
        } else {
            mDragBarLayout.visibility = GONE
        }
    }

    fun hideSmallDargBar() {
        mDragBarLayout.visibility = mTopContainer.visibility
    }

    fun getHoldButtonWidth():Int{
        return mHoldUpBtn.measuredWidth
    }

    fun setHoldStatus(isHold:Boolean){
        if(isHold){
            mDanmakuView.pause()
            setViewShowState(mBottomLayout, GONE)
            setViewShowState(mDanmakuView, GONE)
            setViewShowState(mTopContainer, GONE)
            setViewShowState(mStartButton, GONE)
            isHoldUp=true
        } else {
            isHoldUp=false
            setViewShowState(mBottomLayout, VISIBLE)
            setViewShowState(mDanmakuView, VISIBLE)
            setViewShowState(mTopContainer, VISIBLE)
            setViewShowState(mStartButton, VISIBLE)
            mDanmakuView.resume()
        }

    }
    private fun setDialogVolumeProgressBar(context: Context) {
        val draw = context.getDrawable(R.drawable.shape_video_volume_progress)
        setDialogVolumeProgressBar(draw)
    }

    private fun setDialogProgressBar(context: Context) {
        setDialogProgressColor(context.config.themeColor, mDialogProgressNormalColor)
        val draw = context.getDrawable(R.drawable.shape_video_dialog_progress)
        setDialogProgressBar(draw)
    }

    fun updateTextureViewShowType() {
        changeTextureViewShowType()
    }

    fun updateThemeColor(
        context: Context,
        themeColor: Int,
    ) {
        val draw = PlayerViewDrawable.progressBarDrawable(context, themeColor)
        val bounds = mProgressBar.progressDrawable.bounds
        mProgressBar.progressDrawable = draw
        mProgressBar.progressDrawable.bounds = bounds
        mProgressBar.thumb.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP)
        mBottomProgressBar.progressDrawable = PlayerViewDrawable.bottomProgressBarDrawable(context, themeColor)

        setDialogProgressBar(context)
        setDialogVolumeProgressBar(context)
    }

    /**
     * 锁定控制按钮相关
     */
    inner class OnLockClickListener : OnClickListener {

        val isShowButton get() = mUnlockLeftIV.visibility == VISIBLE

        override fun onClick(v: View) {
            when (v.id) {
                R.id.lock -> {
                    isLock = true
                    postDelayed(dismissControlTask, mDismissControlTime.toLong())
                }
                R.id.layout_lock_screen -> {
                    if (isShowButton) {
                        removeCallbacks(dismissControlTask)
                        hideButton()
                    } else {
                        postDelayed(dismissControlTask, mDismissControlTime.toLong())
                        showButton()
                    }
                }
                R.id.unlock_left, R.id.unlock_right -> {
                    removeCallbacks(dismissControlTask)
                    isLock = false
                }
            }
        }

        private fun showButton() {
            mUnlockLeftIV.visibility = VISIBLE
            mUnlockRightIV.visibility = VISIBLE
        }

        private fun hideButton() {
            mUnlockLeftIV.visibility = GONE
            mUnlockRightIV.visibility = GONE
        }

        var dismissControlTask = Runnable { hideButton() }

    }

    /**
     * 字幕源信息
     */
    data class SubtitleSourceInfo(
        val id: String,
        val lan: String,
        val lan_doc: String,
        val subtitle_url: String,
        val ai_status: Int,
    )

    /**
     * 字幕信息
     */
    data class SubtitleItemInfo(
        val from: Long,
        val to: Long,
        val content: String,
    )
}