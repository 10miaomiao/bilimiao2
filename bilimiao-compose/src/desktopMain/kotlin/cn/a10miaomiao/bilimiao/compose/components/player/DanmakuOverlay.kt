package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import cn.a10miaomiao.bilimiao.danmaku.cache.SimpleTextCacheStuffer
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.DesktopDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.skia.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer

/**
 * 桌面端弹幕渲染覆盖层
 *
 * 使用 DanmakuEngine 渲染弹幕到 BufferedImage，再通过 Skia 绘制到 Compose Canvas。
 *
 * @param currentPosition 当前播放位置（毫秒）
 * @param isPlaying 是否正在播放
 * @param danmakuParser 弹幕解析器（null 表示无弹幕数据）
 * @param modifier Compose 修饰符
 */
@Composable
fun DanmakuOverlay(
    currentPosition: Long,
    isPlaying: Boolean,
    danmakuParser: BaseDanmakuParser?,
    modifier: Modifier = Modifier,
) {
    val engine = remember { mutableStateOf<DanmakuEngine?>(null) }
    val displayer = remember { mutableStateOf<DesktopDisplayer?>(null) }
    val renderTick = remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 初始化引擎
    LaunchedEffect(danmakuParser) {
        val parser = danmakuParser ?: return@LaunchedEffect
        val context = DanmakuContext.create()
        val disp = DesktopDisplayer(context)
        disp.setDensities(density.density, (density.density * 160).toInt(), density.density)
        context.mDisplayer = disp
        context.setCacheStuffer(SimpleTextCacheStuffer(), null)

        val eng = DanmakuEngine(scope)
        eng.setConfig(context)
        eng.setParser(parser)
        eng.setCallback(object : DanmakuEngine.Callback {
            override fun prepared() {
                eng.start()
            }
        })
        eng.prepare()

        displayer.value = disp
        engine.value = eng
    }

    // 同步播放位置
    var lastSyncPosition by remember { mutableStateOf(0L) }
    LaunchedEffect(currentPosition, isPlaying) {
        val eng = engine.value ?: return@LaunchedEffect
        if (isPlaying) {
            if (eng.isStop()) {
                eng.resume()
            }
            // 仅在跳转（位置差>2秒）时调用seekTo，正常播放由引擎内部syncTimer跟踪
            val delta = kotlin.math.abs(currentPosition - lastSyncPosition)
            if (delta > 2000 || lastSyncPosition == 0L) {
                eng.seekTo(currentPosition)
            }
            lastSyncPosition = currentPosition
        } else {
            eng.pause()
        }
    }

    // 渲染循环
    LaunchedEffect(engine.value) {
        val eng = engine.value ?: return@LaunchedEffect
        val disp = displayer.value ?: return@LaunchedEffect
        launch(Dispatchers.Default) {
            while (isActive) {
                disp.clearCanvas()
                eng.draw(disp)
                disp.swapBuffers() // 交换前后缓冲区，避免闪烁
                renderTick.value++
                kotlinx.coroutines.delay(16) // ~60fps
            }
        }
    }

    // 释放引擎
    DisposableEffect(Unit) {
        onDispose {
            engine.value?.release()
        }
    }

    // 绘制弹幕到 Compose Canvas
    val currentDisplayer = displayer.value
    if (currentDisplayer != null) {
        Canvas(modifier = modifier) {
            // 触发重组
            renderTick.value

            // 同步画布尺寸到 displayer
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w > 0 && h > 0 && (w != currentDisplayer.width || h != currentDisplayer.height)) {
                currentDisplayer.setSize(w, h)
            }

            val image = currentDisplayer.getImage() ?: return@Canvas
            val skiaImage = image.toSkiaImage() ?: return@Canvas

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                nativeCanvas.drawImage(skiaImage, 0f, 0f)
            }
        }
    }
}

/**
 * BufferedImage 转 Skia Image（直接像素拷贝，高性能）
 */
private fun BufferedImage.toSkiaImage(): Image? {
    if (width <= 0 || height <= 0) return null

    // 确保是 TYPE_INT_ARGB
    val argbImage = if (type != BufferedImage.TYPE_INT_ARGB) {
        val converted = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = converted.createGraphics()
        g.drawImage(this, 0, 0, null)
        g.dispose()
        converted
    } else {
        this
    }

    // 读取像素数据
    val dataBuffer = argbImage.raster.dataBuffer as DataBufferInt
    val pixels = dataBuffer.data

    // 转换为 Skia 期望的 RGBA 字节数组
    val byteCount = width * height * 4
    val bytes = ByteArray(byteCount)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val offset = i * 4
        // AWT TYPE_INT_ARGB: 0xAARRGGBB
        bytes[offset] = ((pixel shr 16) and 0xFF).toByte()     // R
        bytes[offset + 1] = ((pixel shr 8) and 0xFF).toByte()  // G
        bytes[offset + 2] = (pixel and 0xFF).toByte()           // B
        bytes[offset + 3] = ((pixel shr 24) and 0xFF).toByte() // A
    }

    // 创建 Skia Image
    val imageInfo = ImageInfo(
        width = width,
        height = height,
        colorType = ColorType.RGBA_8888,
        alphaType = ColorAlphaType.PREMUL
    )

    return Image.makeRaster(imageInfo, bytes, width * 4)
}
