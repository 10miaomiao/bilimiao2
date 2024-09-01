package com.a10miaomiao.bilimiao.comm.delegate.helper

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
//import cn.a10miaomiao.player.callback.MediaPlayerListener
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer


@RequiresApi(Build.VERSION_CODES.O)
class PicInPicHelper(
    val activity: Activity,
    val videoPlayer: DanmakuVideoPlayer
) {

    companion object {
        val ACTION_MEDIA_CONTROL = "media_control"
        val EXTRA_CONTROL_TYPE = "control_type"

        val CONTROL_TYPE_PLAY = 1
        val CONTROL_TYPE_PAUSE = 2

        val REQUEST_TYPE_PLAY = 1
        val REQUEST_TYPE_PAUSE = 2
    }


    private val builder = PictureInPictureParams.Builder()

    var isInPictureInPictureMode = false


    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.action != ACTION_MEDIA_CONTROL) {
                return
            }
            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_TYPE_PLAY -> {
                    videoPlayer.onVideoResume()
                }
                CONTROL_TYPE_PAUSE -> {
                    videoPlayer.onVideoPause()
                }
            }
        }
    }

    fun enterPictureInPictureMode(aspectRatio: Rational) {
        // 判断Android版本是否大于等于8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 设置画中画窗口的宽高比例
            builder.setAspectRatio(aspectRatio)
            builder.setActions(getActions(videoPlayer.currentState))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setSeamlessResizeEnabled(true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                builder.setTitle(videoPlayer.findViewById<TextView>(R.id.title).text)
            }
            // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
            activity.enterPictureInPictureMode(builder.build());
        };
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getActions(state: Int): List<RemoteAction> {
        val action = if (state == GSYVideoPlayer.CURRENT_STATE_PLAYING) {
            RemoteAction(
                Icon.createWithResource(activity, R.drawable.bili_player_play_can_pause),
                "暂停",
                "",
                PendingIntent.getBroadcast(
                    activity,
                    REQUEST_TYPE_PAUSE,
                    Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_PAUSE),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
        } else {
            RemoteAction(
                Icon.createWithResource(activity, R.drawable.bili_player_play_can_play),
                "播放",
                "",
                PendingIntent.getBroadcast(
                    activity,
                    REQUEST_TYPE_PLAY,
                    Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_PLAY),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
        }
        return listOf(action)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePictureInPictureActions(state: Int) {
        builder.setActions(getActions(state))
        activity.setPictureInPictureParams(builder.build());
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        this.isInPictureInPictureMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            ContextCompat.registerReceiver(
                activity,
                broadcastReceiver,
                IntentFilter(ACTION_MEDIA_CONTROL),
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            activity.unregisterReceiver(broadcastReceiver)
        }
    }

}