package com.a10miaomiao.bilimiao.comm.delegate.player.model

import android.app.Activity
import android.net.Uri
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadFlieHelper
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.widget.player.BiliDanmukuParser
import com.google.gson.Gson
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class LocalVideoPlayerSource(
    val activity: Activity,
    val localEntry: BiliVideoEntry
): BasePlayerSource {

    override val title: String
        get() = localEntry.title

    override val coverUrl: String
        get() = localEntry.cover

    override suspend fun getPlayerUrl(quality: Int): PlayerSourceInfo {
        val videoDir = DownloadFlieHelper.getVideoPageFileDir(activity, localEntry)
        val pageData = DownloadFlieHelper.getVideoPage(activity, localEntry)
        val videoFlie = File(
            videoDir, "0" + "." + pageData.format
        )
        val acceptList = listOf(
            PlayerSourceInfo.AcceptInfo(0, "本地")
        )
        if (videoFlie.exists()) {
            val url = Uri.fromFile(videoFlie).toString()
            return PlayerSourceInfo(url, 0, acceptList)
        }

        val dashJsonFile = File(
            videoDir, "index.json"
        )
        if (dashJsonFile.exists()) {
            val dashJsonStr = dashJsonFile.readText()
            val dashJsonInfo = Gson().fromJson(dashJsonStr, PlayerAPI.Dash::class.java)
            val mdpUrl = LocalDashSource(videoDir.absolutePath, PlayerAPI.Dash(
                duration = localEntry.total_time_milli / 1000,
                min_buffer_time = 1.5,
                video = dashJsonInfo.video,
                audio = dashJsonInfo.audio,
            )).getMDPUrl()
            return PlayerSourceInfo(mdpUrl, 0, acceptList)
        }
        return PlayerSourceInfo("", -1, acceptList)
    }

    override suspend fun getDanmakuParser(): BaseDanmakuParser? {
        val inputStream = getBiliDanmukuStream()
        return if (inputStream == null) {
            null
        } else {
            val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            loader.load(inputStream)
            val parser = BiliDanmukuParser()
            val dataSource = loader.dataSource
            parser.load(dataSource)
            parser
        }
    }

    private suspend fun getBiliDanmukuStream(): InputStream? {
        val videoDir = DownloadFlieHelper.getDownloadFileDir(activity, localEntry)
        val danmakuXMLFile = File(videoDir, "danmaku.xml")
        return danmakuXMLFile.inputStream()
    }

}