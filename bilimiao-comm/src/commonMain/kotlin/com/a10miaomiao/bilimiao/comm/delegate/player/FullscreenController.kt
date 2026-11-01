package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 全屏模式
 *
 * 对齐原版 [SettingConstants] 的全屏模式常量。
 */
enum class FullscreenMode(val value: Int) {
    /** 跟随视频：竖向视频时为不指定方向，横向视频时为横向全屏(自动旋转) */
    AUTO(SettingConstants.PLAYER_FULL_MODE_AUTO),

    /** 横向全屏(固定方向1) */
    LANDSCAPE(SettingConstants.PLAYER_FULL_MODE_LANDSCAPE),

    /** 横向全屏(固定方向2) */
    REVERSE_LANDSCAPE(SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE),

    /** 横向全屏(自动旋转) */
    SENSOR_LANDSCAPE(SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE),

    /** 跟随系统：不指定方向 */
    UNSPECIFIED(SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED);

    companion object {
        fun fromValue(value: Int): FullscreenMode =
            entries.firstOrNull { it.value == value } ?: AUTO
    }
}

/**
 * 桌面端全屏方式
 *
 * 桌面窗口没有屏幕方向可言，全屏只需要决定「窗口本身要不要进入全屏」：
 * - [SCREEN] 铺满屏幕：窗口去边框铺满显示器（桌面端的原有行为，默认值）
 * - [WINDOW] 铺满窗口：窗口大小与边框保持不变，只有播放器铺满窗口内容区
 *
 * 与 [FullscreenMode] 共用 [SettingPreferences.PlayerFullMode] 偏好项
 * （两端互斥，同一台设备只有其中一套取值），因此取值域特意避开方向值。
 */
enum class DesktopFullscreenMode(val value: Int) {
    /** 铺满屏幕（默认） */
    SCREEN(SettingConstants.PLAYER_DESKTOP_FULL_MODE_SCREEN),

    /** 铺满窗口 */
    WINDOW(SettingConstants.PLAYER_DESKTOP_FULL_MODE_WINDOW);

    companion object {
        /**
         * 由偏好值还原桌面端全屏方式
         *
         * 读到的若是安卓写入的方向值则回落到默认的「铺满屏幕」，
         * 与桌面端的实际行为保持一致（方向值在桌面端没有意义）。
         */
        fun fromValue(value: Int): DesktopFullscreenMode =
            entries.firstOrNull { it.value == value } ?: SCREEN
    }
}

/**
 * 屏幕方向请求（平台无关）
 *
 * 由 [FullscreenController] 计算后通过其构造参数 `setRequestedOrientation` 应用到平台。
 */
enum class ScreenOrientationRequest {
    /** 不指定方向，跟随系统 */
    UNSPECIFIED,

    /** 竖屏（锁定） */
    PORTRAIT,

    /** 横向全屏(自动旋转) */
    SENSOR_LANDSCAPE,

    /** 横向全屏(固定方向1) */
    LANDSCAPE,

    /** 横向全屏(固定方向2) */
    REVERSE_LANDSCAPE,
}

/**
 * 全屏与屏幕方向控制器
 *
 * 整合原版 `PlayerController` 的全屏切换、屏幕方向设置、自动全屏等逻辑，
 * 移除 GSY/View 依赖，改为通过 [StateFlow] 暴露状态，由 Compose UI 观察。
 *
 * 平台差异通过构造参数 [setRequestedOrientation] 注入（不再使用 expect/actual）：
 * - 安卓：由 MainActivity 传入 compose 层 PlatformContext 的实现，直接作用于当前 Activity 窗口
 * - 桌面：不传，默认 no-op（桌面端无屏幕方向概念）
 *
 * @param scope 协程作用域
 * @param isLockScreenOrientationPortraitProvider 返回是否锁定竖屏（来自 AppStore）
 * @param setRequestedOrientation 把方向请求应用到平台窗口
 */
