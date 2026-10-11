package cn.a10miaomiao.bilimiao.compose.platform

import com.a10miaomiao.bilimiao.comm.delegate.player.ScreenOrientationRequest
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

class DesktopPlatformContext : PlatformContext {
    override fun openUrl(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun copyToClipboard(text: String) {
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun shareText(text: String) {
        // Desktop doesn't have a native share mechanism, copy to clipboard instead
        copyToClipboard(text)
    }

    /** 桌面窗口不参与系统屏幕超时，无对应窗口标志（no-op） */
    override fun setKeepScreenOn(enabled: Boolean) {
    }
    /** 桌面无屏幕方向概念，全屏由窗口管理处理（no-op） */
    override fun setRequestedOrientation(request: ScreenOrientationRequest) {
    }
    /** 桌面端暂不支持画中画 */
    override fun isPictureInPictureSupported(): Boolean = false

    override fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int, isPlaying: Boolean): Boolean = false

    override fun setPictureInPictureActions(isPlaying: Boolean) {
    }

    override fun registerPictureInPictureActionReceiver(onTogglePlayPause: () -> Unit): () -> Unit = {}

    /** 桌面无逐窗口亮度（-1 表示跟随系统） */
    override fun getWindowBrightness(): Float = -1f

    override fun setWindowBrightness(brightness: Float) {
    }

    override fun getSystemBrightness(): Float = 0.5f
}
