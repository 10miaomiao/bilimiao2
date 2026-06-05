package cn.a10miaomiao.bilimiao.compose.platform

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
}
