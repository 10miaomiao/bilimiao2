package com.a10miaomiao.bilimiao.comm.delegate.player

import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.R
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class PlayerViews(
    private var activity: AppCompatActivity,
) {

    val videoPlayer = activity.findViewById<StandardGSYVideoPlayer>(R.id.video_player)

}