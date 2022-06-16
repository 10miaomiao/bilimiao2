package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import android.os.Bundle
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate

interface BasePlayerDelegate: BaseDelegate {
    fun playVideo(aid: String, cid: String, title: String)
    fun playBangumi(sid: String, epid: String, cid: String, title: String)
    fun playLocalVideo(biliVideo: BiliVideoEntry)
    fun updateDanmukuSetting()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?)
}