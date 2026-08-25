package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

/** 旧版（bilimiao 2.x）speed_tips 图标：三个快进三角 pathData（viewport 111x66） */
private val TRIANGLE_1_PATH_DATA =
    "M5.86,50.91C4.18,51.9 2.02,51.33 1.03,49.65C0.7,49.11 0.52,48.51 0.52,47.85C0.52,47.85 0.52,17.13 0.52,17.13C0.52,15.18 2.11,13.59 4.06,13.59C4.69,13.59 5.32,13.77 5.86,14.07C5.86,14.07 30.16,28.44 30.16,28.44C32.38,29.76 33.13,32.64 31.81,34.89C31.42,35.58 30.85,36.15 30.16,36.54C30.16,36.54 5.86,50.91 5.86,50.91z"
private val TRIANGLE_2_PATH_DATA =
    "M44.86,50.91C43.18,51.9 41.02,51.33 40.03,49.65C39.7,49.11 39.52,48.51 39.52,47.85C39.52,47.85 39.52,17.13 39.52,17.13C39.52,15.18 41.11,13.59 43.06,13.59C43.69,13.59 44.32,13.77 44.86,14.07C44.86,14.07 69.16,28.44 69.16,28.44C71.38,29.76 72.13,32.64 70.81,34.89C70.42,35.58 69.85,36.15 69.16,36.54C69.16,36.54 44.86,50.91 44.86,50.91z"
private val TRIANGLE_3_PATH_DATA =
    "M83.86,50.91C82.18,51.9 80.02,51.33 79.03,49.65C78.7,49.11 78.52,48.51 78.52,47.85C78.52,47.85 78.52,17.13 78.52,17.13C78.52,15.18 80.11,13.59 82.06,13.59C82.69,13.59 83.32,13.77 83.86,14.07C83.86,14.07 108.16,28.44 108.16,28.44C110.38,29.76 111.13,32.64 109.81,34.89C109.42,35.58 108.85,36.15 108.16,36.54C108.16,36.54 83.86,50.91 83.86,50.91z"

private fun buildPath(pathData: String): Path = PathParser().parsePathString(pathData).toPath()

/**
 * 快进动态图标
 *
 * 还原旧版 `shape_player_speed_tips_icon.xml`（animation-list）的动画：
 * 三个快进三角按 150ms/帧循环切换透明度（0.95/0.56/0.17），
 * 形成高亮向右滚动的快进效果。
 *
 * @param tint 图标颜色
 */
@Composable
fun FastForwardTrianglesIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    val tri1 = remember { buildPath(TRIANGLE_1_PATH_DATA) }
    val tri2 = remember { buildPath(TRIANGLE_2_PATH_DATA) }
    val tri3 = remember { buildPath(TRIANGLE_3_PATH_DATA) }

    val transition = rememberInfiniteTransition(label = "fastForwardTriangles")
    val alpha1 by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600 // 4 帧 * 150ms
                0.95f at 0
                0.56f at 150
                0.17f at 300
                0.56f at 450
                0.95f at 600
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "tri1",
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.56f,
        targetValue = 0.56f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.56f at 0
                0.95f at 150
                0.56f at 300
                0.17f at 450
                0.56f at 600
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "tri2",
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.17f,
        targetValue = 0.17f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                0.17f at 0
                0.56f at 150
                0.95f at 300
                0.56f at 450
                0.17f at 600
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "tri3",
    )

    Canvas(modifier) {
        // 按 viewport 111x66 等比缩放并居中绘制。
        // 注意 scale 需绕原点缩放（pivot = Offset.Zero），
        // 否则默认以画布中心为 pivot 会使图形整体偏移。
        val scale = min(size.width / 111f, size.height / 66f)
        val offsetX = (size.width - 111f * scale) / 2f
        val offsetY = (size.height - 66f * scale) / 2f
        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawPath(tri1, tint.copy(alpha = alpha1))
            drawPath(tri2, tint.copy(alpha = alpha2))
            drawPath(tri3, tint.copy(alpha = alpha3))
        }
    }
}
