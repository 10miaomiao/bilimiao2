package cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.CommonGroup

public val CommonGroup.Likefill: ImageVector
    get() {
        if (_likefill != null) {
            return _likefill!!
        }
        _likefill = Builder(name = "Likefill", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(710.55f, 384.81f)
                arcToRelative(12409.04f, 12409.04f, 0.0f, false, false, 47.47f, -0.32f)
                lineToRelative(8.75f, -0.09f)
                curveToRelative(83.99f, -0.62f, 141.44f, 67.58f, 126.72f, 150.23f)
                lineTo(847.3f, 794.03f)
                curveToRelative(-10.03f, 56.45f, -63.91f, 101.55f, -121.13f, 101.59f)
                lineTo(298.62f, 896.0f)
                arcToRelative(42.73f, 42.73f, 0.0f, false, true, -42.67f, -42.41f)
                lineToRelative(-0.81f, -383.98f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 42.03f, -42.67f)
                lineToRelative(3.16f, -0.06f)
                curveToRelative(5.23f, -0.04f, 11.8f, -0.04f, 19.63f, 0.0f)
                curveToRelative(91.95f, 0.77f, 170.88f, -86.7f, 170.71f, -170.94f)
                curveToRelative(-0.15f, -86.74f, 39.79f, -126.76f, 106.45f, -127.57f)
                curveToRelative(62.25f, -0.75f, 106.6f, 59.61f, 107.35f, 149.12f)
                curveToRelative(0.21f, 26.6f, -6.29f, 73.24f, -14.51f, 107.43f)
                curveToRelative(6.19f, 0.0f, 13.08f, -0.04f, 20.59f, -0.09f)
                close()
                moveTo(212.84f, 448.04f)
                lineTo(213.33f, 874.62f)
                arcTo(21.31f, 21.31f, 0.0f, false, true, 191.79f, 896.0f)
                lineTo(149.53f, 896.0f)
                arcTo(21.33f, 21.33f, 0.0f, false, true, 128.0f, 874.62f)
                lineToRelative(0.04f, -426.58f)
                arcTo(21.27f, 21.27f, 0.0f, false, true, 149.44f, 426.67f)
                horizontalLineToRelative(41.98f)
                curveToRelative(11.67f, 0.0f, 21.42f, 9.58f, 21.42f, 21.38f)
                close()
            }
        }
        .build()
        return _likefill!!
    }

private var _likefill: ImageVector? = null
