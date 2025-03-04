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

public val CommonGroup.Bilicoin: ImageVector
    get() {
        if (_bilicoin != null) {
            return _bilicoin!!
        }
        _bilicoin = Builder(name = "Bilicoin", defaultWidth = 28.0.dp, defaultHeight = 28.0.dp,
                viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(14.045f, 25.545f)
                curveTo(7.694f, 25.545f, 2.545f, 20.397f, 2.545f, 14.045f)
                curveTo(2.545f, 7.694f, 7.694f, 2.545f, 14.045f, 2.545f)
                curveTo(20.396f, 2.545f, 25.545f, 7.694f, 25.545f, 14.045f)
                curveTo(25.545f, 17.095f, 24.333f, 20.021f, 22.177f, 22.177f)
                curveTo(20.02f, 24.334f, 17.095f, 25.545f, 14.045f, 25.545f)
                close()
                moveTo(9.662f, 6.816f)
                horizontalLineTo(18.276f)
                curveTo(18.825f, 6.816f, 19.27f, 7.222f, 19.27f, 7.722f)
                curveTo(19.27f, 8.222f, 18.825f, 8.628f, 18.276f, 8.628f)
                horizontalLineTo(14.95f)
                verticalLineTo(10.29f)
                curveTo(17.989f, 10.444f, 20.377f, 12.949f, 20.385f, 15.992f)
                verticalLineTo(17.199f)
                curveTo(20.385f, 17.7f, 19.98f, 18.105f, 19.48f, 18.105f)
                curveTo(18.979f, 18.105f, 18.574f, 17.7f, 18.574f, 17.199f)
                verticalLineTo(15.992f)
                curveTo(18.567f, 13.948f, 16.988f, 12.253f, 14.95f, 12.102f)
                verticalLineTo(20.557f)
                curveTo(14.95f, 21.058f, 14.544f, 21.463f, 14.044f, 21.463f)
                curveTo(13.544f, 21.463f, 13.138f, 21.058f, 13.138f, 20.557f)
                verticalLineTo(12.102f)
                curveTo(11.1f, 12.253f, 9.521f, 13.948f, 9.514f, 15.992f)
                verticalLineTo(17.199f)
                curveTo(9.514f, 17.7f, 9.109f, 18.105f, 8.609f, 18.105f)
                curveTo(8.108f, 18.105f, 7.703f, 17.7f, 7.703f, 17.199f)
                verticalLineTo(15.992f)
                curveTo(7.712f, 12.949f, 10.099f, 10.444f, 13.138f, 10.29f)
                verticalLineTo(8.628f)
                horizontalLineTo(9.662f)
                curveTo(9.113f, 8.628f, 8.668f, 8.222f, 8.668f, 7.722f)
                curveTo(8.668f, 7.222f, 9.113f, 6.816f, 9.662f, 6.816f)
                close()
            }
        }
        .build()
        return _bilicoin!!
    }

private var _bilicoin: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Bilicoin, contentDescription = "")
    }
}
