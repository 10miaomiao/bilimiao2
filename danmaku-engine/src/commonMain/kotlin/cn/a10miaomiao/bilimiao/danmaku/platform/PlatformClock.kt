package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 平台时钟抽象
 *
 * 弹幕时钟（渲染时钟）只按这个单调墙钟推进，与播放器位置无关。
 */
expect object PlatformClock {
    /**
     * 获取系统启动至今的毫秒数（不含休眠）
     */
    fun uptimeMillis(): Long
}
