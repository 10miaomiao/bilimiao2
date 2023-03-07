package com.a10miaomiao.bilimiao.comm.delegate.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import cn.a10miaomiao.download.*
//import cn.a10miaomiao.player.CompressionTools
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.store.DownloadStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.ByteArrayInputStream
import java.io.InputStream

class DownloadDelegate(
    private var activity: AppCompatActivity,
    override val di: DI,
) : DIAware {

    val downloadStore: DownloadStore by instance()

    val downloadList: ArrayList<BiliVideoEntry>
        get () = if (downloadService != null) {
            downloadService.getDownloadList()
        } else {
            arrayListOf()
        }

    val downloadNotify by lazy { DownloadNotify(activity) }

    lateinit var downloadService: DownloadService

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadService = (service as DownloadBinder).downloadService
            downloadService.getPlayUrl = this@DownloadDelegate::getPlayUrl
            downloadService.getDanmakuXML = this@DownloadDelegate::getDanmakuXML
            downloadService.downloadCallback = this@DownloadDelegate::downloadCallback
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    fun onCreate(savedInstanceState: Bundle?) {
        val intent = Intent(activity, DownloadService::class.java)
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    fun onDestroy() {
        activity.unbindService(mConnection)
    }

    fun getPlayUrl(entry: BiliVideoEntry): BiliVideoPlayUrlEntry{
        val pageData = entry.page_data
        val playerCodecConfigList = listOf(
            PlayerCodecConfig(
                player = "IJK_PLAYER",
                use_ijk_media_codec = false
            ),
            PlayerCodecConfig(
                player = "ANDROID_PLAYER",
                use_ijk_media_codec = false
            )
        )
        var res = BiliApiService.playerAPI.getVideoPalyUrl(
            entry.avid.toString(),
            entry.page_data.cid.toString(),
            entry.prefered_video_quality,
            fnval = 1,
        )
        val segmentList = res.durl!!.map { item ->
            Segment(
                backup_urls = listOf(),
                bytes = item.size,
                duration = item.length,
                md5 = "",
                meta_url = "",
                order = item.order,
                url = item.url
            )
        }
        val description = res.support_formats.find { item -> res.quality == item.quality }?.new_description ?: "清晰 480P"
        return BiliVideoPlayUrlEntry(
            from = pageData.from,
            quality = entry.prefered_video_quality,
            type_tag = entry.type_tag,
            description = description,
            player_codec_config_list = playerCodecConfigList,
            user_agent = "Bilibili Freedoooooom\\/MarkII",
            segment_list = segmentList,
            parse_timestamp_milli = 0,
            available_period_milli = 0,
            is_downloaded = false,
            is_resolved = true,
            time_length = 0,
            marlin_token = "",
            video_codec_id = 0,
            video_project = true,
            format = res.format,
            player_error = 0,
            need_vip = false,
            need_login = false,
            intact = false
        )
    }

    fun getDanmakuXML(cid: String): InputStream {
        return BiliApiService.playerAPI.getDanmakuList(cid)
            .call().let {
                ByteArrayInputStream(CompressionTools.decompressXML(it.body!!.bytes()))
            }
    }

    fun downloadCallback(entry: BiliVideoEntry, download: DownloadInfo) {
        when (download.status) {
            0, -1 -> {
                downloadStore.setVideoInfo(entry)
            }
            1, 2 -> {
                // 下载完成，或暂停
                downloadStore.setVideoInfo(null)
            }
        }
        downloadNotify.notifyData(download)
        downloadStore.setDownloadInfo(download)
    }
}