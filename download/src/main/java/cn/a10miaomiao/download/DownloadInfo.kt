package cn.a10miaomiao.download

import java.text.DecimalFormat

data class DownloadInfo(
        val key: String,
        val name: String,
        val url: String,
        var length: Long = 0,
        var size: Long = 0,
        var progress: Long = 0L,
        var status: Int = 0,
        var header: Map<String, String> = mapOf()
) {
    val statusText get() = when (status) {
        -1 -> "下载失败"
        -101 -> "获取弹幕失败"
        -102 -> "获取播放地址失败"
        0 -> {
            val fnum = DecimalFormat("##0.00")
            "正在下载 ${fnum.format(progress * 1.0 / size * 100.0)}%"
        }
        1 -> "下载完成"
        2 -> "暂停中"
        101 -> "获取弹幕"
        102 -> "获取播放地址"
        else -> "等待中"
    }
}