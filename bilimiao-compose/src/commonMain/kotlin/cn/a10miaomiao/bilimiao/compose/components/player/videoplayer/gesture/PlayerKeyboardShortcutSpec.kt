package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.runtime.Immutable

/**
 * 帮助浮层里的一行键位说明.
 *
 * @param keys 键位文本, 例如 `"Shift + ← / →"`
 * @param label 该键位的动作说明
 */
@Immutable
data class PlayerShortcutItem(val keys: String, val label: String)

/**
 * 帮助浮层里的一个分组, 例如「进度」.
 */
@Immutable
data class PlayerShortcutGroup(val title: String, val items: List<PlayerShortcutItem>)

/**
 * 播放器键盘快捷键的键位表.
 *
 * 与 [playerKeyboardShortcuts] 实际注册的键位保持一致: 新增/修改快捷键时必须同步更新这里,
 * 否则帮助浮层会展示不存在的按键。
 */
val playerKeyboardShortcutGroups: List<PlayerShortcutGroup> = listOf(
    PlayerShortcutGroup(
        title = "播放",
        items = listOf(
            PlayerShortcutItem("Space / K", "播放 / 暂停"),
            PlayerShortcutItem("H / ?", "显示 / 隐藏快捷键帮助"),
        ),
    ),
    PlayerShortcutGroup(
        title = "进度",
        items = listOf(
            PlayerShortcutItem("← / →", "后退 / 前进 5 秒"),
            PlayerShortcutItem("Shift + ← / →", "后退 / 前进 30 秒"),
            PlayerShortcutItem("PageUp / PageDown", "前进 / 后退 30 秒"),
            PlayerShortcutItem("Home / End", "跳到开头 / 结尾"),
            PlayerShortcutItem("1 - 9 / 0", "跳到 10% - 90% / 0%"),
        ),
    ),
    PlayerShortcutGroup(
        title = "音量与倍速",
        items = listOf(
            PlayerShortcutItem("↑ / ↓", "音量 +10% / -10%"),
            PlayerShortcutItem("Shift + ↑ / ↓", "音量 +1% / -1%"),
            PlayerShortcutItem("A / D", "倍速 -0.1 / +0.1"),
            PlayerShortcutItem("S", "倍速恢复 1.0x"),
            PlayerShortcutItem("M", "静音切换"),
        ),
    ),
    PlayerShortcutGroup(
        title = "画面",
        items = listOf(
            PlayerShortcutItem("F", "切换全屏"),
            PlayerShortcutItem("Esc", "退出全屏"),
            PlayerShortcutItem("B", "切换弹幕"),
        ),
    ),
)
