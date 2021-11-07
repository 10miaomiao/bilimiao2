package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import cn.a10miaomiao.player.MyMediaController
import cn.a10miaomiao.player.VideoPlayerView
import cn.a10miaomiao.player.callback.MediaController
import cn.a10miaomiao.player.callback.MediaPlayerListener
import com.a10miaomiao.bilimiao.R
import kotlin.reflect.KFunction1


class MiniMediaController : FrameLayout, MediaController, View.OnClickListener, View.OnTouchListener {

    private val mToolbar: Toolbar by lazy { findViewById(R.id.mToolbar) }
    private val mPauseButton: ImageView by lazy { findViewById(R.id.mPauseButton) }
    private val mSeekBar: SeekBar by lazy { findViewById(R.id.mSeekBar) }
    private val mZoomIv: ImageView by lazy { findViewById(R.id.mZoomIv) }
    private val mCurrentTime: TextView by lazy { findViewById(R.id.mCurrentTime) }
    private val mEndTime: TextView by lazy { findViewById(R.id.mEndTime) }

    var mMediaPlayer: MediaPlayerListener? = null
    var restartPlayEvent: ((Long) -> Unit)? = null

    private var mDuration = 0L
    private var mDragging = false

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.layout_mini_media_controller, this)
        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        mToolbar.inflateMenu(R.menu.mini_player_toolbar)
        mPauseButton.setOnClickListener(this)

        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mDragging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mDragging = false
                try {
                    val state = mMediaPlayer?.state ?: VideoPlayerView.STATE_IDLE
                    if (state == VideoPlayerView.STATE_PLAYBACK_COMPLETED) {
                        restartPlayEvent?.invoke(seekBar.progress.toLong())
                    } else {
                        mMediaPlayer?.seekTo(mDuration * seekBar.progress / 1000L)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun show() {
        visibility = View.VISIBLE
    }

    override fun show(timeout: Int) {
        visibility = View.VISIBLE
    }

    override fun hide() {
        visibility = View.GONE
    }

    override fun isShowing(): Boolean {
        return visibility == View.VISIBLE
    }

    override fun setMediaPlayer(player: MediaPlayerListener) {
        mMediaPlayer = player
    }

    override fun setAnchorView(v: View) {

    }

    override fun setTitle(title: String) {
        mToolbar.title = title
    }

    override fun onClick(v: View) {
        val mPlayer = mMediaPlayer ?: return
        if (mPlayer.isPlaying) {
            mPlayer.pause()
            mPauseButton.setImageResource(cn.a10miaomiao.player.R.drawable.bili_player_play_can_play)
        } else {
            if (mPlayer.state == VideoPlayerView.STATE_PLAYBACK_COMPLETED) {
                restartPlayEvent?.invoke(0L)
            } else {
                mPlayer.start()
                mPauseButton.setImageResource(cn.a10miaomiao.player.R.drawable.bili_player_play_can_pause)
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return false
    }

    fun setBackOnClick(onClickListener: OnClickListener) {
        mToolbar.setNavigationOnClickListener(onClickListener)
    }

    fun setZoomOnClick(onClickListener: OnClickListener) {
        mZoomIv.setOnClickListener(onClickListener)
    }

    fun setOnMenuItemClickListener(onMenuItemClickListener: KFunction1<MenuItem, Boolean>) {
        mToolbar.setOnMenuItemClickListener(onMenuItemClickListener)
    }

    /**
     * 设置播放进度
     */
    fun setProgress(): Long {
        val mPlayer = mMediaPlayer
        if (mPlayer == null || mDragging) {
            return 0
        }
        val position = mPlayer.currentPosition
        val duration = mPlayer.duration
        if (mSeekBar != null) {
            if (duration > 0) {
                val pos = 1000L * position / duration
                mSeekBar.progress = pos.toInt()
            }
            val percent = mPlayer.bufferPercentage
            mSeekBar.secondaryProgress = (percent * 10).toInt()
        }
        mDuration = duration
        mEndTime.text = MyMediaController.generateTime(duration)
        mCurrentTime.text = MyMediaController.generateTime(position)
        return position
    }

    fun setProgress(position: Long) {
        val mPlayer = mMediaPlayer
        if (mPlayer == null || mDragging) {
            return
        }
        val duration = mPlayer.duration
        val pos = 1000L * position / duration
        mSeekBar.progress = pos.toInt()
        mCurrentTime.text = MyMediaController.generateTime(position)
    }

    fun updatePausePlay() {
        val mPlayer = mMediaPlayer
        if (mPlayer != null) {
            if (mPlayer.isPlaying) {
                mPauseButton.setImageResource(cn.a10miaomiao.player.R.drawable.bili_player_play_can_pause)
            } else {
                mPauseButton.setImageResource(cn.a10miaomiao.player.R.drawable.bili_player_play_can_play)
            }
        }
    }

    fun updateColor(color: Int){
        val draw = context.getDrawable(R.drawable.layer_progress)
        val bounds = mSeekBar.progressDrawable.bounds
        mSeekBar.progressDrawable = draw
        mSeekBar.progressDrawable.bounds = bounds
        mSeekBar.thumb.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

}
