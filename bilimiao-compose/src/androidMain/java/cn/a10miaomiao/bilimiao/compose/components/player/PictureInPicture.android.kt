package cn.a10miaomiao.bilimiao.compose.components.player

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import cn.a10miaomiao.bilimiao.compose.R
import com.a10miaomiao.bilimiao.comm.delegate.player.ActivityHolder

/**
 * 画中画播放/暂停动作按钮的广播 action
 *
 * 动作由本应用在画中画期间动态注册的接收器处理，不对外暴露。
 */
private const val ACTION_PICTURE_IN_PICTURE_PLAYBACK =
    "cn.a10miaomiao.bilimiao.action.PICTURE_IN_PICTURE_PLAYBACK"

/**
 * 动作按钮待发广播的请求码（播放与暂停共用同一个待发广播）
 */
private const val REQUEST_CODE_PICTURE_IN_PICTURE_PLAYBACK = 1

/**
 * 进入画中画时使用的宽高比
 *
 * 更新动作按钮会整体替换 [PictureInPictureParams]，复用同一宽高比可避免小窗尺寸跳变。
 */
private var pictureInPictureAspectRatio: Rational? = null

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
 * 通过 [android.app.PictureInPictureParams] 设置视频宽高比与播放/暂停动作按钮后进入画中画。
 * 视频尺寸未知时使用 16:9 兜底。
 */
actual fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val activity = ActivityHolder.get() ?: return false
    return try {
        val aspectRatio = Rational(aspectWidth.coerceAtLeast(1), aspectHeight.coerceAtLeast(1))
        pictureInPictureAspectRatio = aspectRatio
        activity.enterPictureInPictureMode(
            buildPictureInPictureParams(activity, aspectRatio, isPlaying)
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 安卓端 actual：画中画动作按钮（播放/暂停）
 *
 * 动作按钮的点击经待发广播回到本应用，这里按画中画窗口的生命周期注册/注销接收器；
 * 播放状态变化时同步按钮图标（见 [updatePictureInPictureActions]）。
 */
@Composable
actual fun PictureInPicturePlaybackAction(
    enabled: Boolean,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
) {
    val activity = ActivityHolder.get()
    // 持有 State 本体而非当次取值：接收器回调里要读到最新的回调闭包
    val togglePlayPause = rememberUpdatedState(onTogglePlayPause)

    // 播放状态变化时同步动作按钮图标
    LaunchedEffect(enabled, isPlaying) {
        if (enabled) {
            updatePictureInPictureActions(isPlaying)
        }
    }

    DisposableEffect(enabled, activity) {
        if (!enabled || activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return@DisposableEffect onDispose {}
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_PICTURE_IN_PICTURE_PLAYBACK) {
                    togglePlayPause.value()
                }
            }
        }
        ContextCompat.registerReceiver(
            activity,
            receiver,
            IntentFilter(ACTION_PICTURE_IN_PICTURE_PLAYBACK),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose {
            runCatching { activity.unregisterReceiver(receiver) }
        }
    }
}

/**
 * 更新画中画动作按钮（播放/暂停），沿用进入画中画时的宽高比
 */
private fun updatePictureInPictureActions(isPlaying: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val activity = ActivityHolder.get() ?: return
    val aspectRatio = pictureInPictureAspectRatio ?: return
    try {
        activity.setPictureInPictureParams(
            buildPictureInPictureParams(activity, aspectRatio, isPlaying)
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildPictureInPictureParams(
    activity: Activity,
    aspectRatio: Rational,
    isPlaying: Boolean,
): PictureInPictureParams = PictureInPictureParams.Builder()
    .setAspectRatio(aspectRatio)
    .setActions(listOf(playPauseAction(activity, isPlaying)))
    .build()

/**
 * 播放/暂停动作按钮：正在播放时按钮为「暂停」，否则为「播放」
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun playPauseAction(activity: Activity, isPlaying: Boolean): RemoteAction {
    val title = if (isPlaying) "暂停" else "播放"
    val intent = Intent(ACTION_PICTURE_IN_PICTURE_PLAYBACK)
        .setPackage(activity.packageName)
    val pendingIntent = PendingIntent.getBroadcast(
        activity,
        REQUEST_CODE_PICTURE_IN_PICTURE_PLAYBACK,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    return RemoteAction(
        Icon.createWithResource(
            activity,
            if (isPlaying) R.drawable.ic_pip_pause else R.drawable.ic_pip_play,
        ),
        title,
        title,
        pendingIntent,
    )
}
