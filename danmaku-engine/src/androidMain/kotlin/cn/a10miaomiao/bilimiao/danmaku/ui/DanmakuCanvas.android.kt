package cn.a10miaomiao.bilimiao.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.platform.AndroidDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine

/**
 * 安卓端 actual：通过 android.graphics.Canvas 渲染弹幕
 *
 * 在 Compose Draw 阶段获取底层 [android.graphics.Canvas]（通过 [nativeCanvas]），
 * 注入 [AndroidDisplayer] 进行渲染。与桌面端 Skia 实现对应，共用 [DanmakuEngine]。
 * 画布尺寸变化时通过 [DanmakuEngine.notifyDispSizeChanged] 通知引擎（更新布局并清轨道占用）。
 */
@Composable
actual fun DanmakuCanvas(
    engine: DanmakuEngine,
    displayer: IDisplayer,
    frameTick: Long,
    modifier: Modifier,
) {
    val androidDisplayer = displayer as AndroidDisplayer
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
            val androidCanvas = canvas.nativeCanvas
            androidCanvas.save()
            androidCanvas.clipRect(0f, 0f, size.width, size.height)
            androidDisplayer.setCanvas(androidCanvas)
            engine.drawWithSync(androidDisplayer)
            androidDisplayer.setCanvas(null)
            androidCanvas.restore()
        }
    }
}
