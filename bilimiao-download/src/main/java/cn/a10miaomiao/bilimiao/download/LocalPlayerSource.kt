package cn.a10miaomiao.bilimiao.download

import android.app.Activity
import android.net.Uri
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.BiliDanmukuParser
import java.io.File
import java.io.InputStream

class LocalPlayerSource(
    val activity: Activity,
    val entryDirPath: String,
    override val id: String,
    override val title: String,
    override val coverUrl: String,
): BasePlayerSource() {

    override val ownerId: String
        get() = ""

    override val ownerName: String
        get() = "本地视频"

    private val entry = getEntryFileInfo()

    override suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo {
        val duration = entry.total_time_milli
        val acceptList = listOf(
            PlayerSourceInfo.AcceptInfo(0, "本地")
        )
        val emptyPlayerSourceInfo = PlayerSourceInfo().also {
            it.url = ""
            it.quality = -1
            it.acceptList = acceptList
            it.duration = duration
        }

        val videoDirPath = entryDirPath + "/" + entry.type_tag
        val videoDir = File(videoDirPath)
        if (!videoDir.exists() || !videoDir.isDirectory) {
            return emptyPlayerSourceInfo
        }
        val videoIndexJsonFile = File(videoDirPath, "index.json")
        if (!videoIndexJsonFile.exists()) {
            return emptyPlayerSourceInfo
        }
        val videoIndexJson = videoIndexJsonFile.readText()
        if (entry.media_type == 1) {
            val mediaInfo = MiaoJson.fromJson<BiliDownloadMediaFileInfo.Type1>(videoIndexJson)
            val videoFile = File(
                videoDir, "0" + "." + mediaInfo.format
            )
            if (videoFile.exists()) {
                val url = Uri.fromFile(videoFile).toString()
                return PlayerSourceInfo().also {
                    it.url = url
                    it.quality = 0
                    it.acceptList = acceptList
                    it.duration = duration
                }
            } else {
                return emptyPlayerSourceInfo
            }
        } else {
            val mediaInfo = MiaoJson.fromJson<BiliDownloadMediaFileInfo.Type2>(videoIndexJson)
            val videoFile = File(videoDir, "video.m4s")
            val audioFile = File(videoDir, "audio.m4s")
            val url = Uri.fromFile(videoFile).toString()
            if (audioFile.exists()) {
                val audioUrl = Uri.fromFile(audioFile).toString()
                val mergingUrl = "[local-merging]\n$url\n$audioUrl"
                return PlayerSourceInfo().also {
                    it.height = mediaInfo.video[0].height
                    it.width = mediaInfo.video[0].width
                    it.url = mergingUrl
                    it.quality = 0
                    it.acceptList = acceptList
                    it.duration = duration
                }
            } else {
                return PlayerSourceInfo().also {
                    it.height = mediaInfo.video[0].height
                    it.width = mediaInfo.video[0].width
                    it.url = url
                    it.quality = 0
                    it.acceptList = acceptList
                    it.duration = duration
                }
            }
        }
    }

    override fun getSourceIds(): PlayerSourceIds {
        return PlayerSourceIds(
            cid = id,
            sid = entry.season_id ?: "",
            epid = entry.ep?.episode_id?.toString() ?: "",
            aid = entry.avid?.toString() ?: "",
        )
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

    private fun getEntryFileInfo(): BiliDownloadEntryInfo {
        val entryJsonFile = File(entryDirPath, "entry.json")
        return MiaoJson.fromJson(entryJsonFile.readText())
    }

    private fun getBiliDanmukuStream(): InputStream? {
        val danmakuXMLFile = File(entryDirPath, "danmaku.xml")
        return danmakuXMLFile.inputStream()
    }

    override suspend fun getSubtitles(): List<SubtitleSourceInfo> = emptyList()
}