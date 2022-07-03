package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView


class DanmakuVideoPlayer : StandardGSYVideoPlayer  {

    private val mDanmakuView: DanmakuView by lazy { findViewById(R.id.danmaku_view) }
    private val mRootLayout: RelativeLayout by lazy { findViewById(R.id.root_layout) }
    private val mDanmakuTime = object : DanmakuTimer() {
        private var lastTime = 0L
        override fun currMillisecond(): Long {
            val currentPosition: Long = if (mCurrentState == CURRENT_STATE_PLAYING
                || mCurrentState == CURRENT_STATE_PAUSE) {
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

    var danmakuStartSeekPosition: Long = -1
    var danmakuParser: BaseDanmakuParser? = null
        set(value) {
            if (value != null) {
                value.timer = mDanmakuTime
            }
            field = value
        }
    var danmakuContext: DanmakuContext? = null
//    private var mDanmakuContext: DanmakuContext by lazy { DanmakuContext.create() }

    constructor(context: Context?, fullFlag: Boolean?): super(context, fullFlag) {
        initView()
    }

    constructor(context: Context?): super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs) {
        initView()
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_danmaku_palyer
    }

    private fun initView() {
        initDanmakuContext()
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
//            if (mDanmaKuShow) {
//                if (!getDanmakuView().isShown()) getDanmakuView().show()
//                mToogleDanmaku.setText("弹幕关")
//            } else {
//                if (getDanmakuView().isShown()) {
//                    getDanmakuView().hide()
//                }
//                mToogleDanmaku.setText("弹幕开")
//            }
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


}