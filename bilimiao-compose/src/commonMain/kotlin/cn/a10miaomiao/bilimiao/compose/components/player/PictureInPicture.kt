package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable

/**
 * 是否支持画中画（Picture-in-Picture）
 *
 * - 安卓：Android 8.0（API 26）及以上支持
 * - 桌面：暂不支持
 */
expect fun isPictureInPictureSupported(): Boolean

/**
 * 进入画中画模式
 *
 * @param aspectWidth 视频画面宽度（仅用于计算宽高比）
 * @param aspectHeight 视频画面高度
 * @param isPlaying 当前是否正在播放：决定画中画动作按钮为「暂停」还是「播放」
 * @return 是否成功进入画中画
 */
expect fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean

/**
 * 画中画动作按钮（播放/暂停）
 *
 * 画中画窗口内的画面不接收点击（窗口的单击被系统控制条/缩放接管），因此播放控制
 * 只能以系统动作按钮（安卓端为 RemoteAction）的形式交给系统绘制：
 *
 * - [enabled] 为 true（处于画中画窗口）时注册动作按钮的点击处理；
 * - [isPlaying] 变化时同步动作按钮图标（正在播放显示「暂停」，否则显示「播放」）；
 * - [enabled] 变回 false（退出画中画）时注销点击处理。
 *
 * 桌面端为 no-op。
 *
 * @param onTogglePlayPause 动作按钮点击回调（正在播放则暂停，否则播放）
 */
@Composable
expect fun PictureInPicturePlaybackAction(
    enabled: Boolean,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
)
