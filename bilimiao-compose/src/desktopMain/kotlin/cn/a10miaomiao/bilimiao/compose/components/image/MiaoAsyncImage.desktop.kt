package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
actual fun MiaoAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: Painter?,
    error: Painter?,
) {
    val url = model as? String
    // 非 GIF 地址直接交给 Coil，避免额外的网络开销
    if (url == null || !url.looksLikeGif()) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            placeholder = placeholder,
            error = error,
        )
        return
    }

    var animation by remember(url) { mutableStateOf<GifAnimation?>(null) }
    LaunchedEffect(url) {
        val bytes = loadImageBytes(url) ?: return@LaunchedEffect
        val decoded = withContext(Dispatchers.Default) {
            runCatching { GifAnimation.decode(bytes) }.getOrNull()
        } ?: return@LaunchedEffect
        if (decoded.frameCount > 1) {
            animation = decoded
        } else {
            decoded.close()
        }
    }

    val gif = animation
    if (gif != null) {
        DisposableEffect(gif) {
            onDispose { gif.close() }
        }
        GifImage(
            animation = gif,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = error,
        )
    } else {
        // 动图数据就绪前先用 Coil 显示首帧
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            placeholder = placeholder,
            error = error,
        )
    }
}

@Composable
private fun GifImage(
    animation: GifAnimation,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    error: Painter?,
) {
    var frame by remember(animation) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(animation) {
        var index = 0
        while (isActive) {
            val bitmap = withContext(Dispatchers.Default) {
                runCatching { animation.frame(index) }.getOrNull()
            }
            if (bitmap != null) {
                frame = bitmap
            }
            delay(animation.duration(index).coerceIn(MIN_FRAME_DELAY, MAX_FRAME_DELAY).toLong())
            index = (index + 1) % animation.frameCount
        }
    }

    val bitmap = frame
    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )

        error != null -> Image(
            painter = error,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )

        else -> Box(modifier)
    }
}
