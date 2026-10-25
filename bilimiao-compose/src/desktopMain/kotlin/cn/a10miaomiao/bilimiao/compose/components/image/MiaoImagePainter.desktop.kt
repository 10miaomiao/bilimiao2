package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
actual fun rememberMiaoImagePainter(
    model: Any?,
    onSuccess: () -> Unit,
): Painter? {
    val url = model as? String
    if (url == null || !url.looksLikeGif()) {
        return rememberCoilImagePainter(model, onSuccess)
    }

    var animation by remember(url) { mutableStateOf<GifAnimation?>(null) }
    val frameState = remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        val bytes = loadImageBytes(url) ?: return@LaunchedEffect
        val decoded = withContext(Dispatchers.Default) {
            runCatching { GifAnimation.decode(bytes, PREVIEW_MAX_DECODE_SIZE) }.getOrNull()
        } ?: return@LaunchedEffect
        if (decoded.frameCount > 1) {
            animation = decoded
        } else {
            decoded.close()
        }
    }

    val gif = animation
    if (gif == null) {
        // 动图数据未就绪时先用 Coil 显示首帧
        return rememberCoilImagePainter(model, onSuccess)
    }

    DisposableEffect(gif) {
        onDispose { gif.close() }
    }
    LaunchedEffect(gif) {
        var index = 0
        var notified = false
        while (isActive) {
            val bitmap = withContext(Dispatchers.Default) {
                runCatching { gif.frame(index) }.getOrNull()
            }
            if (bitmap != null) {
                frameState.value = bitmap
                if (!notified) {
                    // 首帧可显示后再回调，保证缩略图切换时大图已经有内容
                    notified = true
                    onSuccess()
                }
            }
            delay(gif.duration(index).coerceIn(MIN_FRAME_DELAY, MAX_FRAME_DELAY).toLong())
            index = (index + 1) % gif.frameCount
        }
    }

    if (frameState.value == null) {
        // 首帧尚未解码完成，继续用 Coil 的首帧占位，避免闪黑
        return rememberCoilImagePainter(model, onSuccess)
    }

    return remember(gif) {
        GifPainter(
            frameState = frameState,
            intrinsicSize = Size(gif.width.toFloat(), gif.height.toFloat()),
        )
    }
}

/**
 * 读取帧状态并绘制的大图 Painter，帧变化会触发 draw 阶段重绘。
 */
private class GifPainter(
    private val frameState: State<ImageBitmap?>,
    override val intrinsicSize: Size,
) : Painter() {

    override fun DrawScope.onDraw() {
        val frame = frameState.value ?: return
        val width = size.width.roundToInt().coerceAtLeast(1)
        val height = size.height.roundToInt().coerceAtLeast(1)
        drawImage(
            image = frame,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(frame.width, frame.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(width, height),
            filterQuality = FilterQuality.Medium,
        )
    }
}
