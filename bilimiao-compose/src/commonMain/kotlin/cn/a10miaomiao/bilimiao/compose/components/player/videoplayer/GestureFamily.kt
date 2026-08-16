@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer

import androidx.compose.runtime.Immutable

/**
 * 手势家族, 描述当前输入设备类型下哪些手势可用.
 *
 * 原始 animeko 中由 `Platform.mouseFamily` 推断, bilimiao-compose 在 commonMain 简化为 [TOUCH],
 * desktop 平台可用 `actual` 覆盖为 [MOUSE].
 */
@Immutable
enum class GestureFamily(
    /** 点击切换播放/暂停 */
    val clickToPauseResume: Boolean,
    /** 点击切换控制器显隐 */
    val clickToToggleController: Boolean,
    /** 双击进入/退出全屏 */
    val doubleClickToFullscreen: Boolean,
    /** 双击切换播放/暂停 */
    val doubleClickToPauseResume: Boolean,
    /** 横向滑动快进/快退 */
    val swipeToSeek: Boolean,
    /** 屏幕右侧滑动调节音量 */
    val swipeRhsForVolume: Boolean,
    /** 屏幕左侧滑动调节亮度 */
    val swipeLhsForBrightness: Boolean,
    /** 屏幕中间垂直滑动进入/退出全屏 */
    val swipeMidForFullscreen: Boolean,
    /** 长按快进 */
    val longPressForFastSkip: Boolean,
    /** 滚轮调节音量 */
    val scrollForVolume: Boolean,
    /** 自动隐藏控制器 */
    val autoHideController: Boolean,
    /** 音量控制器位于底部栏 */
    val volumeControllerOnBottomBar: Boolean,
    /** 鼠标悬停时显示控制器 (移动端不支持) */
    val mouseHoverForController: Boolean = true,
) {
    TOUCH(
        clickToPauseResume = false,
        clickToToggleController = true,
        doubleClickToFullscreen = false,
        doubleClickToPauseResume = true,
        swipeToSeek = true,
        swipeRhsForVolume = true,
        swipeLhsForBrightness = true,
        swipeMidForFullscreen = true,
        longPressForFastSkip = true,
        volumeControllerOnBottomBar = false,
        scrollForVolume = false,
        autoHideController = true,
        mouseHoverForController = false,
    ),
    MOUSE(
        clickToPauseResume = true,
        clickToToggleController = false,
        doubleClickToFullscreen = true,
        doubleClickToPauseResume = false,
        swipeToSeek = false,
        swipeRhsForVolume = false,
        swipeLhsForBrightness = false,
        swipeMidForFullscreen = false,
        longPressForFastSkip = false,
        scrollForVolume = true,
        autoHideController = false,
        volumeControllerOnBottomBar = true,
    )
}
