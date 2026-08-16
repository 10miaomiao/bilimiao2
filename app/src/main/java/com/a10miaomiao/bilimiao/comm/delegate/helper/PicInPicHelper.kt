package com.a10miaomiao.bilimiao.comm.delegate.helper

import android.app.Activity
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.a10miaomiao.bilimiao.R

/**
 * 画中画辅助类（简化版，不再依赖 GSY DanmakuVideoPlayer）
 *
 * 新架构基于 mediamp (ExoPlayer)，画中画由 Activity 级别处理。
 * 视频画面通过 Compose VideoScaffold 渲染，进入画中画模式时自动全屏。
 *
 * 原有 GSY 相关的播放器操作（暂停/播放/Seek 远程控制）由 PlaybackService 的 MediaSession 接管。
 */
@RequiresApi(Build.VERSION_CODES.O)
class PicInPicHelper(
    val activity: Activity,
) {

    companion object {
        val ACTION_PLAY = "cn.a10miaomiao.bilimiao.action.play"
        val ACTION_PAUSE = "cn.a10miaomiao.bilimiao.action.pause"
        val ACTION_NEXT = "cn.a10miaomiao.bilimiao.action.next"
        val ACTION_PREV = "cn.a10miaomiao.bilimiao.action.prev"
    }

    private val builder = PictureInPictureParams.Builder()

    var isInPictureInPictureMode = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 画中画远程控制由 PlaybackService 的 MediaSession 处理
            // 此 Receiver 保留用于兼容旧逻辑，新架构下可空实现
        }
    }

    /**
     * 进入画中画模式
     * @param aspectRatio 宽高比
     */
    fun enterPictureInPictureMode(aspectRatio: Rational) {
        try {
            builder.setAspectRatio(aspectRatio).build()
            activity.enterPictureInPictureMode(
                builder.build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        this.isInPictureInPictureMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            try {
                activity.registerReceiver(
                    broadcastReceiver,
                    IntentFilter().apply {
                        addAction(ACTION_PLAY)
                        addAction(ACTION_PAUSE)
                        addAction(ACTION_NEXT)
                        addAction(ACTION_PREV)
                    },
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
            } catch (_: Exception) {
            }
        } else {
            try {
                activity.unregisterReceiver(broadcastReceiver)
            } catch (_: Exception) {
            }
        }
    }
}
