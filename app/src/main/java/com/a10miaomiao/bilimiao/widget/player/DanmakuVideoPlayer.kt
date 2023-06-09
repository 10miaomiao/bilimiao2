package com.a10miaomiao.bilimiao.widget.player

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import splitties.dimensions.dip
import splitties.views.backgroundColor


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
    private val mDragBar: View by lazy { findViewById(R.id.drag_bar) }

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


    // 弹幕时间与播放器时间同步
    private val mDanmakuTime = object : DanmakuTimer() {
        private var lastTime = 0L
        override fun currMillisecond(): Long {
            return try {
                gsyVideoManager.currentPosition
            } catch (e: Exception) {
                0L
            }
        }

        override fun update(curr: Long): Long {
            lastInterval = curr - lastTime
            return lastInterval
        }
    }

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

    // 是否显示当面
    var isShowDanmaKu = true
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
        isShowDragProgressTextOnSeekBar = true
        enlargeImageRes = R.drawable.ic_player_portrait_fullscreen
        shrinkImageRes = R.drawable.ic_player_portrait_fullscreen
        setDialogVolumeProgressBar(context)
        setDialogProgressBar(context)
        initDanmakuContext()
        mButtomPlay.setOnClickListener {
            clickStartIcon()
        }
        mDanmakuSwitch.setOnClickListener {
            isShowDanmaKu = !isShowDanmaKu
            cancelDismissControlViewTimer()
            startDismissControlViewTimer()
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
    }

    private fun updateMode() {
        when (mode) {
            PlayerMode.SMALL_TOP, PlayerMode.SMALL_FLOAT -> {
                mFullModeBottomContainer.visibility = GONE
                mPlaySpeedName.visibility = GONE
                mBackButton.setImageResource(R.drawable.video_small_close)
                if (mode == PlayerMode.SMALL_FLOAT) {
                    mDragBar.visibility = mTopContainer.visibility
                } else {
                    mDragBar.visibility = GONE
                }
            }
            PlayerMode.FULL -> {
                mFullModeBottomContainer.visibility = VISIBLE
                mPlaySpeedName.visibility = VISIBLE
                mBackButton.setImageResource(R.drawable.bili_player_back_button)
                mDragBar.visibility = GONE
            }
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

    // start 长按倍速播放
    private var touchSurfaceDownTime = Long.MAX_VALUE
    private var isSpeedPlaying = false
    private val longClickControlTask = Runnable {
        if (System.currentTimeMillis() - touchSurfaceDownTime >= 500
            && mCurrentState == CURRENT_STATE_PLAYING
            && !mChangePosition && !mChangeVolume && !mBrightness) {
            isSpeedPlaying = true
            speed = 2f
            mSpeedTips.visibility = View.VISIBLE
            mTouchingProgressBar = false
            (mSpeedTipsIV.drawable as? AnimationDrawable)?.start()
        }
    }

    override fun touchSurfaceDown(x: Float, y: Float) {
        super.touchSurfaceDown(x, y)
        touchSurfaceDownTime = System.currentTimeMillis()
        postDelayed(longClickControlTask, 500)
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (isSpeedPlaying) {
            return
        }
        super.touchSurfaceMove(deltaX, deltaY, y)
    }

    override fun touchSurfaceUp() {
        removeCallbacks(longClickControlTask)
        touchSurfaceDownTime = Long.MAX_VALUE
        if (isSpeedPlaying) {
            mSpeedTips.visibility = View.GONE
            isSpeedPlaying = false
            speed = 1f
            (mSpeedTipsIV.drawable as? AnimationDrawable)?.stop()
        } else {
            super.touchSurfaceUp()
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
        } else {
            super.setViewShowState(view, visibility)
            if (view.id == mBottomLayout.id) {
                mBottomSubtitleTV.translationY =
                    if (visibility == VISIBLE) 0f else dip(40).toFloat()
                when (mode) {
                    PlayerMode.SMALL_FLOAT -> {
                        mDragBar.visibility = visibility
                    }
                    PlayerMode.SMALL_TOP -> {
                        mDragBar.visibility = View.GONE
                    }
                    PlayerMode.FULL -> {
                        statusBarHelper?.isShowStatus = visibility == View.VISIBLE
                    }
                }
            }
        }
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
        DebugMiao.log("onAutoCompletion")
    }

    override fun onVideoPause() {
        super.onVideoPause()
        danmakuOnPause()
        videoPlayerCallBack?.onVideoPause()
    }

    override fun onVideoResume(isResume: Boolean) {
        super.onVideoResume(isResume)
        danmakuOnResume()
        videoPlayerCallBack?.onVideoResume(isResume)
    }

    override fun clickStartIcon() {
        super.clickStartIcon()
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            danmakuOnResume()
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            danmakuOnPause()
        }
    }

    override fun onCompletion() {
        super.onCompletion()
        releaseDanmaku()
        DebugMiao.log("onCompletion")
    }

    override fun release() {
        releaseDanmaku()
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
        if (mDanmakuView != null && mDanmakuView.isPaused) {
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
            if (isShowDanmaKu) {
                if (!mDanmakuView.isShown) {
                    mDanmakuView.show()
                }
                mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_open)
                mDanmakuSwitchTV.text = "弹幕开"
            } else {
                if (mDanmakuView.isShown) {
                    mDanmakuView.hide()
                }
                mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_closed)
                mDanmakuSwitchTV.text = "弹幕关"
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
    private fun addDanmaku(danmaku: BaseDanmaku) {
        mDanmakuView.addDanmaku(danmaku)
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

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        if (mode == PlayerMode.FULL) {
            mTopContainer.setPadding(left, top, right, 0)
            mBottomContainer.setPadding(left, 0, right, 0)
            mLockContainer.setPadding(left, 0, right, 0)
        } else {
            if (mode == PlayerMode.SMALL_FLOAT) {
                mTopContainer.setPadding(0, dip(15), 0, 0)
            } else {
                mTopContainer.setPadding(0, 0, 0, 0)
            }
            mBottomContainer.setPadding(0, 0, 0, 0)
            mLockContainer.setPadding(0, 0, 0, 0)
        }
    }

    fun hideController() {
        hideAllWidget()
    }

    fun showSmallDargBar() {
        post {
            if (mode == PlayerMode.SMALL_FLOAT) {
                mDragBar.visibility = VISIBLE
            } else {
                mDragBar.visibility = GONE
            }
        }
    }

    fun hideSmallDargBar() {
        post {
            mDragBar.visibility = mTopContainer.visibility
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

    fun updateThemeColor(
        context: Context,
        themeColor: Int,
    ) {
        val draw = context.getDrawable(R.drawable.layer_progress)
        val bounds = mProgressBar.progressDrawable.bounds
        mProgressBar.progressDrawable = draw
        mProgressBar.progressDrawable.bounds = bounds
        mProgressBar.thumb.setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP)
        mBottomProgressBar.progressDrawable = context.getDrawable(R.drawable.shape_bottom_progress)

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