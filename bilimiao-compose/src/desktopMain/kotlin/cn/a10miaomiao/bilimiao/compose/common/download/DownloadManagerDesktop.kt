package cn.a10miaomiao.bilimiao.compose.common.download

import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import cn.a10miaomiao.bilimiao.download.DownloadServiceDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
/**
 * 桌面端下载管理实现：桥接到 [DownloadServiceDesktop]。
 *
 * 类型转换复用 [DownloadManagerAndroid] 中的同名扩展函数模式，由于桌面端与
 * bilimiao-download 的 commonMain 类型在 JVM 上结构一致，这里直接通过映射函数
 * 在两套类型间转换。
 */
class DownloadManagerDesktop : DownloadManager {

    private val service: DownloadServiceDesktop get() = DownloadServiceDesktop.instance

    private val _downloadListVersion = MutableStateFlow(0)
    private val _curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)

    init {
        // 触发单例初始化，确保 BaseDownloadService.initialize 已执行
        val svc = DownloadServiceDesktop.instance
        // 桥接状态流到 UI 可观察的 StateFlow
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            svc.downloadListVersion.collect { _downloadListVersion.value = it }
        }
        scope.launch {
            svc.curDownload.collect { raw -> _curDownload.value = raw?.toCommon() }
        }
    }

    override val downloadListVersion: StateFlow<Int> get() = _downloadListVersion
    override val curDownload: StateFlow<CurrentDownloadInfo?> get() = _curDownload

    override val downloadList: List<BiliDownloadEntryAndPathInfo>
        get() = service.downloadList.map { it.toCommon() }

    override fun getDownloadPath(): String = service.getDownloadPath()

    override fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo> {
        return service.readDownloadDirectory(File(dirPath)).map { it.toCommon() }
    }

    override fun createDownload(biliEntry: BiliDownloadEntryInfo) {
        service.createDownload(biliEntry.toOriginal())
    }

    override fun startDownload(entryDirPath: String) {
        service.startDownload(entryDirPath)
    }

    override fun cancelDownload(taskId: Long) {
        service.cancelDownload(taskId)
    }

    override fun deleteDownload(pageDirPath: String, entryDirPath: String) {
        service.deleteDownload(pageDirPath, entryDirPath)
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
