package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 平台时钟抽象
 */
expect object PlatformClock {
    /**
     * 获取系统启动至今的毫秒数（不含休眠）
     */
    fun uptimeMillis(): Long

    /**
     * 休眠指定毫秒
     */
    fun sleep(millis: Long)
}
