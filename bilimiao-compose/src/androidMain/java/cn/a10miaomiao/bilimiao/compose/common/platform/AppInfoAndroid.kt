package cn.a10miaomiao.bilimiao.compose.common.platform

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class AppInfoAndroid(
    context: Context,
) : AppInfo {

    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    private val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
    private val iconDrawable = applicationInfo.loadIcon(context.packageManager)

    override val versionName: String = packageInfo.versionName ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toString()
    }

    override val appId: Int = 1

    override val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    @Composable
    override fun AppIcon(modifier: Modifier) {
        androidx.compose.foundation.Image(
            painter = rememberDrawablePainter(iconDrawable),
            contentDescription = "app icon",
            modifier = modifier,
        )
    }
}
