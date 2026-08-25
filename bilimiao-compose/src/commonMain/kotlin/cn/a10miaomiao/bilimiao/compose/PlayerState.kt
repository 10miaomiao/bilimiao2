package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlayerPortraitLayoutState(
    val minHeightPx: Int = 0,
    val currentHeightPx: Int = 0,
    val maxHeightPx: Int = 0,
)

data class PlayerFloatingLayoutState(
    val defaultWidthPx: Float = 0f,
    val defaultHeightPx: Float = 0f,
    val widthPx: Float = 0f,
    val heightPx: Float = 0f,
    val offsetXPx: Float = 0f,
    val offsetYPx: Float = 0f,
    val initialized: Boolean = false,
)

/**
 * 播放器宿主状态（安卓/桌面端共用的唯一实现）
 *
 * 统一管理播放器在页面中的展示状态：
 * - [fullScreenPlayer] 全屏状态：数据源为
 *   [com.a10miaomiao.bilimiao.comm.delegate.player.FullscreenController.isFullscreen]，
 *   由入口点（MainActivity / 桌面端 Main.kt）在创建时注入，Compose 层通过
 *   [kotlinx.coroutines.flow.StateFlow] 观察，不再维护副本状态。
 * - 小屏播放器高度（[portraitPlayerLayoutState]）：由平台层（安卓）根据屏幕尺寸
 *   通过 [setSmallModePlayerHeight] 更新，桌面端使用默认值。
 *
 * @param fullScreenPlayer 全屏状态流
 */
class PlayerState(
    fullScreenPlayer: StateFlow<Boolean> = MutableStateFlow(false),
) {

    /** 全屏状态（唯一数据源，由入口点注入） */
    val fullScreenPlayer: StateFlow<Boolean> = fullScreenPlayer

    private val _showPlayer = mutableStateOf(false)
    val showPlayer get() = _showPlayer.value

    private val _orientation = mutableStateOf(ORIENTATION_PORTRAIT)
    val orientation get() = _orientation.value

    private val _portraitPlayerLayoutState = mutableStateOf(PlayerPortraitLayoutState())
    val portraitPlayerLayoutState get() = _portraitPlayerLayoutState.value

    private val _floatingPlayerLayoutState = mutableStateOf(PlayerFloatingLayoutState())
    val floatingPlayerLayoutState get() = _floatingPlayerLayoutState.value

    private val _playerVideoRatio = mutableStateOf(16f / 9f)
    val playerVideoRatio get() = _playerVideoRatio.value

    private val _anchorBounds = mutableStateOf<Rect?>(null)
    val anchorBounds get() = _anchorBounds.value

    fun setShowPlayer(value: Boolean) {
        _showPlayer.value = value
    }

    fun setOrientation(value: Int) {
        _orientation.value = value
    }

    /**
     * 更新竖屏小窗播放器高度
     *
     * @param minHeightPx 最小高度
     * @param currentHeightPx 当前高度
     * @param maxHeightPx 最大高度（受屏幕尺寸与视频比例限制）
     */
    fun setSmallModePlayerHeight(minHeightPx: Int, currentHeightPx: Int, maxHeightPx: Int) {
        val state = PlayerPortraitLayoutState(
            minHeightPx = minHeightPx,
            currentHeightPx = currentHeightPx,
            maxHeightPx = maxHeightPx,
        )
        if (_portraitPlayerLayoutState.value == state) {
            return
        }
        _portraitPlayerLayoutState.value = state
    }

    /**
     * 更新悬浮播放器布局状态
     *
     * 保留已有默认尺寸（[PlayerFloatingLayoutState.defaultWidthPx]/[PlayerFloatingLayoutState.defaultHeightPx]），
     * 避免后续更新把默认值覆盖为 0。
     */
    fun updateFloatingPlayerLayoutState(state: PlayerFloatingLayoutState) {
        val prev = _floatingPlayerLayoutState.value
        val merged = state.copy(
            defaultWidthPx = state.defaultWidthPx.takeIf { it > 0f } ?: prev.defaultWidthPx,
            defaultHeightPx = state.defaultHeightPx.takeIf { it > 0f } ?: prev.defaultHeightPx,
        )
        _floatingPlayerLayoutState.value = merged
    }

    fun setPlayerVideoRatio(ratio: Float) {
        _playerVideoRatio.value = ratio
    }

    fun setAnchorBounds(bounds: Rect?) {
        _anchorBounds.value = bounds
    }

}
