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

public val CommonGroup.Delete: ImageVector
    get() {
        if (_delete != null) {
            return _delete!!
        }
        _delete = Builder(name = "Delete", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF272636)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(975.0f, 109.1f)
                lineTo(647.8f, 109.1f)
                curveToRelative(0.0f, -2.2f, 2.2f, -4.5f, 2.2f, -6.7f)
                curveToRelative(0.0f, -55.6f, -46.7f, -102.4f, -102.4f, -102.4f)
                lineToRelative(-66.8f, 0.0f)
                curveTo(423.0f, -2.1f, 378.4f, 44.6f, 378.4f, 100.2f)
                curveToRelative(0.0f, 2.2f, 0.0f, 4.5f, 2.2f, 6.7f)
                lineTo(49.0f, 106.9f)
                curveToRelative(-22.3f, 0.0f, -40.1f, 17.8f, -40.1f, 40.1f)
                reflectiveCurveToRelative(17.8f, 40.1f, 40.1f, 40.1f)
                lineToRelative(77.9f, 0.0f)
                lineToRelative(0.0f, 636.6f)
                curveToRelative(0.0f, 111.3f, 91.3f, 200.3f, 200.3f, 200.3f)
                lineToRelative(389.5f, 0.0f)
                curveToRelative(111.3f, 0.0f, 200.3f, -91.3f, 200.3f, -200.3f)
                lineTo(917.1f, 189.3f)
                lineToRelative(60.1f, 0.0f)
                curveToRelative(22.3f, 0.0f, 40.1f, -17.8f, 40.1f, -40.1f)
                reflectiveCurveTo(997.3f, 109.1f, 975.0f, 109.1f)
                close()
                moveTo(458.6f, 100.2f)
                curveToRelative(0.0f, -11.1f, 11.1f, -22.3f, 22.3f, -22.3f)
                lineToRelative(66.8f, 0.0f)
                curveToRelative(11.1f, 0.0f, 22.3f, 11.1f, 22.3f, 22.3f)
                curveToRelative(0.0f, 2.2f, 0.0f, 4.5f, 2.2f, 6.7f)
                lineToRelative(-113.5f, 0.0f)
                curveTo(456.4f, 106.9f, 458.6f, 102.5f, 458.6f, 100.2f)
                close()
                moveTo(837.0f, 825.9f)
                curveToRelative(0.0f, 66.8f, -53.4f, 120.2f, -120.2f, 120.2f)
                lineTo(327.2f, 946.1f)
                curveToRelative(-66.8f, 0.0f, -120.2f, -53.4f, -120.2f, -120.2f)
                lineTo(207.0f, 189.3f)
                lineToRelative(629.9f, 0.0f)
                lineTo(837.0f, 825.9f)
                close()
                moveTo(411.8f, 756.9f)
                curveToRelative(22.3f, 0.0f, 40.1f, -17.8f, 40.1f, -40.1f)
                lineToRelative(0.0f, -311.6f)
                curveToRelative(0.0f, -22.3f, -17.8f, -40.1f, -40.1f, -40.1f)
                reflectiveCurveToRelative(-40.1f, 17.8f, -40.1f, 40.1f)
                lineToRelative(0.0f, 311.6f)
                curveTo(371.8f, 739.1f, 389.6f, 756.9f, 411.8f, 756.9f)
                close()
                moveTo(632.2f, 756.9f)
                curveToRelative(22.3f, 0.0f, 40.1f, -17.8f, 40.1f, -40.1f)
                lineToRelative(0.0f, -311.6f)
                curveToRelative(0.0f, -22.3f, -17.8f, -40.1f, -40.1f, -40.1f)
                curveToRelative(-22.3f, 0.0f, -40.1f, 17.8f, -40.1f, 40.1f)
                lineToRelative(0.0f, 311.6f)
                curveTo(592.1f, 739.1f, 609.9f, 756.9f, 632.2f, 756.9f)
                close()
            }
        }
        .build()
        return _delete!!
    }

private var _delete: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Delete, contentDescription = "")
    }
}
