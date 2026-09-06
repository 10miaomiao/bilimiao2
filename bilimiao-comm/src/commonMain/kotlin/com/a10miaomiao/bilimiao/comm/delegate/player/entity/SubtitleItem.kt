package com.a10miaomiao.bilimiao.comm.delegate.player.entity

/**
 * 字幕内容行（毫秒时间轴）
 *
 * 由 B 站字幕 JSON（[com.a10miaomiao.bilimiao.comm.entity.player.SubtitleJsonInfo]）
 * 解析而来：原始 JSON 中 from/to 以秒为单位，此处转换为毫秒便于直接与播放位置比较。
 * 对齐原安卓版 `DanmakuVideoPlayer.SubtitleItemInfo` 的数据结构。
 *
 * @param from 开始时间（毫秒）
 * @param to 结束时间（毫秒）
 * @param content 字幕文本
 */
data class SubtitleItem(
    val from: Long,
    val to: Long,
    val content: String,
)
