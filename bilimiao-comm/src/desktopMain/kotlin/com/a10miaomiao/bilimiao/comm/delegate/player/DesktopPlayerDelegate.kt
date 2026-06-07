package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo

class DesktopPlayerDelegate : BasePlayerDelegate {
    override fun onCreate() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onStart() {}
    override fun onStop() {}
    override fun onDestroy() {}
    override fun onBackPressed(): Boolean = false
    override fun openPlayer(source: BasePlayerSource) {}
    override fun closePlayer() {}
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {}
    override fun isOpened(): Boolean = false
    override fun isPlaying(): Boolean = false
    override fun isPause(): Boolean = true
    override fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {}
    override fun onConfigurationChanged(orientation: Int) {}
    override fun getSourceIds(): PlayerSourceIds = PlayerSourceIds()
    override fun currentPosition(): Long = 0L
    override fun sendDanmaku(type: Int, danmakuText: String, danmakuTextSize: Float, danmakuTextColor: Int, danmakuPosition: Long) {}
    override fun setProxy(proxyServer: ProxyServerInfo, uposHost: String) {}
}
