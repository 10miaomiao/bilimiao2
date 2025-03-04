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

public val CommonGroup.Bililike: ImageVector
    get() {
        if (_bililike != null) {
            return _bililike!!
        }
        _bililike = Builder(name = "Bililike", defaultWidth = 36.0.dp, defaultHeight = 36.0.dp,
                viewportWidth = 36.0f, viewportHeight = 36.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(9.772f, 30.857f)
                verticalLineTo(11.747f)
                horizontalLineTo(7.546f)
                curveTo(5.509f, 11.747f, 3.857f, 13.393f, 3.857f, 15.425f)
                verticalLineTo(27.179f)
                curveTo(3.857f, 29.211f, 5.509f, 30.857f, 7.546f, 30.857f)
                horizontalLineTo(9.772f)
                close()
                moveTo(11.99f, 30.857f)
                verticalLineTo(11.705f)
                curveTo(14.99f, 10.627f, 16.694f, 7.885f, 17.105f, 3.336f)
                curveTo(17.267f, 1.555f, 18.963f, 0.814f, 20.58f, 1.595f)
                curveTo(22.185f, 2.37f, 23.243f, 4.326f, 23.243f, 6.939f)
                curveTo(23.243f, 8.503f, 23.048f, 10.105f, 22.658f, 11.747f)
                horizontalLineTo(29.732f)
                curveTo(31.774f, 11.747f, 33.429f, 13.402f, 33.429f, 15.443f)
                curveTo(33.429f, 15.742f, 33.393f, 16.039f, 33.321f, 16.328f)
                lineTo(30.988f, 25.796f)
                curveTo(30.256f, 28.768f, 27.589f, 30.857f, 24.528f, 30.857f)
                horizontalLineTo(11.991f)
                horizontalLineTo(11.99f)
                close()
            }
        }
        .build()
        return _bililike!!
    }

private var _bililike: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Bililike, contentDescription = "")
    }
}
