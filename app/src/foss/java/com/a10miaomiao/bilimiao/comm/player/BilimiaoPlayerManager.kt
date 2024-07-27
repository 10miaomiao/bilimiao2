package com.a10miaomiao.bilimiao.comm.player

import com.a10miaomiao.bilimiao.widget.player.media3.Media3ExoPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory

object BilimiaoPlayerManager {

    fun initConfig() {
        PlayerFactory.setPlayManager(Media3ExoPlayerManager::class.java)
    }

}