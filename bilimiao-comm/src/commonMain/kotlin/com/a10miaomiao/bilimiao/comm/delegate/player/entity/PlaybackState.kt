package com.a10miaomiao.bilimiao.comm.delegate.player.entity

/**
 * 播放器总体状态（合并 isPlaying / isLoading / isCompleted / error 状态）
 */
enum class PlaybackStatus {
    /** 空闲（未加载） */
    Idle,

    /** 加载中 */
    Loading,

    /** 播放中 */
    Playing,

    /** 已暂停 */
    Paused,

    /** 播放完成 */
    Completed,

    /** 出错（[PlaybackState.errorMessage] 携带错误详情） */
    Error,
}

/**
 * 播放状态（低频稳定状态）
 *
 * 由 [com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl] 通过单个
 * [kotlinx.coroutines.flow.MutableStateFlow] 维护，UI 层观察
 * [com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate.playbackState] 获取。
 */
data class PlaybackState(
    /** 播放器总体状态 */
    val status: PlaybackStatus = PlaybackStatus.Idle,
    /** 总时长（毫秒） */
    val duration: Long = 0L,
    /** 加载提示文本 */
    val loadingMessage: String = "",
    /** 错误信息（null 表示无错误，仅在 status = Error 时有值） */
    val errorMessage: String? = null,
    /** 弹幕是否可见 */
    val danmakuVisible: Boolean = true,
    /** 音量 (0-100) */
    val volume: Int = 100,
    /** 播放倍速 */
    val playbackSpeed: Float = 1.0f,
)
