package cn.a10miaomiao.bilimiao.compose.platform

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
import androidx.core.content.ContextCompat
import cn.a10miaomiao.bilimiao.compose.R

/**
 * 安卓端画中画（应用外小窗）实现
 *
 * 由 [AndroidPlatformContext] 持有并委托：进小窗、同步动作按钮、注册动作按钮回调。
 * Activity 在构造时注入（由 MainActivity 以自身窗口创建），因此不需要全局持有 Activity。
 */
internal class AndroidPictureInPicture(private val activity: Activity) {

    /**
     * 进入画中画时使用的宽高比
     *
     * 更新动作按钮会整体替换 [PictureInPictureParams]，复用同一宽高比可避免小窗尺寸跳变。
     */
    private var aspectRatio: Rational? = null

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /** 进入画中画；宽高比未知时由调用方传 16:9 兜底 */
    fun enter(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean {
        if (!isSupported) return false
        return try {
            val ratio = Rational(aspectWidth.coerceAtLeast(1), aspectHeight.coerceAtLeast(1))
            aspectRatio = ratio
            activity.enterPictureInPictureMode(buildParams(ratio, isPlaying))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** 播放状态变化时同步动作按钮图标 */
    fun updateActions(isPlaying: Boolean) {
        if (!isSupported) return
        val ratio = aspectRatio ?: return
        try {
            activity.setPictureInPictureParams(buildParams(ratio, isPlaying))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 注册动作按钮点击回调
     *
     * 动作按钮的点击经待发广播回到本应用，接收器在画中画窗口存续期间注册。
     *
     * @return 注销函数
     */
    fun registerReceiver(onTogglePlayPause: () -> Unit): () -> Unit {
        if (!isSupported) return {}
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_PLAYBACK) {
                    onTogglePlayPause()
                }
            }
        }
        ContextCompat.registerReceiver(
            activity,
            receiver,
            IntentFilter(ACTION_PLAYBACK),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        return { runCatching { activity.unregisterReceiver(receiver) } }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildParams(aspectRatio: Rational, isPlaying: Boolean): PictureInPictureParams =
        PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .setActions(listOf(playPauseAction(isPlaying)))
            .build()

    /** 播放/暂停动作按钮：正在播放时按钮为「暂停」，否则为「播放」 */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun playPauseAction(isPlaying: Boolean): RemoteAction {
        val title = if (isPlaying) "暂停" else "播放"
        val intent = Intent(ACTION_PLAYBACK).setPackage(activity.packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            REQUEST_CODE_PLAYBACK,
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

    private companion object {
        /** 画中画播放/暂停动作按钮的广播 action（接收器由本应用动态注册，不对外暴露） */
        const val ACTION_PLAYBACK = "cn.a10miaomiao.bilimiao.action.PICTURE_IN_PICTURE_PLAYBACK"

        /** 动作按钮待发广播的请求码（播放与暂停共用同一个待发广播） */
        const val REQUEST_CODE_PLAYBACK = 1
    }
}
