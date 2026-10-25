package cn.a10miaomiao.bilimiao.danmaku.platform

actual object PlatformClock {
    actual fun uptimeMillis(): Long = android.os.SystemClock.uptimeMillis()
}
