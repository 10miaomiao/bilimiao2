package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.datastore.preferences.core.edit
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
 * - [pictureInPicture] 画中画（应用外小窗）状态：数据源为平台层
 *   [com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate.pictureInPicture]，
 *   同样由入口点注入。画中画窗口内只有播放器画面，页面级控件（appbar、内容区）需要让位，
 *   因此它是 [components.layout.ComposeScaffold]（宿主布局）与
 *   [components.player.BiliVideoScaffold]（播放器 UI）之间的共享状态。
 * - 小屏播放器高度（[portraitPlayerLayoutState]）：由平台层（安卓）根据屏幕尺寸
 *   通过 [setSmallModePlayerHeight] 更新，桌面端使用默认值。
 * - 自由悬浮窗口几何（[floatingPlayerLayoutState]）：随拖动/缩放实时更新，
 *   并通过 [appDataStore] 持久化，下次启动时恢复位置与大小。
 *
 * @param fullScreenPlayer 全屏状态流
 * @param pictureInPicture 画中画（应用外小窗）状态流
 * @param scope 持久化读写使用的协程作用域（入口点注入）
 */
class PlayerState(
    fullScreenPlayer: StateFlow<Boolean> = MutableStateFlow(false),
    pictureInPicture: StateFlow<Boolean> = MutableStateFlow(false),
    private val scope: CoroutineScope,
) {

    /** 浮窗几何持久化防抖间隔 */
    private companion object {
        const val FLOATING_LAYOUT_SAVE_DEBOUNCE_MILLIS = 800L
    }

    /** 全屏状态（唯一数据源，由入口点注入） */
    val fullScreenPlayer: StateFlow<Boolean> = fullScreenPlayer

    /** 画中画（应用外小窗）状态（唯一数据源，由入口点注入） */
    val pictureInPicture: StateFlow<Boolean> = pictureInPicture

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

    private var floatingSaveJob: Job? = null

    init {
        // 启动时异步恢复上次保存的浮窗几何；若用户已先完成浮窗初始化则跳过
        scope.launch {
            restoreFloatingPlayerLayout()
        }
    }

    /**
     * 从 [appDataStore] 恢复自由悬浮窗口的大小与位置。
     *
     * 仅在浮窗尚未初始化（[PlayerFloatingLayoutState.initialized] 为 false）时生效，
     * 避免覆盖当前会话中用户已拖动的状态。
     */
    private suspend fun restoreFloatingPlayerLayout() {
        if (_floatingPlayerLayoutState.value.initialized) {
            return
        }
        val preferences = appDataStore.data.first()
        val width = preferences[SettingPreferences.PlayerFloatingWidthPx] ?: return
        val height = preferences[SettingPreferences.PlayerFloatingHeightPx] ?: return
        if (width <= 0f || height <= 0f) {
            return
        }
        val offsetX = preferences[SettingPreferences.PlayerFloatingOffsetXPx] ?: 0f
        val offsetY = preferences[SettingPreferences.PlayerFloatingOffsetYPx] ?: 0f
        _floatingPlayerLayoutState.value = PlayerFloatingLayoutState(
            defaultWidthPx = width,
            defaultHeightPx = height,
            widthPx = width,
            heightPx = height,
            offsetXPx = offsetX,
            offsetYPx = offsetY,
            initialized = true,
        )
    }

    /**
     * 防抖保存自由悬浮窗口几何到 [appDataStore]。
     *
     * 拖动/缩放期间 [updateFloatingPlayerLayoutState] 被高频调用，这里延迟落盘，
     * 手势结束后仅写入一次最终状态。
     */
    private fun scheduleFloatingLayoutSave() {
        floatingSaveJob?.cancel()
        floatingSaveJob = scope.launch {
            delay(FLOATING_LAYOUT_SAVE_DEBOUNCE_MILLIS)
            val state = _floatingPlayerLayoutState.value
            appDataStore.edit { prefs ->
                prefs[SettingPreferences.PlayerFloatingWidthPx] = state.widthPx
                prefs[SettingPreferences.PlayerFloatingHeightPx] = state.heightPx
                prefs[SettingPreferences.PlayerFloatingOffsetXPx] = state.offsetXPx
                prefs[SettingPreferences.PlayerFloatingOffsetYPx] = state.offsetYPx
            }
        }
    }

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
     * 避免后续更新把默认值覆盖为 0。更新后触发防抖持久化。
     */
    fun updateFloatingPlayerLayoutState(state: PlayerFloatingLayoutState) {
        val prev = _floatingPlayerLayoutState.value
        val merged = state.copy(
            defaultWidthPx = state.defaultWidthPx.takeIf { it > 0f } ?: prev.defaultWidthPx,
            defaultHeightPx = state.defaultHeightPx.takeIf { it > 0f } ?: prev.defaultHeightPx,
        )
        _floatingPlayerLayoutState.value = merged
        scheduleFloatingLayoutSave()
    }

    fun setPlayerVideoRatio(ratio: Float) {
        _playerVideoRatio.value = ratio
    }

    fun setAnchorBounds(bounds: Rect?) {
        _anchorBounds.value = bounds
    }

}
