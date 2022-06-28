package com.a10miaomiao.bilimiao.comm.delegate.player.model

import android.app.Activity
import android.net.Uri
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadFlieHelper
import java.io.File

class LocalVideoPlayerSource(
    val activity: Activity,
    val localEntry: BiliVideoEntry
): BasePlayerSource {

    override val title: String
        get() = localEntry.title

    override suspend fun getPlayerUrl(quality: Int): String {
        val videoDir = DownloadFlieHelper.getVideoPageFileDir(activity, localEntry)
        val pageData = DownloadFlieHelper.getVideoPage(activity, localEntry)
        val videoFlie = File(
            videoDir, "0" + "." + pageData.format
        )
        return Uri.fromFile(videoFlie).toString()
    }


}