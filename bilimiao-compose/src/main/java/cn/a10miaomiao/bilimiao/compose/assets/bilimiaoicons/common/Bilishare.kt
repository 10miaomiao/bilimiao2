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

public val CommonGroup.Bilishare: ImageVector
    get() {
        if (_bilishare != null) {
            return _bilishare!!
        }
        _bilishare = Builder(name = "Bilishare", defaultWidth = 28.0.dp, defaultHeight = 28.0.dp,
                viewportWidth = 28.0f, viewportHeight = 28.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(12.606f, 10.333f)
                verticalLineTo(5.444f)
                curveTo(12.606f, 4.646f, 13.272f, 4.0f, 14.093f, 4.0f)
                curveTo(14.442f, 4.0f, 14.78f, 4.119f, 15.048f, 4.336f)
                lineTo(25.385f, 12.722f)
                curveTo(26.112f, 13.312f, 26.209f, 14.363f, 25.601f, 15.068f)
                curveTo(25.535f, 15.144f, 25.463f, 15.214f, 25.385f, 15.278f)
                lineTo(15.048f, 23.664f)
                curveTo(14.417f, 24.175f, 13.479f, 24.094f, 12.952f, 23.482f)
                curveTo(12.728f, 23.223f, 12.606f, 22.895f, 12.606f, 22.556f)
                verticalLineTo(18.053f)
                curveTo(7.595f, 18.053f, 5.371f, 19.912f, 2.572f, 23.525f)
                curveTo(2.476f, 23.649f, 2.0f, 23.777f, 2.0f, 23.212f)
                curveTo(2.0f, 16.216f, 3.901f, 10.333f, 12.606f, 10.333f)
                close()
            }
        }
        .build()
        return _bilishare!!
    }

private var _bilishare: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Bilishare, contentDescription = "")
    }
}
