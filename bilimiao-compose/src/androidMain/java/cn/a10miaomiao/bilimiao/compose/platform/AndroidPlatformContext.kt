package cn.a10miaomiao.bilimiao.compose.platform

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import com.a10miaomiao.bilimiao.comm.delegate.player.ScreenOrientationRequest

/**
 * 安卓端 PlatformContext：由 MainActivity 以自身作为窗口宿主创建
 *
 * 因此画中画、窗口亮度、屏幕方向、屏幕常亮等窗口能力都直接作用于当前 Activity，
 * 且随 Activity 重建自动更新，无需全局持有 Activity。
 *
 * @param coverImageLauncher 封面页启动器，由 app 层注入，避免 compose 模块反向依赖 cover 模块
 */
class AndroidPlatformContext(
    private val activity: Activity,
    private val coverImageLauncher: (String) -> Unit = {},
) : PlatformContext {
    /** 画中画（应用外小窗）的安卓实现（宽高比状态与动作按钮都在其中） */
    private val pictureInPicture = AndroidPictureInPicture(activity)

    override fun openUrl(url: String) {
        try {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(activity, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to default browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
        }
    }

    override fun copyToClipboard(text: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(intent, "分享"))
    }

    override fun openCoverImage(aid: String) {
        coverImageLauncher(aid)
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        val window = activity.window
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun setRequestedOrientation(request: ScreenOrientationRequest) {
        activity.requestedOrientation = when (request) {
            ScreenOrientationRequest.UNSPECIFIED -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ScreenOrientationRequest.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ScreenOrientationRequest.SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ScreenOrientationRequest.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ScreenOrientationRequest.REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
    }

    override fun isPictureInPictureSupported(): Boolean = pictureInPicture.isSupported

    override fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean =
        pictureInPicture.enter(aspectWidth, aspectHeight, isPlaying)

    override fun setPictureInPictureActions(isPlaying: Boolean) = pictureInPicture.updateActions(isPlaying)

    override fun registerPictureInPictureActionReceiver(onTogglePlayPause: () -> Unit): () -> Unit =
        pictureInPicture.registerReceiver(onTogglePlayPause)

    override fun getWindowBrightness(): Float = try {
        activity.window.attributes.screenBrightness
    } catch (_: Exception) {
        -1f
    }

    override fun setWindowBrightness(brightness: Float) {
        try {
            activity.window.attributes = activity.window.attributes.apply { screenBrightness = brightness }
        } catch (_: Exception) {}
    }

    override fun getSystemBrightness(): Float = try {
        // 系统亮度设置 0-255，读取失败时退回 50%
        Settings.System.getInt(activity.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toFloat() / 255f
    } catch (_: Exception) {
        0.5f
    }
}