class FullscreenController(
    private val scope: CoroutineScope,
    private val isLockScreenOrientationPortraitProvider: () -> Boolean,
    /**
     * 应用屏幕方向到平台窗口（由入口注入）
     *
     * - 安卓：MainActivity 传入 compose 层 PlatformContext 的实现
     * - 桌面：默认 no-op
     */
    private val setRequestedOrientation: (ScreenOrientationRequest) -> Unit = {},
) {
    /** 当前是否全屏 */
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    /**
     * 用户设置的全屏模式（[SettingPreferences.PlayerFullMode] 的镜像）
     *
     * 由 [observeFullscreenModeSetting] 跟随 DataStore 刷新，单次全屏调用不会改写它，
     * 这样设置页改动即时生效，自动全屏也不会把用户的选择冲掉。
     */
    private val _fullscreenMode = MutableStateFlow(FullscreenMode.AUTO)
    val fullscreenMode: StateFlow<FullscreenMode> = _fullscreenMode.asStateFlow()

    /**
     * 桌面端全屏方式（与 [fullscreenMode] 共用 [SettingPreferences.PlayerFullMode] 的镜像）
     *
     * 只在桌面端有意义：桌面宿主据此决定是否让窗口进入全屏。
     * 安卓端不读该设置，全屏形态由 [screenOrientation] 表示。
     */
    private val _desktopFullscreenMode = MutableStateFlow(DesktopFullscreenMode.SCREEN)
    val desktopFullscreenMode: StateFlow<DesktopFullscreenMode> = _desktopFullscreenMode.asStateFlow()

    /** 全屏模式设置监听任务 */
    private var settingsJob: Job? = null

    /** 当前屏幕方向请求 */
    private val _screenOrientation = MutableStateFlow(ScreenOrientationRequest.UNSPECIFIED)
    val screenOrientation: StateFlow<ScreenOrientationRequest> = _screenOrientation.asStateFlow()

    /** 仅全屏（退出时直接关闭播放器，不回到小屏） */
    private var onlyFullscreen = false

    /** 是否由自动全屏触发（用于自动切回小屏判断） */
    private var canAutoCloseFullscreen = false

    /** 当前播放解析信息（用于判断视频比例，由 PlayerDelegateImpl 在加载完成后设置） */
    var playbackInfo: PlayerSourceInfo? = null

    /** 当前屏幕方向（来自平台配置变化） */
    var orientation: Int = ORIENTATION_PORTRAIT

    init {
        // 跟随设置页的「全屏播放屏幕方向」
        observeFullscreenModeSetting()
    }

    /**
     * 监听「全屏播放屏幕方向 / 桌面端全屏方式」设置
     *
     * 两个平台共用 [SettingPreferences.PlayerFullMode]，只是取值域不同（见 [DesktopFullscreenMode]）。
     * 旧版每次进入全屏都实时读 DataStore，因此设置页改完立刻生效；
     * 这里用订阅保持两个模式流与设置同步，达到同样的效果。
     * 全屏状态中改动设置时按新方向重新应用（旧版长按菜单选完也会立即生效）。
     *
     * 桌面端全屏方式只做同步：窗口全屏与否由桌面宿主消费该状态决定。
     *
     * 幂等：重复调用只保留一个监听协程。
     */
    private fun observeFullscreenModeSetting() {
        if (settingsJob?.isActive == true) return
        settingsJob = scope.launch {
            appDataStore.data.collect { preferences ->
                val value = preferences[SettingPreferences.PlayerFullMode]
                _desktopFullscreenMode.value = DesktopFullscreenMode.fromValue(
                    value ?: SettingConstants.PLAYER_DESKTOP_FULL_MODE_SCREEN
                )
                val mode = FullscreenMode.fromValue(value ?: SettingConstants.PLAYER_FULL_MODE_AUTO)
                if (_fullscreenMode.value == mode) return@collect
                _fullscreenMode.value = mode
                if (_isFullscreen.value) {
                    applyScreenOrientation(mode)
                }
            }
        }
    }

    /**
     * 切换全屏/小屏
     */
    fun toggleFullscreen() {
        if (_isFullscreen.value) {
            smallScreen()
        } else {
            fullscreen(_fullscreenMode.value)
        }
    }

    /**
     * 进入全屏
     *
     * 只按 [mode] 应用屏幕方向，**不写回** [SettingPreferences.PlayerFullMode]：
     * 自动全屏传入的 [FullscreenMode.UNSPECIFIED] 若写回设置，会把用户在设置页
     * 选的方向冲掉（旧版 fullScreen 同样不持久化，只有长按菜单选择时才写入）。
     *
     * @param mode 全屏模式
     * @param onlyFull 是否仅全屏（退出时关闭播放器）
     */
    fun fullscreen(mode: FullscreenMode = _fullscreenMode.value, onlyFull: Boolean = false) {
        onlyFullscreen = onlyFull
        canAutoCloseFullscreen = false
        _isFullscreen.value = true
        applyScreenOrientation(mode)
    }

    /**
     * 退出全屏，回到小屏
     */
    fun smallScreen() {
        _isFullscreen.value = false
        applyAppSettingScreenOrientation()
    }

    /**
     * 设置全屏模式（长按全屏按钮时调用）
     */
    fun setFullscreenMode(mode: FullscreenMode) {
        _fullscreenMode.value = mode
        if (_isFullscreen.value) {
            applyScreenOrientation(mode)
        }
        scope.launch(Dispatchers.IO) {
            SettingPreferences.editPreferences {
                it[SettingPreferences.PlayerFullMode] = mode.value
            }
        }
    }

    /**
     * 设置桌面端全屏方式（桌面端长按/右键全屏按钮时调用）
     *
     * 写的是共用的 [SettingPreferences.PlayerFullMode]，取值见 [DesktopFullscreenMode]。
     * 不在这里动窗口：窗口形态由桌面宿主观察 [desktopFullscreenMode] 后应用，
     * 这样切换模式时（含全屏播放中切换）两处行为一致。
     */
    fun setDesktopFullscreenMode(mode: DesktopFullscreenMode) {
        _desktopFullscreenMode.value = mode
        scope.launch(Dispatchers.IO) {
            SettingPreferences.editPreferences {
                it[SettingPreferences.PlayerFullMode] = mode.value
            }
        }
    }

    /**
     * 播放器打开时检查是否默认全屏
     *
     * 对齐原版 `checkIsPlayerDefaultFull`：
     * 根据屏幕方向和 [SettingPreferences.PlayerOpenMode] 自动进入全屏。
     */
    fun checkIsPlayerDefaultFull() {
        scope.launch(Dispatchers.IO) {
            val openMode = SettingPreferences.mapPreferences {
                it[SettingPreferences.PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
            }
            val autoFullScreenPortrait = openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN != 0
            val autoFullScreenLandscape = openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE != 0
            val autoFull = (orientation == ORIENTATION_PORTRAIT && autoFullScreenPortrait) ||
                    (orientation == ORIENTATION_LANDSCAPE && autoFullScreenLandscape)

            // 方向请求会直接落到 Activity 窗口，必须回主线程执行
            withContext(Dispatchers.Main) {
                if (autoFull) {
                    fullscreen(_fullscreenMode.value, onlyFull = true)
                }
            }
        }
    }

    /**
     * 屏幕方向变化时调用
     *
     * 对齐原版 `onChangedScreenOrientation`：
     * 根据 [SettingPreferences.PlayerOpenMode] 自动切换全屏/小屏。
     *
     * @param newOrientation 新的屏幕方向 ([ORIENTATION_PORTRAIT] 或 [ORIENTATION_LANDSCAPE])
     */
    fun onOrientationChanged(newOrientation: Int) {
        orientation = newOrientation
        scope.launch(Dispatchers.IO) {
            val openMode = SettingPreferences.mapPreferences {
                it[SettingPreferences.PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
            }
            val autoFullScreen = if (newOrientation == ORIENTATION_PORTRAIT) {
                openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN != 0
            } else {
                openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE != 0
            }
            if (autoFullScreen && !_isFullscreen.value) {
                // 方向请求会直接落到 Activity 窗口，必须回主线程执行
                withContext(Dispatchers.Main) {
                    // 自动切换全屏
                    fullscreen(FullscreenMode.UNSPECIFIED)
                    canAutoCloseFullscreen = true
                }
            } else if (!autoFullScreen && canAutoCloseFullscreen && _isFullscreen.value) {
                withContext(Dispatchers.Main) {
                    // 自动切回小屏
                    smallScreen()
                }
            }
        }
    }

    /**
     * 是否仅全屏模式（退出时关闭播放器而非回小屏）
     */
    fun isOnlyFullscreen(): Boolean = onlyFullscreen

    /**
     * 释放设置监听
     *
     * 控制器随界面重建而重建、但协程作用域是进程级的，
     * 界面销毁时需取消订阅，否则旧实例会一直持有已销毁 Activity 的方向回调。
     */
    fun release() {
        settingsJob?.cancel()
        settingsJob = null
    }

    /**
     * 根据全屏模式应用屏幕方向
     */
    private fun applyScreenOrientation(mode: FullscreenMode) {
        val request = when (mode) {
            FullscreenMode.SENSOR_LANDSCAPE -> ScreenOrientationRequest.SENSOR_LANDSCAPE
            FullscreenMode.LANDSCAPE -> ScreenOrientationRequest.LANDSCAPE
            FullscreenMode.REVERSE_LANDSCAPE -> ScreenOrientationRequest.REVERSE_LANDSCAPE
            FullscreenMode.UNSPECIFIED -> getAppSettingScreenOrientation()
            FullscreenMode.AUTO -> {
                // 跟随视频：竖向视频时为不指定方向，横向视频时为横向全屏(自动旋转)
                val screenProportion = playbackInfo?.screenProportion ?: 1f
                if (screenProportion < 1f) {
                    getAppSettingScreenOrientation()
                } else {
                    ScreenOrientationRequest.SENSOR_LANDSCAPE
                }
            }
        }
        _screenOrientation.value = request
        setRequestedOrientation(request)
    }

    /**
     * 获取应用设置的屏幕方向（考虑竖屏锁定）
     */
    private fun getAppSettingScreenOrientation(): ScreenOrientationRequest {
        return if (isLockScreenOrientationPortraitProvider()) {
            ScreenOrientationRequest.PORTRAIT
        } else {
            ScreenOrientationRequest.UNSPECIFIED
        }
    }

    /**
     * 应用 App 设置的屏幕方向（退出全屏时使用）
     */
    private fun applyAppSettingScreenOrientation() {
        val request = getAppSettingScreenOrientation()
        _screenOrientation.value = request
        setRequestedOrientation(request)
    }

    companion object {
        const val ORIENTATION_PORTRAIT = 1
        const val ORIENTATION_LANDSCAPE = 2
    }
}
