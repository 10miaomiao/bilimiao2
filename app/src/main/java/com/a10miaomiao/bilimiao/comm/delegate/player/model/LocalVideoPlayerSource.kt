package com.a10miaomiao.bilimiao.comm.delegate.player.model

import android.app.Activity
import android.net.Uri
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadFlieHelper
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.widget.player.BiliDanmukuParser
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.google.gson.Gson
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.io.File
import java.io.InputStream

class LocalVideoPlayerSource(
    val activity: Activity,
    val localEntry: BiliVideoEntry
): BasePlayerSource() {

    override val id: String
        get() = localEntry.page_data.cid.toString()

    override val title: String
        get() = localEntry.title

    override val coverUrl: String
        get() = localEntry.cover

    override val ownerId: String
        get() = localEntry.owner_id.toString()

    override val ownerName: String
        get() = "本地视频"

    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
        localEntry.owner_id
        val duration = localEntry.total_time_milli
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

            return PlayerSourceInfo().also {
                it.url = url
                it.quality = 0
                it.acceptList = acceptList
                it.duration = duration
            }
        }

        val dashJsonFile = File(
            videoDir, "index.json"
        )
        if (dashJsonFile.exists()) {
            val dashJsonStr = dashJsonFile.readText()
            val dashJsonInfo = Gson().fromJson(dashJsonStr, PlayerAPI.Dash::class.java)
            val videoFile = File(videoDir, "video.m4s")
            val audioFile = File(videoDir, "audio.m4s")
            val url = Uri.fromFile(videoFile).toString()
            val audioUrl = Uri.fromFile(audioFile).toString()
            val mergingUrl = "[local-merging]\n$url\n$audioUrl"
            return PlayerSourceInfo().also {
                it.url = mergingUrl
                it.quality = 0
                it.acceptList = acceptList
                it.duration = duration
            }
        }
        return PlayerSourceInfo().also {
            it.url = ""
            it.quality = -1
            it.acceptList = acceptList
            it.duration = duration
        }
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

    private fun getBiliDanmukuStream(): InputStream? {
        val videoDir = DownloadFlieHelper.getDownloadFileDir(activity, localEntry)
        val danmakuXMLFile = File(videoDir, "danmaku.xml")
        return danmakuXMLFile.inputStream()
    }

    override suspend fun getSubtitles(): List<SubtitleSourceInfo> = emptyList()
}