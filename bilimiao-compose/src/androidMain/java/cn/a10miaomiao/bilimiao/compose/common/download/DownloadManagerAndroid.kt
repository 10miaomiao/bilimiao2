package cn.a10miaomiao.bilimiao.compose.common.download

import android.content.Context
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import cn.a10miaomiao.bilimiao.download.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadManagerAndroid(
    private val context: Context,
) : DownloadManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _downloadListVersion = MutableStateFlow(0)
    private val _curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)

    private val service: DownloadService?
        get() = DownloadService.instance

    init {
        // 确保服务已启动
        DownloadService.startService(context)
        // 桥接状态流
        scope.launch {
            while (true) {
                val svc = service
                if (svc != null) {
                    launch { svc.downloadListVersion.collect { _downloadListVersion.value = it } }
                    launch { svc.curDownload.collect { raw -> _curDownload.value = raw?.toCommon() } }
                    break
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    override val downloadListVersion: StateFlow<Int> get() = _downloadListVersion
    override val curDownload: StateFlow<CurrentDownloadInfo?> get() = _curDownload

    override val downloadList: List<BiliDownloadEntryAndPathInfo>
        get() = service?.downloadList?.map { it.toCommon() } ?: emptyList()

    override fun getDownloadPath(): String = service?.getDownloadPath() ?: ""

    override fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo> {
        return service?.readDownloadDirectory(File(dirPath))?.map { it.toCommon() } ?: emptyList()
    }

    override fun createDownload(biliEntry: BiliDownloadEntryInfo) {
        service?.createDownload(biliEntry.toOriginal())
    }

    override fun startDownload(entryDirPath: String) {
        service?.startDownload(entryDirPath)
    }

    override fun cancelDownload(taskId: Long) {
        service?.cancelDownload(taskId)
    }

    override fun deleteDownload(pageDirPath: String, entryDirPath: String) {
        service?.deleteDownload(pageDirPath, entryDirPath)
    }
}

// ---- bilimiao-download 类型 → common 类型 ----

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo.toCommon()
    = BiliDownloadEntryAndPathInfo(pageDirPath, entryDirPath, entry.toCommon())

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.toCommon()
    = BiliDownloadEntryInfo(
        media_type, has_dash_audio, is_completed, total_bytes, downloaded_bytes,
        title, type_tag, cover, video_quality, prefered_video_quality,
        quality_pithy_description, guessed_total_bytes, total_time_milli,
        danmaku_count, time_update_stamp, time_create_stamp, can_play_in_advance,
        interrupt_transform_temp_file, avid, spid, bvid, owner_id,
        page_data?.toCommon(), season_id, source?.toCommon(), ep?.toCommon(),
    )

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.PageInfo.toCommon()
    = BiliDownloadEntryInfo.PageInfo(cid, page, from, part, vid, has_alias, tid, width, height, rotate, download_title, download_subtitle)

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.SourceInfo.toCommon()
    = BiliDownloadEntryInfo.SourceInfo(av_id, cid)

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.EpInfo.toCommon()
    = BiliDownloadEntryInfo.EpInfo(av_id, page, danmaku, cover, episode_id, index, index_title, from, season_type, width, height, rotate, link, bvid, sort_index)

private fun cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo.toCommon()
    = CurrentDownloadInfo(taskId, parentDirPath, parentId, id, name, url, length, size, progress, status, header)

// ---- common 类型 → bilimiao-download 类型 ----

private fun BiliDownloadEntryInfo.toOriginal()
    = cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo(
        media_type, has_dash_audio, is_completed, total_bytes, downloaded_bytes,
        title, type_tag, cover, video_quality, prefered_video_quality,
        quality_pithy_description, guessed_total_bytes, total_time_milli,
        danmaku_count, time_update_stamp, time_create_stamp, can_play_in_advance,
        interrupt_transform_temp_file, avid, spid, bvid, owner_id,
        page_data?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.PageInfo(it.cid, it.page, it.from, it.part, it.vid, it.has_alias, it.tid, it.width, it.height, it.rotate, it.download_title, it.download_subtitle) },
        season_id,
        source?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.SourceInfo(it.av_id, it.cid) },
        ep?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.EpInfo(it.av_id, it.page, it.danmaku, it.cover, it.episode_id, it.index, it.index_title, it.from, it.season_type, it.width, it.height, it.rotate, it.link, it.bvid, it.sort_index) },
    )
