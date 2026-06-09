package cn.a10miaomiao.bilimiao.danmaku.platform

actual object PlatformClock {
    actual fun uptimeMillis(): Long = android.os.SystemClock.uptimeMillis()
    actual fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (_: InterruptedException) {
        }
    }
}
