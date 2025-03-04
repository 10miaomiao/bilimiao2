package cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.CommonGroup
import kotlin.Unit

public val CommonGroup.Share: ImageVector
    get() {
        if (_share != null) {
            return _share!!
        }
        _share = Builder(name = "Share", defaultWidth = 246.3.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1261.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(662.2f, 1023.2f)
                arcToRelative(153.3f, 153.3f, 0.0f, false, true, -151.3f, -148.3f)
                curveToRelative(-1.3f, -26.6f, -1.3f, -53.2f, 0.0f, -79.7f)
                curveToRelative(1.0f, -23.3f, -8.8f, -31.3f, -31.2f, -27.9f)
                curveToRelative(-98.6f, 14.4f, -191.1f, 42.7f, -267.1f, 112.0f)
                arcToRelative(318.7f, 318.7f, 0.0f, false, false, -54.9f, 70.6f)
                curveToRelative(-28.4f, 46.0f, -58.1f, 61.7f, -98.6f, 51.4f)
                curveTo(21.8f, 991.7f, -0.8f, 961.9f, 0.0f, 910.2f)
                curveToRelative(2.4f, -143.7f, 36.6f, -278.8f, 121.0f, -397.8f)
                curveToRelative(86.1f, -121.6f, 204.4f, -194.8f, 347.9f, -228.3f)
                curveToRelative(31.2f, -7.0f, 45.8f, -19.1f, 42.4f, -53.0f)
                curveToRelative(-3.1f, -30.9f, -0.5f, -62.3f, -0.6f, -93.5f)
                curveToRelative(0.0f, -56.6f, 25.6f, -98.3f, 75.7f, -122.2f)
                reflectiveCurveToRelative(102.9f, -20.1f, 147.1f, 14.8f)
                quadToRelative(239.3f, 188.8f, 476.1f, 381.1f)
                curveToRelative(69.7f, 56.5f, 67.8f, 153.3f, -2.3f, 209.7f)
                quadToRelative(-226.6f, 182.4f, -453.6f, 364.3f)
                curveToRelative(-28.3f, 22.4f, -57.9f, 42.9f, -91.5f, 37.9f)
                close()
                moveTo(105.4f, 849.0f)
                curveToRelative(120.3f, -132.4f, 270.4f, -178.2f, 436.4f, -183.5f)
                curveToRelative(50.0f, -1.6f, 62.7f, 13.0f, 63.3f, 64.0f)
                curveToRelative(0.0f, 45.1f, -0.9f, 90.2f, 1.0f, 135.2f)
                arcToRelative(62.7f, 62.7f, 0.0f, false, false, 37.7f, 57.7f)
                curveToRelative(24.0f, 11.4f, 37.2f, -5.6f, 51.6f, -17.2f)
                quadToRelative(219.4f, -175.1f, 438.1f, -351.2f)
                curveToRelative(38.8f, -31.3f, 39.0f, -46.6f, -0.5f, -78.4f)
                curveTo(984.9f, 355.5f, 835.9f, 237.0f, 687.3f, 117.7f)
                curveToRelative(-17.3f, -13.9f, -35.9f, -23.8f, -57.2f, -14.4f)
                reflectiveCurveToRelative(-24.7f, 31.6f, -24.7f, 53.1f)
                verticalLineToRelative(156.0f)
                curveToRelative(0.0f, 29.8f, -13.9f, 47.0f, -44.1f, 52.3f)
                arcToRelative(954.9f, 954.9f, 0.0f, false, false, -94.7f, 20.9f)
                curveTo(245.8f, 447.0f, 118.5f, 635.3f, 105.6f, 848.8f)
                close()
            }
        }
        .build()
        return _share!!
    }

private var _share: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Share, contentDescription = "")
    }
}
