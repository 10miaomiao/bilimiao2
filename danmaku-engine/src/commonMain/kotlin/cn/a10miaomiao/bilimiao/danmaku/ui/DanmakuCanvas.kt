package cn.a10miaomiao.bilimiao.danmaku.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine

/**
 * 弹幕渲染 Canvas（Compose 组件，expect/actual）
 *
 * 在 Compose Draw 阶段渲染弹幕。平台差异：
 * - 安卓：通过 [androidx.compose.ui.graphics.drawscope.drawIntoCanvas] 获取 android.graphics.Canvas，
 *   注入 [cn.a10miaomiao.bilimiao.danmaku.platform.AndroidDisplayer]
 * - 桌面：通过 [androidx.compose.ui.graphics.skiaCanvas] 获取 Skia Canvas，
 *   注入 [cn.a10miaomiao.bilimiao.danmaku.platform.SkiaDisplayer]
 *
 * 注意：本组件是 Compose 渲染入口，与 [cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuCanvas]
 * （引擎内部的绘制画布抽象接口）不同。
 *
 * @param engine 弹幕引擎
 * @param displayer 平台 Displayer
 * @param frameTick 帧计数器（用于触发重绘）
 * @param modifier 布局修饰符
 */
@Composable
expect fun DanmakuCanvas(
    engine: DanmakuEngine,
    displayer: IDisplayer,
    frameTick: Long,
    modifier: Modifier = Modifier,
)
