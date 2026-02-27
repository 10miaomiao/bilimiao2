package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer

interface PlayerViews {
    val videoPlayer: DanmakuVideoPlayer

    fun <T : View> findViewById(@IdRes id: Int): T

}