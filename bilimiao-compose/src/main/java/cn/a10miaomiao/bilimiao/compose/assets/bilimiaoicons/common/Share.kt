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
        _share = Builder(name = "Share", defaultWidth = 246.28906.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1261.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(662.2f, 1023.23f)
                arcToRelative(153.35f, 153.35f, 0.0f, false, true, -151.32f, -148.26f)
                curveToRelative(-1.32f, -26.56f, -1.25f, -53.18f, 0.0f, -79.67f)
                curveToRelative(0.98f, -23.28f, -8.78f, -31.3f, -31.23f, -27.88f)
                curveToRelative(-98.63f, 14.43f, -191.05f, 42.73f, -267.1f, 112.01f)
                arcToRelative(318.68f, 318.68f, 0.0f, false, false, -54.93f, 70.61f)
                curveToRelative(-28.44f, 46.0f, -58.13f, 61.69f, -98.56f, 51.37f)
                curveTo(21.84f, 991.73f, -0.81f, 961.9f, 0.02f, 910.18f)
                curveToRelative(2.44f, -143.73f, 36.59f, -278.81f, 121.0f, -397.79f)
                curveToRelative(86.08f, -121.63f, 204.37f, -194.82f, 347.89f, -228.28f)
                curveToRelative(31.16f, -6.97f, 45.79f, -19.1f, 42.45f, -53.04f)
                curveToRelative(-3.07f, -30.88f, -0.49f, -62.31f, -0.63f, -93.47f)
                curveToRelative(0.0f, -56.6f, 25.58f, -98.28f, 75.7f, -122.19f)
                reflectiveCurveToRelative(102.88f, -20.14f, 147.14f, 14.78f)
                quadToRelative(239.29f, 188.82f, 476.07f, 381.06f)
                curveToRelative(69.7f, 56.46f, 67.82f, 153.35f, -2.3f, 209.73f)
                quadToRelative(-226.6f, 182.41f, -453.62f, 364.34f)
                curveToRelative(-28.3f, 22.44f, -57.85f, 42.94f, -91.52f, 37.92f)
                close()
                moveTo(105.41f, 848.98f)
                curveToRelative(120.31f, -132.43f, 270.38f, -178.16f, 436.41f, -183.46f)
                curveToRelative(49.98f, -1.6f, 62.73f, 12.96f, 63.29f, 63.99f)
                curveToRelative(0.0f, 45.1f, -0.91f, 90.2f, 1.05f, 135.22f)
                arcToRelative(62.73f, 62.73f, 0.0f, false, false, 37.71f, 57.71f)
                curveToRelative(24.05f, 11.43f, 37.15f, -5.65f, 51.65f, -17.22f)
                quadToRelative(219.42f, -175.09f, 438.08f, -351.23f)
                curveToRelative(38.75f, -31.3f, 39.03f, -46.56f, -0.49f, -78.42f)
                curveTo(984.92f, 355.48f, 835.9f, 236.99f, 687.29f, 117.66f)
                curveToRelative(-17.29f, -13.94f, -35.9f, -23.77f, -57.16f, -14.43f)
                reflectiveCurveToRelative(-24.67f, 31.58f, -24.67f, 53.11f)
                verticalLineToRelative(155.99f)
                curveToRelative(0.0f, 29.76f, -13.94f, 47.05f, -44.05f, 52.28f)
                arcToRelative(954.92f, 954.92f, 0.0f, false, false, -94.66f, 20.91f)
                curveTo(245.79f, 447.0f, 118.52f, 635.34f, 105.62f, 848.77f)
                close()
            }
        }
        .build()
        return _share!!
    }

private var _share: ImageVector? = null
