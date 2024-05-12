package cn.a10miaomiao.bilimiao.download.entry

import java.text.DecimalFormat

data class CurrentDownloadInfo(
    val taskId: Long,
    val parentDirPath: String,
    val parentId: String,
    val id: Long,
    val name: String,
    val url: String,
    var length: Long = 0,
    var size: Long = 0,
    var progress: Long = 0L,
    var status: Int = STATUS_WAIT,
    var header: Map<String, String> = mapOf()
) {
    val statusText get() = when (status) {
        STATUS_FAIL_DOWNLOAD -> "下载失败"
        STATUS_FAIL_DANMAKU -> "获取弹幕失败"
        STATUS_FAIL_PLAYURL -> "获取播放地址失败"
        STATUS_DOWNLOADING -> {
            val fnum = DecimalFormat("##0.00")
            "正在下载 ${fnum.format(progress * 1.0 / size * 100.0)}%"
        }
        STATUS_AUDIO_DOWNLOADING -> {
            "正在下载音频"
        }
        STATUS_COMPLETED -> "下载完成"
        STATUS_PAUSE -> "暂停中"
        STATUS_GET_DANMAKU -> "获取弹幕"
        STATUS_GET_PLAYURL -> "获取播放地址"
        STATUS_WAIT -> "等待中"
        else -> "等待中"
    }

    val rate get() = if (size == 0L) 0F else progress.toFloat() / size.toFloat()

    companion object {
        const val STATUS_DOWNLOADING = 100
        const val STATUS_AUDIO_DOWNLOADING = 101
        const val STATUS_COMPLETED = 200
        const val STATUS_PAUSE = 201
        const val STATUS_GET_DANMAKU = 102
        const val STATUS_GET_PLAYURL = 101
        const val STATUS_FAIL_DOWNLOAD = -100
        const val STATUS_FAIL_DANMAKU = -102
        const val STATUS_FAIL_PLAYURL = -101
        const val STATUS_WAIT = 0
    }

}