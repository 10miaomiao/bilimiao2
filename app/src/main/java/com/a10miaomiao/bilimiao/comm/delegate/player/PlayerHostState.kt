package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.compose.PlayerFloatingLayoutState
import cn.a10miaomiao.bilimiao.compose.PlayerPortraitLayoutState

/**
 * 安卓端播放器宿主状态接口
 *
 * 桥接 Compose [PlayerState] 与原生 Activity（窗口插入、屏幕方向、状态栏等）。
 * 由 [com.a10miaomiao.bilimiao.MainActivity.DirectComposePlayerHostState] 实现。
 */
interface PlayerHostState {
    companion object {
        const val VERTICAL = 1
        const val HORIZONTAL = 2
    }

    var showPlayer: Boolean
    var fullScreenPlayer: Boolean
    var orientation: Int
    var smallModePlayerMaxHeight: Int

    val portraitLayoutState: PlayerPortraitLayoutState
    val floatingLayoutState: PlayerFloatingLayoutState

    var playerVideoRatio: Float

    fun animatePlayerHeight(target: Int)
    fun holdUpPlayer()
    fun updateFloatingPlayerLayoutState(state: PlayerFloatingLayoutState)
    fun updateSmallModePlayerMaxHeight()
}
