package com.a10miaomiao.bilimiao.comm.delegate.player.entity

import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser

/**
 * 播放源状态（当前播放内容相关的稳定状态）
 *
 * 由 [com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl] 通过单个
 * [kotlinx.coroutines.flow.MutableStateFlow] 维护，UI 层观察
 * [com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate.sourceState] 获取。
 *
 * - [currentSource]：当前播放源（领域对象，标题/封面/作者等，openPlayer 时同步设置）
 * - [playbackInfo]：播放解析信息（URL/清晰度列表/时长/分辨率等，加载完成后才有）
 */
data class PlayerSourceState(
    /** 当前播放源 */
    val currentSource: BasePlayerSource? = null,
    /** 播放解析信息 */
    val playbackInfo: PlayerSourceInfo? = null,
    /** 当前清晰度 */
    val currentQuality: Int = 64,
    /** 弹幕解析器 */
    val danmakuParser: BaseDanmakuParser? = null,
    /** 可用字幕列表 */
    val subtitleList: List<SubtitleSourceInfo> = emptyList(),
    /** 当前选中的字幕（null 表示关闭字幕） */
    val currentSubtitle: SubtitleSourceInfo? = null,
)
