package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo

interface BasePlayerDelegate : BaseDelegate {
    fun openPlayer(source: BasePlayerSource)
    fun closePlayer()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean)
    fun isOpened(): Boolean
    fun isPlaying(): Boolean
    fun isPause(): Boolean
    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int)
    fun onConfigurationChanged(orientation: Int)
    fun getSourceIds(): PlayerSourceIds
    fun currentPosition(): Long
    fun sendDanmaku(type: Int, danmakuText: String, danmakuTextSize: Float, danmakuTextColor: Int, danmakuPosition: Long)
    fun setProxy(proxyServer: ProxyServerInfo, uposHost: String)
}
