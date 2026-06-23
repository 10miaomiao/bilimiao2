package app.bilimiao.desktop

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfo
import org.jetbrains.skia.Image
import javax.imageio.ImageIO

class AppInfoDesktop : AppInfo {

    override val versionName: String = BuildConfig.VERSION_NAME
    override val versionCode: Long = BuildConfig.VERSION_CODE
    override val appId: Int = run {
        val osName = System.getProperty("os.name").orEmpty().lowercase()
        when {
            osName.contains("windows") -> 11
            osName.contains("linux") -> 12
            osName.contains("mac") -> 13
            else -> 0
        }
    }

    @Composable
    override fun AppIcon(modifier: Modifier) {
        val painter = remember {
            val awtImage = ImageIO.read(javaClass.getResourceAsStream("/ic_launcher.png"))
            val bytes = java.io.ByteArrayOutputStream().also { ImageIO.write(awtImage, "png", it) }.toByteArray()
            BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
        }
        Image(
            painter = painter,
            contentDescription = "app icon",
            modifier = modifier,
        )
    }
}
