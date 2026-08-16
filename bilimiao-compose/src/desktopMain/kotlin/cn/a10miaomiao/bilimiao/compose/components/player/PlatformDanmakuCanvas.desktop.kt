package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.platform.SkiaDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine

/**
 * 桌面端 actual：通过 Skia Canvas 渲染弹幕
 *
 * 直接在 Compose Draw 阶段渲染到 Skia Canvas（GPU 后端），
 * 完全绕过 AWT BufferedImage 中间层。
 */
@Composable
actual fun PlatformDanmakuCanvas(
    engine: DanmakuEngine,
    displayer: IDisplayer,
    frameTick: Long,
    modifier: Modifier,
) {
    val skiaDisplayer = displayer as SkiaDisplayer
    Canvas(modifier = modifier) {
        // 读取 frameTick 触发每帧重绘
        frameTick

        val w = size.width.toInt()
        val h = size.height.toInt()
        if (w <= 0 || h <= 0) return@Canvas

        skiaDisplayer.setSize(w, h)

        drawIntoCanvas { canvas ->
            val skiaCanvas = canvas.skiaCanvas
            skiaCanvas.save()
            skiaCanvas.clipRect(org.jetbrains.skia.Rect.makeWH(size.width, size.height))
            skiaDisplayer.setCanvas(skiaCanvas)
            engine.drawWithSync(skiaDisplayer)
            skiaDisplayer.setCanvas(null)
            skiaCanvas.restore()
        }
    }
}
