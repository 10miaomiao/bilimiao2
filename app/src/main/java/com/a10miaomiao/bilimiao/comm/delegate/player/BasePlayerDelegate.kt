package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import android.os.Bundle
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BasePlayerSource
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

interface BasePlayerDelegate: BaseDelegate {
    fun openPlayer(source: BasePlayerSource)
    fun closePlayer()
    fun updateDanmukuSetting()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?)
    fun isPlaying(): Boolean
    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int)
    fun onConfigurationChanged(newConfig: Configuration)
}