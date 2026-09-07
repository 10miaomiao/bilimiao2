package com.a10miaomiao.bilimiao.comm.delegate.player

import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.source.MediaData
import org.openani.mediamp.source.UriMediaData

/**
 * 平台特定的播放器创建与配置
 *
 * expect/actual 将安卓 (ExoPlayerMediampPlayer) 和桌面 (MpvMediampPlayer) 的差异
 * 封装在此处，供 [PlayerDelegateImpl] (commonMain) 调用。
 */

/**
 * 创建平台对应的 [MediampPlayer] 实例
 * - 安卓：[org.openani.mediamp.exoplayer.ExoPlayerMediampPlayer]
 * - 桌面：[org.openani.mediamp.mpv.MpvMediampPlayer]
 */
expect fun createMediampPlayer(): MediampPlayer

/**
 * 设置音视频分离的媒体数据（B站 DASH 格式常见：视频流 + 独立音频流）
 *
 * - 安卓 ExoPlayer：通过 MergingMediaSource 合并，UriMediaData 只承载视频流，
 *   外部音频通过 MediaItem 配置或自定义 MediaSource 注入。
 * - 桌面 mpv：通过 audio-files 属性注入外部音频，videoUrl 通过 UriMediaData 承载。
 *
 * @param player 目标播放器
 * @param videoUrl 视频流 URL
 * @param audioUrl 音频流 URL（可能为 null，表示音视频合一）
 * @param headers HTTP 请求头
 */
expect suspend fun setMergingMediaData(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
)

/**
 * 设置音量 (0-100)
 *
 * - 安卓 ExoPlayer：通过 AudioLevelController feature
 * - 桌面 mpv：通过 MPVHandle 的 volume 属性
 */
expect fun setPlayerVolume(player: MediampPlayer, volume: Int)

/**
 * 设置屏幕方向（平台特定）
 *
 * - 安卓：调用 `Activity.requestedOrientation`，映射 [ScreenOrientationRequest] 到 `ActivityInfo` 常量
 * - 桌面：no-op（桌面端无屏幕方向概念）
 *
 * 由 [FullscreenController] 调用。
 *
 * @param request 屏幕方向请求
 */
expect fun setRequestedOrientation(request: ScreenOrientationRequest)
