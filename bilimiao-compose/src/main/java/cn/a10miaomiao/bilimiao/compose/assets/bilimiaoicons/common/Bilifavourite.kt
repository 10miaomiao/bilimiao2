package cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
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

public val CommonGroup.Bilifavourite: ImageVector
    get() {
        if (_bilifavourite != null) {
            return _bilifavourite!!
        }
        _bilifavourite = Builder(name = "Bilifavourite", defaultWidth = 28.0.dp, defaultHeight =
                28.0.dp, viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(19.807f, 9.262f)
                curveTo(18.744f, 9.099f, 17.762f, 8.368f, 17.353f, 7.394f)
                lineTo(15.472f, 3.497f)
                curveTo(14.9f, 2.198f, 13.1f, 2.198f, 12.446f, 3.497f)
                lineTo(10.647f, 7.394f)
                curveTo(10.156f, 8.368f, 9.256f, 9.099f, 8.193f, 9.262f)
                lineTo(3.94f, 9.911f)
                curveTo(2.632f, 10.073f, 2.059f, 11.697f, 3.04f, 12.671f)
                lineTo(6.23f, 15.919f)
                curveTo(6.966f, 16.65f, 7.293f, 17.705f, 7.13f, 18.76f)
                lineTo(6.394f, 23.307f)
                curveTo(6.148f, 24.687f, 7.621f, 25.661f, 8.847f, 25.012f)
                lineTo(12.446f, 23.063f)
                curveTo(13.428f, 22.495f, 14.654f, 22.495f, 15.636f, 23.063f)
                lineTo(19.235f, 25.012f)
                curveTo(20.461f, 25.661f, 21.852f, 24.687f, 21.688f, 23.307f)
                lineTo(20.87f, 18.76f)
                curveTo(20.705f, 17.705f, 21.034f, 16.65f, 21.77f, 15.919f)
                lineTo(24.96f, 12.671f)
                curveTo(25.941f, 11.697f, 25.369f, 10.073f, 24.06f, 9.911f)
                lineTo(19.807f, 9.262f)
                close()
            }
        }
        .build()
        return _bilifavourite!!
    }

private var _bilifavourite: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Bilifavourite, contentDescription = "")
    }
}
