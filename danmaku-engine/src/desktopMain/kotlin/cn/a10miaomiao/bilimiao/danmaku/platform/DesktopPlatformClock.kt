package cn.a10miaomiao.bilimiao.danmaku.platform

actual object PlatformClock {
    actual fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L
}
