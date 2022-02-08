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
import androidx.annotation.RequiresApi
import cn.a10miaomiao.player.callback.MediaPlayerListener
import com.a10miaomiao.bilimiao.R


class PicInPicHelper(
    val activity: Activity,
    val mediaPlayer: MediaPlayerListener
) {

    companion object {
        val ACTION_MEDIA_CONTROL = "media_control"
        val EXTRA_CONTROL_TYPE = "control_type"

        val CONTROL_TYPE_PLAY = 1
        val CONTROL_TYPE_PAUSE = 2

        val REQUEST_TYPE_PLAY = 1
        val REQUEST_TYPE_PAUSE = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val builder = PictureInPictureParams.Builder()

    private val actions: List<RemoteAction> @RequiresApi(Build.VERSION_CODES.O)
    get() {
        val action = if (mediaPlayer.isPlaying){
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
        }else{
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
        return listOf(
            action
        )
    }


    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.action != ACTION_MEDIA_CONTROL) {
                return
            }

            val controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
            when (controlType) {
                CONTROL_TYPE_PLAY -> {
                    mediaPlayer.start()
                    updatePictureInPictureActions()
                }
                CONTROL_TYPE_PAUSE -> {
                    mediaPlayer.pause()
                    updatePictureInPictureActions()
                }
            }
        }
    }

    fun enterPictureInPictureMode(){
        // 判断Android版本是否大于等于8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
            val aspectRatio = Rational(16, 9);
            // 设置画中画窗口的宽高比例
            builder.setAspectRatio(aspectRatio)
            builder.setActions(actions)
            // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
            activity.enterPictureInPictureMode(builder.build());
        };
    }

    fun updatePictureInPictureActions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setActions(actions)
            activity.setPictureInPictureParams(builder.build());
        }
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean){
        if (isInPictureInPictureMode) {
            activity.registerReceiver(broadcastReceiver,  IntentFilter(ACTION_MEDIA_CONTROL))
        }else{
            activity.unregisterReceiver(broadcastReceiver)
        }

    }

}