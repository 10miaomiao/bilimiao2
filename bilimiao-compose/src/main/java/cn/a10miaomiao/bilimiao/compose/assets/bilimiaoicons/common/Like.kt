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

public val CommonGroup.Like: ImageVector
    get() {
        if (_like != null) {
            return _like!!
        }
        _like = Builder(name = "Like", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(757.76f, 852.91f)
                curveToRelative(36.91f, -0.02f, 72.83f, -30.21f, 79.3f, -66.56f)
                lineToRelative(51.09f, -287.04f)
                curveToRelative(10.07f, -56.47f, -27.09f, -100.52f, -84.37f, -100.1f)
                lineToRelative(-10.26f, 0.09f)
                arcToRelative(19972.27f, 19972.27f, 0.0f, false, true, -52.84f, 0.36f)
                arcToRelative(3552.85f, 3552.85f, 0.0f, false, true, -56.75f, 0.0f)
                lineToRelative(-31.0f, -0.43f)
                lineToRelative(11.5f, -28.8f)
                curveToRelative(10.24f, -25.64f, 21.76f, -95.74f, 21.5f, -128.02f)
                curveToRelative(-0.62f, -73.05f, -31.36f, -114.86f, -69.29f, -114.41f)
                curveToRelative(-46.61f, 0.55f, -69.46f, 23.47f, -69.33f, 91.14f)
                curveToRelative(0.21f, 112.66f, -102.14f, 226.11f, -225.13f, 225.11f)
                arcToRelative(1214.08f, 1214.08f, 0.0f, false, false, -20.63f, 0.0f)
                lineToRelative(-3.52f, 0.04f)
                curveToRelative(-0.19f, 0.0f, 0.64f, 409.11f, 0.64f, 409.11f)
                curveToRelative(0.0f, -0.09f, 459.09f, -0.49f, 459.09f, -0.49f)
                close()
                moveTo(740.46f, 356.99f)
                arcToRelative(15332.29f, 15332.29f, 0.0f, false, false, 52.69f, -0.36f)
                lineToRelative(10.28f, -0.09f)
                curveToRelative(84.01f, -0.62f, 141.44f, 67.52f, 126.72f, 150.25f)
                lineTo(879.06f, 793.81f)
                curveToRelative(-10.09f, 56.66f, -63.68f, 101.7f, -121.26f, 101.76f)
                lineToRelative(-458.92f, 0.38f)
                arcTo(42.67f, 42.67f, 0.0f, false, true, 256.0f, 853.55f)
                lineToRelative(-0.85f, -409.17f)
                arcToRelative(42.62f, 42.62f, 0.0f, false, true, 42.35f, -42.73f)
                lineToRelative(3.67f, -0.04f)
                curveToRelative(5.91f, -0.06f, 13.12f, -0.06f, 21.33f, 0.0f)
                curveToRelative(98.18f, 0.79f, 182.29f, -92.44f, 182.14f, -182.38f)
                curveTo(504.47f, 128.02f, 546.24f, 86.19f, 616.11f, 85.33f)
                curveToRelative(65.17f, -0.77f, 111.68f, 62.51f, 112.45f, 156.71f)
                curveToRelative(0.26f, 28.48f, -6.85f, 78.83f, -15.7f, 115.05f)
                curveToRelative(8.02f, 0.0f, 17.28f, -0.04f, 27.58f, -0.11f)
                close()
                moveTo(170.67f, 448.0f)
                verticalLineToRelative(405.33f)
                horizontalLineToRelative(23.47f)
                arcToRelative(21.33f, 21.33f, 0.0f, false, true, 0.0f, 42.67f)
                lineTo(154.84f, 896.0f)
                arcTo(26.71f, 26.71f, 0.0f, false, true, 128.0f, 869.33f)
                verticalLineToRelative(-437.33f)
                curveToRelative(0.0f, -14.78f, 12.07f, -26.67f, 26.77f, -26.67f)
                horizontalLineToRelative(38.91f)
                arcToRelative(21.33f, 21.33f, 0.0f, false, true, 0.0f, 42.67f)
                lineTo(170.67f, 448.0f)
                close()
            }
        }
        .build()
        return _like!!
    }

private var _like: ImageVector? = null
