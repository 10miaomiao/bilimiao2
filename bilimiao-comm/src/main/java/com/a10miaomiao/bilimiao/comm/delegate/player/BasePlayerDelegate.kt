package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo

interface BasePlayerDelegate: BaseDelegate {
    fun openPlayer(source: BasePlayerSource)
    fun closePlayer()
    fun updateDanmukuSetting()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?)
    fun isPlaying(): Boolean
    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int)
    fun onConfigurationChanged(newConfig: Configuration)

    fun setProxy(proxyServer: ProxyServerInfo, uposHost: String, )
}