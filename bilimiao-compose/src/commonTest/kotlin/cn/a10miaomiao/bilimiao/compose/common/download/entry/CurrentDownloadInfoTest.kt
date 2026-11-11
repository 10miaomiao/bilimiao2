package cn.a10miaomiao.bilimiao.compose.common.download.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 下载状态值的纯逻辑测试.
 *
 * 覆盖曾经踩过的坑: [CurrentDownloadInfo.STATUS_GET_PLAYURL] 与
 * [CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING] 撞值, 导致「获取播放地址」的提示
 * 永远显示成「正在下载音频」.
 */
class CurrentDownloadInfoTest {

    private fun info(
        status: Int,
        progress: Long = 0L,
        size: Long = 0L,
    ) = CurrentDownloadInfo(
        taskId = 1L,
        parentDirPath = "",
        parentId = "",
        id = 2L,
        name = "",
        url = "",
        length = 0L,
        size = size,
        progress = progress,
        status = status,
    )

    @Test
    fun statusValuesAreDistinct() {
        val values = listOf(
            CurrentDownloadInfo.STATUS_WAIT,
            CurrentDownloadInfo.STATUS_DOWNLOADING,
            CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING,
            CurrentDownloadInfo.STATUS_GET_DANMAKU,
            CurrentDownloadInfo.STATUS_GET_PLAYURL,
            CurrentDownloadInfo.STATUS_COMPLETED,
            CurrentDownloadInfo.STATUS_PAUSE,
            CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD,
            CurrentDownloadInfo.STATUS_FAIL_DANMAKU,
            CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
        )
        assertEquals(values.size, values.toSet().size, "状态值不得重复: $values")
    }

    @Test
    fun getPlayUrlDoesNotShowAsDownloadingAudio() {
        assertEquals(
            "获取播放地址",
            info(CurrentDownloadInfo.STATUS_GET_PLAYURL).statusText,
        )
        assertEquals(
            "正在下载音频",
            info(CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING).statusText,
        )
    }

    @Test
    fun statusTextMapping() {
        assertEquals("等待中", info(CurrentDownloadInfo.STATUS_WAIT).statusText)
        assertEquals("获取弹幕", info(CurrentDownloadInfo.STATUS_GET_DANMAKU).statusText)
        assertEquals("下载完成", info(CurrentDownloadInfo.STATUS_COMPLETED).statusText)
        assertEquals("暂停中", info(CurrentDownloadInfo.STATUS_PAUSE).statusText)
        assertEquals("下载失败", info(CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD).statusText)
        assertEquals("获取弹幕失败", info(CurrentDownloadInfo.STATUS_FAIL_DANMAKU).statusText)
        assertEquals("获取播放地址失败", info(CurrentDownloadInfo.STATUS_FAIL_PLAYURL).statusText)
    }

    @Test
    fun downloadingTextShowsRoundedPercent() {
        assertEquals(
            "正在下载 50.00%",
            info(CurrentDownloadInfo.STATUS_DOWNLOADING, progress = 50L, size = 100L).statusText,
        )
        assertEquals(
            "正在下载 0.00%",
            info(CurrentDownloadInfo.STATUS_DOWNLOADING).statusText,
        )
        assertEquals(
            "正在下载 33.33%",
            info(CurrentDownloadInfo.STATUS_DOWNLOADING, progress = 1L, size = 3L).statusText,
        )
    }

    @Test
    fun rateIsZeroBeforeLengthKnown() {
        assertEquals(0F, info(CurrentDownloadInfo.STATUS_WAIT).rate)
        assertTrue(info(CurrentDownloadInfo.STATUS_DOWNLOADING, progress = 25L, size = 100L).rate == 0.25F)
    }
}
