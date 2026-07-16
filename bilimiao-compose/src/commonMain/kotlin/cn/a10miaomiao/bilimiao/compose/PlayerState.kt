package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
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

class PlayerState(
    private val onFloatingPlayerLayoutStateChanged: (PlayerFloatingLayoutState) -> Unit = {},
) {

    private val _showPlayer = mutableStateOf(false)
    val showPlayer get() = _showPlayer.value

    private val _orientation = mutableStateOf(ORIENTATION_PORTRAIT)
    val orientation get() = _orientation.value

    private val _fullScreenPlayer = mutableStateOf(false)
    val fullScreenPlayer get() = _fullScreenPlayer.value

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

    fun setFullScreenPlayer(value: Boolean) {
        _fullScreenPlayer.value = value
    }

    fun setPortraitPlayerLayoutState(state: PlayerPortraitLayoutState) {
        _portraitPlayerLayoutState.value = state
    }

    fun setFloatingPlayerLayoutState(state: PlayerFloatingLayoutState) {
        _floatingPlayerLayoutState.value = state
    }

    fun updateFloatingPlayerLayoutState(state: PlayerFloatingLayoutState) {
        _floatingPlayerLayoutState.value = state
        onFloatingPlayerLayoutStateChanged(state)
    }

    fun setPlayerVideoRatio(ratio: Float) {
        _playerVideoRatio.value = ratio
    }

    fun setAnchorBounds(bounds: Rect?) {
        _anchorBounds.value = bounds
    }

}
