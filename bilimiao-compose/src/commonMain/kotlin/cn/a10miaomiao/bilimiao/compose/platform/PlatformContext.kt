package cn.a10miaomiao.bilimiao.compose.platform

import androidx.compose.runtime.staticCompositionLocalOf
import com.a10miaomiao.bilimiao.comm.delegate.player.ScreenOrientationRequest

interface PlatformContext {
    fun openUrl(url: String)
    fun copyToClipboard(text: String)
    fun shareText(text: String)
    fun openCoverImage(aid: String) {}

    /**
     * 播放期间保持屏幕常亮（与是否全屏无关，对齐旧版 PlayerDelegate2.setStateAndUi）。
     *
     * - 安卓：直接打在当前 Activity 窗口上的 `FLAG_KEEP_SCREEN_ON`
     * - 桌面：no-op（桌面窗口不参与系统屏幕超时）
     *
     * 须在主线程调用（Compose 的 DisposableEffect 已在主线程）。
     */
    fun setKeepScreenOn(enabled: Boolean)

    /**
     * 请求屏幕方向（全屏播放 / 退出全屏时由 FullscreenController 触发）
     *
     * - 安卓：映射到当前 Activity 的 `requestedOrientation`
     * - 桌面：no-op（桌面无屏幕方向概念，全屏由窗口管理处理）
     *
     * 须在主线程调用。
     */
    fun setRequestedOrientation(request: ScreenOrientationRequest)

    // ---- 播放器窗口能力（由平台实现直接作用于当前窗口）----

    /** 是否支持画中画（应用外小窗） */
    fun isPictureInPictureSupported(): Boolean

    /**
     * 进入画中画（应用外小窗）
     *
     * @param aspectWidth 视频宽
     * @param aspectHeight 视频高
     * @param isPlaying 初始动作按钮是「暂停」（播放中）还是「播放」
     * @return 是否成功进入；不支持或调用失败返回 false
     */
    fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean

    /** 同步画中画动作按钮（播放/暂停）的图标与行为 */
    fun setPictureInPictureActions(isPlaying: Boolean)

    /**
     * 注册画中画动作按钮的点击回调
     *
     * 动作按钮的点击经广播回到应用，注册/注销需与画中画窗口的生命周期一致。
     *
     * @return 注销函数，须在画中画窗口离开组合时调用
     */
    fun registerPictureInPictureActionReceiver(onTogglePlayPause: () -> Unit): () -> Unit

    /** 当前窗口亮度（0f..1f）；-1f 表示跟随系统自动亮度 */
    fun getWindowBrightness(): Float

    /** 设置当前窗口亮度（0f..1f）；传 -1f 恢复跟随系统自动亮度 */
    fun setWindowBrightness(brightness: Float)

    /** 系统亮度设置（0f..1f），跟随系统自动亮度时作为显示与调整的基准 */
    fun getSystemBrightness(): Float
}

val LocalPlatformContext = staticCompositionLocalOf<PlatformContext> {
    error("PlatformContext not provided")
}
