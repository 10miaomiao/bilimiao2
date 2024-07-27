package com.a10miaomiao.bilimiao.comm.player

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.shuyu.gsyvideoplayer.player.PlayerFactory

object BilimiaoPlayerManager {

    @OptIn(UnstableApi::class)
    fun initConfig() {
        // AV1
        PlayerFactory.setPlayManager(Libgav1Media3ExoPlayerManager::class.java)
    }

}