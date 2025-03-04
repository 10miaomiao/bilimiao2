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

public val CommonGroup.Upper: ImageVector
    get() {
        if (_upper != null) {
            return _upper!!
        }
        _upper = Builder(name = "Upper", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(800.0f, 128.0f)
                lineTo(224.0f, 128.0f)
                curveTo(134.4f, 128.0f, 64.0f, 198.4f, 64.0f, 288.0f)
                verticalLineToRelative(448.0f)
                curveToRelative(0.0f, 89.6f, 70.4f, 160.0f, 160.0f, 160.0f)
                horizontalLineToRelative(576.0f)
                curveToRelative(89.6f, 0.0f, 160.0f, -70.4f, 160.0f, -160.0f)
                lineTo(960.0f, 288.0f)
                curveToRelative(0.0f, -89.6f, -70.4f, -160.0f, -160.0f, -160.0f)
                close()
                moveTo(896.0f, 736.0f)
                curveToRelative(0.0f, 54.4f, -41.6f, 96.0f, -96.0f, 96.0f)
                lineTo(224.0f, 832.0f)
                curveToRelative(-54.4f, 0.0f, -96.0f, -41.6f, -96.0f, -96.0f)
                lineTo(128.0f, 288.0f)
                curveToRelative(0.0f, -54.4f, 41.6f, -96.0f, 96.0f, -96.0f)
                horizontalLineToRelative(576.0f)
                curveToRelative(54.4f, 0.0f, 96.0f, 41.6f, 96.0f, 96.0f)
                verticalLineToRelative(448.0f)
                close()
                moveTo(419.2f, 544.0f)
                curveToRelative(0.0f, 51.2f, -3.2f, 108.8f, -83.2f, 108.8f)
                reflectiveCurveTo(252.8f, 595.2f, 252.8f, 544.0f)
                verticalLineToRelative(-217.6f)
                lineTo(192.0f, 326.4f)
                verticalLineToRelative(243.2f)
                curveToRelative(0.0f, 96.0f, 51.2f, 140.8f, 140.8f, 140.8f)
                curveToRelative(89.6f, 0.0f, 147.2f, -48.0f, 147.2f, -144.0f)
                verticalLineToRelative(-240.0f)
                horizontalLineToRelative(-60.8f)
                lineTo(419.2f, 544.0f)
                close()
                moveTo(710.4f, 326.4f)
                horizontalLineToRelative(-156.8f)
                lineTo(553.6f, 704.0f)
                horizontalLineToRelative(60.8f)
                verticalLineToRelative(-147.2f)
                horizontalLineToRelative(96.0f)
                curveToRelative(102.4f, 0.0f, 121.6f, -67.2f, 121.6f, -115.2f)
                curveToRelative(0.0f, -44.8f, -19.2f, -115.2f, -121.6f, -115.2f)
                close()
                moveTo(707.2f, 505.6f)
                horizontalLineToRelative(-92.8f)
                lineTo(614.4f, 384.0f)
                horizontalLineToRelative(92.8f)
                curveToRelative(32.0f, 0.0f, 60.8f, 12.8f, 60.8f, 60.8f)
                curveToRelative(0.0f, 44.8f, -32.0f, 60.8f, -60.8f, 60.8f)
                close()
            }
        }
        .build()
        return _upper!!
    }

private var _upper: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Upper, contentDescription = "")
    }
}
