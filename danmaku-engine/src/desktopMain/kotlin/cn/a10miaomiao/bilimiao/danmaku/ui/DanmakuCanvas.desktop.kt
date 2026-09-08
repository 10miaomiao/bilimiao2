package cn.a10miaomiao.bilimiao.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.layout.onSizeChanged
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.platform.SkiaDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine

/**
 * 桌面端 actual：通过 Skia Canvas 渲染弹幕
 *
 * 直接在 Compose Draw 阶段渲染到 Skia Canvas（GPU 后端），
 * 完全绕过 AWT BufferedImage 中间层。
 * 画布尺寸变化时通过 [DanmakuEngine.notifyDispSizeChanged] 通知引擎（更新布局并清轨道占用）。
 */
@Composable
actual fun DanmakuCanvas(
    engine: DanmakuEngine,
    displayer: IDisplayer,
    frameTick: Long,
    modifier: Modifier,
) {
    val skiaDisplayer = displayer as SkiaDisplayer
    Canvas(modifier = modifier.onSizeChanged { size ->
        // 画布尺寸变化时通知引擎（内部已判断是否真的变化）
        if (size.width > 0 && size.height > 0) {
            engine.notifyDispSizeChanged(size.width, size.height)
        }
    }) {
        // 读取 frameTick 触发每帧重绘
        frameTick

        val w = size.width.toInt()
        val h = size.height.toInt()
        if (w <= 0 || h <= 0) return@Canvas

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
