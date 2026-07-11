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
import cn.a10miaomiao.bilimiao.danmaku.platform.SkiaDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine

/**
 * 桌面端弹幕渲染覆盖层
 *
 * 直接在 Compose draw 阶段渲染弹幕到 Skia Canvas（GPU 后端），
 * 完全绕过 AWT BufferedImage 中间层。
 *
 * 使用 Canvas (androidx.compose.foundation.Canvas) 而非 Box+drawWithCache，
 * 确保绘制操作与视频播放器的 Canvas 在同一个 Compose 绘制层中。
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
    val displayer = remember { mutableStateOf<SkiaDisplayer?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 初始化引擎
    LaunchedEffect(danmakuParser) {
        val parser = danmakuParser ?: return@LaunchedEffect
        val context = DanmakuContext.create()
        val disp = SkiaDisplayer(context)
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

    // 帧驱动：每帧请求重绘
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTick++ }
        }
    }

    val currentEngine = engine.value
    val currentDisplayer = displayer.value
    if (currentEngine != null && currentDisplayer != null && visible) {
        Canvas(modifier = modifier) {
            // 读取 frameTick 触发每帧重绘
            frameTick

            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w <= 0 || h <= 0) return@Canvas

            currentDisplayer.setSize(w, h)

            drawIntoCanvas { canvas ->
                val skiaCanvas = canvas.nativeCanvas
                currentDisplayer.setCanvas(skiaCanvas)
                currentEngine.drawWithSync(currentDisplayer)
                currentDisplayer.setCanvas(null)
            }
        }
    }
}
