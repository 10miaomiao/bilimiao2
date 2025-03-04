package cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.CommonGroup
import kotlin.Unit

public val CommonGroup.Reply: ImageVector
    get() {
        if (_reply != null) {
            return _reply!!
        }
        _reply = Builder(name = "Reply", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(345.9f, 1023.9f)
                lineTo(151.8f, 1023.9f)
                curveToRelative(-23.1f, 0.0f, -41.8f, -7.7f, -52.9f, -28.8f)
                reflectiveCurveToRelative(-6.0f, -41.4f, 7.0f, -59.9f)
                curveToRelative(11.9f, -17.1f, 24.4f, -34.0f, 37.1f, -50.3f)
                curveToRelative(6.2f, -7.9f, 5.1f, -12.7f, -1.5f, -19.7f)
                curveTo(64.0f, 784.0f, 17.4f, 687.4f, 4.1f, 576.4f)
                curveToRelative(-17.7f, -149.4f, 21.9f, -283.1f, 120.2f, -397.9f)
                curveToRelative(85.3f, -99.5f, 194.0f, -158.2f, 323.8f, -174.4f)
                curveToRelative(270.2f, -33.7f, 512.6f, 145.0f, 564.5f, 401.8f)
                curveTo(1051.5f, 599.7f, 994.6f, 765.5f, 847.5f, 898.3f)
                curveToRelative(-76.9f, 69.6f, -168.8f, 108.3f, -271.3f, 122.1f)
                arcToRelative(338.7f, 338.7f, 0.0f, false, true, -48.0f, 3.5f)
                curveToRelative(-60.8f, -0.3f, -121.6f, -0.0f, -182.4f, 0.0f)
                close()
                moveTo(382.5f, 942.5f)
                horizontalLineToRelative(96.4f)
                curveToRelative(40.8f, 0.0f, 81.4f, 0.0f, 121.9f, -8.6f)
                curveToRelative(215.3f, -45.3f, 373.1f, -251.3f, 337.1f, -487.4f)
                curveTo(905.2f, 232.4f, 719.5f, 82.6f, 518.4f, 81.9f)
                curveToRelative(-77.3f, -0.3f, -150.6f, 15.4f, -216.8f, 55.1f)
                curveTo(152.8f, 226.3f, 77.9f, 357.8f, 82.6f, 532.4f)
                curveToRelative(2.3f, 83.9f, 29.9f, 159.7f, 77.7f, 228.3f)
                curveToRelative(24.8f, 35.6f, 55.3f, 65.8f, 88.6f, 93.2f)
                curveToRelative(8.4f, 7.0f, 9.9f, 12.0f, 2.5f, 21.1f)
                curveToRelative(-13.9f, 17.2f, -26.6f, 35.4f, -39.5f, 53.4f)
                curveToRelative(-2.6f, 3.5f, -8.7f, 7.0f, -5.8f, 11.9f)
                curveToRelative(2.6f, 4.3f, 8.8f, 2.1f, 13.5f, 2.1f)
                quadToRelative(81.4f, 0.1f, 162.8f, 0.1f)
                close()
            }
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(567.7f, 511.6f)
                arcToRelative(55.7f, 55.7f, 0.0f, true, true, -55.7f, -54.9f)
                arcToRelative(54.4f, 54.4f, 0.0f, false, true, 55.7f, 54.9f)
                close()
                moveTo(266.2f, 511.3f)
                arcToRelative(54.0f, 54.0f, 0.0f, false, true, 55.7f, -54.7f)
                arcToRelative(55.7f, 55.7f, 0.0f, true, true, -0.5f, 111.2f)
                curveToRelative(-32.0f, -0.1f, -55.4f, -24.1f, -55.2f, -56.5f)
                close()
                moveTo(758.0f, 513.2f)
                arcToRelative(54.0f, 54.0f, 0.0f, false, true, -55.7f, 54.6f)
                arcToRelative(55.7f, 55.7f, 0.0f, true, true, 0.7f, -111.2f)
                curveToRelative(32.0f, 0.1f, 55.3f, 24.1f, 55.0f, 56.6f)
                close()
            }
        }
        .build()
        return _reply!!
    }

private var _reply: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Reply, contentDescription = "")
    }
}
