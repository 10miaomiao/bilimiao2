package com.a10miaomiao.bilimiao.delegate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import cn.a10miaomiao.download.*
import cn.a10miaomiao.player.BiliDanmukuParser
import cn.a10miaomiao.player.CompressionTools
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.netword.PlayurlHelper
import com.a10miaomiao.miaoandriod.MiaoLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import java.io.ByteArrayInputStream
import java.io.InputStream


class DownloadDelegate(
        private var activity: AppCompatActivity
) {

    val downloadList: ArrayList<BiliVideoEntry>
        get () = if (downloadService != null) {
            downloadService.getDownloadList()
        } else {
            arrayListOf()
        }

    val curDownload = MiaoLiveData<DownloadInfo?>(null)
    val downloadNotify by lazy { DownloadNotify(activity) }
    var curVideo: BiliVideoEntry? = null
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

    fun getPlayUrl(entry: BiliVideoEntry): Observable<BiliVideoPlayUrlEntry> {
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
        return PlayurlHelper.getVideoPalyUrl(entry.avid.toString(), entry.page_data.cid.toString(), entry.prefered_video_quality)
                .map {
                    val segmentList = it.durl.map {
                        Segment(
                                backup_urls = listOf(),
                                bytes = it.size,
                                duration = it.length,
                                md5 = "",
                                meta_url = "",
                                order = it.order,
                                url = it.url
                        )
                    }
                    val description = it.support_formats.find { item -> it.quality == item.quality }?.new_description
                            ?: "清晰 480P"
                    return@map BiliVideoPlayUrlEntry(
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
                            format = it.format,
                            player_error = 0,
                            need_vip = false,
                            need_login = false,
                            intact = false
                    )
                }
    }

    fun getDanmakuXML(cid: String): Observable<InputStream> {
        val url = BiliApiService.getDanmakuList(cid)
        return MiaoHttp.get(url, {
            ByteArrayInputStream(CompressionTools.decompressXML(it.body()!!.bytes()))
        })
    }

    fun downloadCallback(entry: BiliVideoEntry, download: DownloadInfo) {
        when (download.status) {
            0 -> {
                curVideo = entry
            }
            1, 2 -> {
                // 下载完成，或暂停
                curVideo = null
            }
            -1 -> {
                curVideo = entry
            }
        }
        downloadNotify.notifyData(download)
        curDownload post download
    }
}
