package cn.a10miaomiao.bilimiao.compose.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import cn.a10miaomiao.bilimiao.cover.CoverActivity

class AndroidPlatformContext(
    private val context: Context,
) : PlatformContext {
    override fun openUrl(url: String) {
        try {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to default browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

    override fun openCoverImage(aid: String) {
        CoverActivity.launch(context, aid)
    }
}
