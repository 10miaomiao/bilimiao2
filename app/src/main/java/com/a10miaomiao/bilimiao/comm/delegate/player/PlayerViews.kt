package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class PlayerViews(
    private var activity: AppCompatActivity,
) {

    val videoPlayer = activity.findViewById<DanmakuVideoPlayer>(R.id.video_player)
}