package cn.a10miaomiao.bilimiao.compose.components.player

import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import com.a10miaomiao.bilimiao.comm.delegate.player.ActivityHolder

/**
 * 安卓端 actual：画中画支持
 *
 * 画中画需要 Android 8.0（API 26）及以上。
 * Activity 引用通过 [ActivityHolder] 获取（在 MainActivity 中初始化）。
 */
actual fun isPictureInPictureSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * 安卓端 actual：进入画中画模式
 *
 * 通过 [android.app.PictureInPictureParams] 设置视频宽高比后进入画中画。
 * 视频尺寸未知时使用 16:9 兜底。
 */
actual fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val activity = ActivityHolder.get() ?: return false
    return try {
        val width = aspectWidth.coerceAtLeast(1)
        val height = aspectHeight.coerceAtLeast(1)
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(width, height))
            .build()
        activity.enterPictureInPictureMode(params)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
