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
 * 声明当前媒体的外部音频轨（B站 DASH 音视频分离：视频流 + 独立音频流）
 *
 * 必须在加载该媒体（`setMediaData` / `playUri`）之前调用，由平台实现负责把音频轨
 * 接到接下来加载的媒体上：
 * - 安卓 ExoPlayer：记录待接入的音频，在 open 时用 `MergingMediaSource` 把视频流与
 *   音频流合并为一个媒体源（一次 open 完成，不依赖播放状态时机）
 * - 桌面 mpv：设置 `audio-files` 属性（mpv 在 loadfile 时生效，因此必须先于加载设置）
 *
 * [audioUrl] 为 null 表示当前媒体没有独立音频（音视频合一、分段视频、MPD 内含音频等），
 * 此时必须清除上一次的声明：否则会沿用上一个视频的音频，表现为
 * 「播放新视频时响的是上一个视频的声音」（桌面 mpv 的 audio-files 属性会跨文件保留）。
 *
 * @param player 目标播放器
 * @param videoUrl 即将加载的视频（主媒体）地址，用于把音频与本次加载对应起来
 * @param audioUrl 音频流 URL，null 表示无独立音频
 * @param headers HTTP 请求头
 */
expect fun setExternalAudioTrack(
    player: MediampPlayer,
    videoUrl: String,
    audioUrl: String?,
    headers: Map<String, String>,
)

/**
 * 更新系统「通知栏 / 锁屏」播放控制器展示的信息（仅安卓端有效）
 *
 * 安卓端把标题 / UP主 / 封面写入底层 ExoPlayer 的 MediaItem 元数据，由 media3 的
 * MediaSessionService 渲染通知栏播放器控制器（标题、封面），桌面端为 no-op。
 *
 * 必须在加载该媒体（`setMediaData` / `playUri`）之前调用，否则元数据不会随本次 open 生效。
 *
 * @param player 目标播放器
 * @param title 视频标题（为空时通知栏不显示标题）
 * @param artist UP主名称
 * @param artworkUri 封面地址
 */
expect fun updateMediaSessionMetadata(
    player: MediampPlayer,
    title: String?,
    artist: String?,
    artworkUri: String?,
)

/**
 * 设置音量 (0-100)
 *
 * - 安卓 ExoPlayer：通过 AudioLevelController feature
 * - 桌面 mpv：通过 MPVHandle 的 volume 属性
 */
expect fun setPlayerVolume(player: MediampPlayer, volume: Int)

/**
 * 应用「占用音频焦点」开关（对齐旧版 DanmakuVideoPlayer.enabledAudioFocus）
 *
 * - 安卓：通过 media3 [androidx.media3.exoplayer.ExoPlayer] 的 `handleAudioFocus` 控制。
 *   开启时播放期间申请音频焦点，其它应用播放 / 来电抢占时自动暂停、释放后自动续播；
 *   关闭时不申请音频焦点，可与其它应用同时出声。支持运行时切换。
 * - 桌面：no-op（桌面端无系统音频焦点概念）
 *
 * @param player 目标播放器
 * @param enabled 是否占用音频焦点
 */
expect fun setPlayerAudioFocusEnabled(player: MediampPlayer, enabled: Boolean)

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
