package cn.a10miaomiao.bilimiao.danmaku.platform

actual object PlatformClock {
    actual fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L
    actual fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (_: InterruptedException) {
        }
    }
}
