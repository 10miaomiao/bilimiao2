package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration
import android.os.Bundle
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.comm.delegate.BaseDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BasePlayerSource

interface BasePlayerDelegate: BaseDelegate {
    fun openPlayer(source: BasePlayerSource)
    fun updateDanmukuSetting()
    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?)
}