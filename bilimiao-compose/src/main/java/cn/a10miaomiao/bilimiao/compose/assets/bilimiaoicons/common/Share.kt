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

public val CommonGroup.Share: ImageVector
    get() {
        if (_share != null) {
            return _share!!
        }
        _share = Builder(name = "Share", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(604.31f, 852.62f)
                arcToRelative(75.44f, 75.44f, 0.0f, false, true, -75.4f, -75.38f)
                verticalLineToRelative(-80.66f)
                curveToRelative(-50.81f, 0.47f, -121.48f, 7.26f, -194.51f, 33.66f)
                curveToRelative(-47.8f, 17.28f, -74.68f, 35.18f, -75.88f, 36.0f)
                lineToRelative(-1.43f, 1.0f)
                lineToRelative(-1.41f, 0.74f)
                curveToRelative(-23.26f, 12.74f, -46.07f, 17.55f, -67.81f, 14.3f)
                curveToRelative(-20.18f, -3.0f, -38.25f, -12.93f, -52.27f, -28.7f)
                curveToRelative(-30.31f, -34.06f, -21.93f, -81.77f, -11.79f, -105.75f)
                arcTo(427.0f, 427.0f, 0.0f, false, true, 215.0f, 495.31f)
                curveToRelative(34.08f, -36.67f, 75.45f, -68.39f, 123.0f, -94.26f)
                curveToRelative(73.85f, -40.21f, 146.68f, -58.0f, 190.93f, -65.6f)
                verticalLineToRelative(-68.52f)
                arcTo(75.35f, 75.35f, 0.0f, false, true, 657.0f, 213.13f)
                lineTo(890.75f, 442.3f)
                arcToRelative(111.72f, 111.72f, 0.0f, false, true, 0.0f, 159.58f)
                lineTo(657.0f, 831.05f)
                arcToRelative(75.22f, 75.22f, 0.0f, false, true, -52.69f, 21.57f)
                close()
                moveTo(546.46f, 631.62f)
                arcTo(47.41f, 47.41f, 0.0f, false, true, 593.91f, 679.0f)
                verticalLineToRelative(98.21f)
                arcToRelative(10.35f, 10.35f, 0.0f, false, false, 17.59f, 7.39f)
                lineToRelative(233.75f, -229.13f)
                arcToRelative(46.75f, 46.75f, 0.0f, false, false, 0.0f, -66.76f)
                lineTo(611.5f, 259.54f)
                arcToRelative(10.35f, 10.35f, 0.0f, false, false, -17.59f, 7.39f)
                verticalLineToRelative(83.52f)
                arcToRelative(47.66f, 47.66f, 0.0f, false, true, -40.59f, 46.92f)
                curveToRelative(-37.0f, 5.38f, -110.15f, 20.42f, -184.26f, 60.77f)
                curveTo(278.0f, 507.75f, 216.0f, 579.05f, 184.9f, 670.07f)
                lineToRelative(-0.55f, 1.61f)
                lineToRelative(-0.45f, 1.0f)
                curveToRelative(-2.34f, 5.64f, -8.79f, 27.55f, 0.26f, 37.72f)
                curveToRelative(3.3f, 3.71f, 13.25f, 14.9f, 39.11f, 1.23f)
                curveToRelative(15.89f, -10.48f, 132.63f, -82.85f, 321.79f, -80.0f)
                curveToRelative(0.47f, -0.01f, 0.94f, -0.02f, 1.4f, -0.02f)
                close()
            }
        }
        .build()
        return _share!!
    }

private var _share: ImageVector? = null
