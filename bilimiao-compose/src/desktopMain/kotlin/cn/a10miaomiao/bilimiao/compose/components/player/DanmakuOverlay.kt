package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import cn.a10miaomiao.bilimiao.danmaku.cache.SimpleTextCacheStuffer
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.DesktopDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ColorAlphaType
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 桌面端弹幕渲染覆盖层
 *
 * 直接在 Compose draw 阶段渲染弹幕，无需后台线程和三缓冲。
 * 缓存资源由 DesktopDisplayer 管理，暂停时跳过渲染。
 */
@Composable
fun DanmakuOverlay(
    currentPosition: Long,
    isPlaying: Boolean,
    danmakuParser: BaseDanmakuParser?,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val engine = remember { mutableStateOf<DanmakuEngine?>(null) }
    val displayer = remember { mutableStateOf<DesktopDisplayer?>(null) }
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
        eng.nonBlockModeEnable = true
        eng.idleSleep = false
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
    LaunchedEffect(currentPosition, isPlaying) {
        val eng = engine.value ?: return@LaunchedEffect
        if (isPlaying) {
            if (eng.isStop()) {
                eng.resume()
            }
            eng.externalPlayerPosition = currentPosition
        } else {
            eng.pause()
        }
    }

    // 释放引擎
    DisposableEffect(Unit) {
        onDispose {
            engine.value?.release()
        }
    }

    // 驱动 UI 刷新
    var frameCount by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameCount++ }
        }
    }

    val currentEngine = engine.value
    val currentDisplayer = displayer.value
    if (currentEngine != null && currentDisplayer != null && visible) {
        Box(modifier = modifier.drawWithCache {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameCount
            val eng = currentEngine
            val disp = currentDisplayer

            val w = size.width.toInt()
            val h = size.height.toInt()

            var pixels: IntArray? = null
            var imgW = 0
            var imgH = 0

            if (w > 0 && h > 0) {
                // 渲染弹幕（DesktopDisplayer 内部缓存 BufferedImage 和 Graphics2D）
                disp.renderDanmaku(eng, w, h)
                pixels = disp.getRenderPixels()
                imgW = w
                imgH = h
            }

            onDrawBehind {
                if (pixels == null || imgW <= 0 || imgH <= 0) return@onDrawBehind
                val skiaImage = createSkiaImage(pixels, imgW, imgH) ?: return@onDrawBehind
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawImage(skiaImage, 0f, 0f)
                }
            }
        })
    }
}

// 可重用的 ByteArray，避免每帧分配
private var reusableBytes: ByteArray? = null

private fun createSkiaImage(pixels: IntArray, width: Int, height: Int): Image? {
    if (width <= 0 || height <= 0) return null
    val byteCount = pixels.size * 4
    var bytes = reusableBytes
    if (bytes == null || bytes.size < byteCount) {
        bytes = ByteArray(byteCount)
        reusableBytes = bytes
    }
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(pixels)
    return Image.makeRaster(
        ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.PREMUL),
        bytes, width * 4
    )
}
