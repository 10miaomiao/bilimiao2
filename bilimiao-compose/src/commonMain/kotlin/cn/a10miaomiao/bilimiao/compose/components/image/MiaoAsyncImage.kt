package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter

/**
 * 支持动图（GIF）的异步图片组件。
 *
 * - Android：coil-gif 解码，Coil 自动播放
 * - 桌面端：skiko Codec 逐帧解码并驱动重绘（Coil 3 在桌面端只解码首帧）
 */
@Composable
expect fun MiaoAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: Painter? = null,
    error: Painter? = null,
)

/**
 * 图片预览器（大图）使用的 Painter 版本。
 *
 * 返回 null 表示尚未加载完成，调用方可以继续显示加载占位。
 * Android 与桌面端的非动图场景由 Coil 解码；桌面端 GIF 由自绘 Painter 播放。
 */
@Composable
expect fun rememberMiaoImagePainter(
    model: Any?,
    onSuccess: () -> Unit = {},
): Painter?

/**
 * 用 Coil 加载图片并返回 Painter，仅在加载成功后返回非 null。
 */
@Composable
internal fun rememberCoilImagePainter(
    model: Any?,
    onSuccess: () -> Unit,
): Painter? {
    val platformContext = LocalPlatformContext.current
    val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
    var success by remember(model) { mutableStateOf(false) }
    val painter = rememberAsyncImagePainter(
        model = model,
        imageLoader = imageLoader,
        onSuccess = {
            success = true
            onSuccess()
        },
    )
    return if (success) painter else null
}
