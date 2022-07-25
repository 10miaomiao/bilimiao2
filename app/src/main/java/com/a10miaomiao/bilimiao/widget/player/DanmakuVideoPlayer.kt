package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView


class DanmakuVideoPlayer : StandardGSYVideoPlayer {

    enum class PlayerMode {
        SMALL,
        FULL
    }

    private val mDanmakuView: DanmakuView by lazy { findViewById(R.id.danmaku_view) }
    private val mRootLayout: RelativeLayout by lazy { findViewById(R.id.root_layout) }
    private val mMoreBtn: View by lazy { findViewById(R.id.more) }
    private val mBottomLayout: RelativeLayout by lazy { findViewById(R.id.layout_bottom) }
    private val mFullModeBottomContainer: ViewGroup by lazy { findViewById(R.id.layout_full_mode_bottom) }
    private val mButtomPlay: ImageView by lazy { findViewById(R.id.buttom_play) }
    private val mDanmakuSwitch: ViewGroup by lazy { findViewById(R.id.danmaku_switch) }
    private val mDanmakuSwitchIV: ImageView by lazy { findViewById(R.id.danmaku_switch_icon) }
    private val mDanmakuSwitchTV: TextView by lazy { findViewById(R.id.danmaku_switch_text) }
    private val mQuality: ViewGroup by lazy { findViewById(R.id.quality) }
    private val mQualityTV: TextView by lazy { findViewById(R.id.quality_text) }
    private val mLock: ViewGroup by lazy { findViewById(R.id.lock) }
    private val mLockContainer: ViewGroup by lazy { findViewById(R.id.layout_lock_screen) }
    private val mUnlockLeftIV: ImageView by lazy { findViewById(R.id.unlock_left) }
    private val mUnlockRightIV: ImageView by lazy { findViewById(R.id.unlock_right) }

    private val mDanmakuTime = object : DanmakuTimer() {
        private var lastTime = 0L
        override fun currMillisecond(): Long {
            val currentPosition: Long = if (mCurrentState == CURRENT_STATE_PLAYING
                || mCurrentState == CURRENT_STATE_PAUSE
            ) {
                try {
                    gsyVideoManager.currentPosition
                } catch (e: Exception) {
                    0L
                }
            } else {
                mCurrentPosition
            }
            if (
                currentPosition < lastTime && lastTime - currentPosition < 1000
            ) {
                return lastTime
            }
            lastTime = currentPosition
            return currentPosition
        }

        override fun update(curr: Long): Long {
            lastInterval = curr - lastTime
            return lastInterval
        }
    }

    var mode = PlayerMode.SMALL
        set(value) {
            field = value
            updateMode()
        }
    var isShowDanmaKu = true
        set(value) {
            field = value
            resolveDanmakuShow()
        }
    var danmakuStartSeekPosition: Long = -1
    var danmakuParser: BaseDanmakuParser? = null
        set(value) {
            if (value != null) {
                value.timer = mDanmakuTime
            }
            field = value
        }
    var danmakuContext: DanmakuContext? = null
    var statusBarHelper: StatusBarHelper? = null

    val topContainer: ViewGroup get() = mTopContainer
    val qualityView: View get() = mQuality
    val moreBtn: View get() = mMoreBtn
//    private var mDanmakuContext: DanmakuContext by lazy { DanmakuContext.create() }

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
        enlargeImageRes = R.drawable.ic_player_portrait_fullscreen
        shrinkImageRes = R.drawable.ic_player_portrait_fullscreen
        initDanmakuContext()
        mButtomPlay.setOnClickListener {
            clickStartIcon()
        }
        mDanmakuSwitch.setOnClickListener {
            isShowDanmaKu = !isShowDanmaKu
            cancelDismissControlViewTimer()
            startDismissControlViewTimer()
        }

        val lockClickListener = OnLockClickListener()
        mLock.setOnClickListener(lockClickListener)
        mLockContainer.setOnClickListener(lockClickListener)
        mUnlockLeftIV.setOnClickListener(lockClickListener)
        mUnlockRightIV.setOnClickListener(lockClickListener)
    }

    private fun updateMode() {
        when (mode) {
            PlayerMode.SMALL -> {
                mFullModeBottomContainer.visibility = GONE
                mBackButton.setImageResource(R.drawable.video_small_close)
            }
            PlayerMode.FULL -> {
                mFullModeBottomContainer.visibility = VISIBLE
                mBackButton.setImageResource(R.drawable.bili_player_back_button)
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
    }

    override fun setViewShowState(view: View, visibility: Int) {
        super.setViewShowState(view, visibility)
        if (mode == PlayerMode.FULL && view == mTopContainer) {
            statusBarHelper?.isShowStatus = visibility == View.VISIBLE
        }
    }

    override fun onPrepared() {
        super.onPrepared()
        onPrepareDanmaku(this)
    }

    override fun onVideoPause() {
        super.onVideoPause()
        danmakuOnPause()
    }

    override fun onVideoResume(isResume: Boolean) {
        super.onVideoResume(isResume)
        danmakuOnResume()
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
        releaseDanmaku()
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

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        if (mode == PlayerMode.FULL) {
            mTopContainer.setPadding(left, top, right, 0)
            mBottomContainer.setPadding(left, 0, right, 0)
            mLockContainer.setPadding(left, 0, right, 0)
        } else {
            mTopContainer.setPadding(0, 0, 0, 0)
            mBottomContainer.setPadding(0, 0, 0, 0)
            mLockContainer.setPadding(0, 0, 0, 0)
        }
    }

    fun hideController() {
        hideAllWidget()
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

}