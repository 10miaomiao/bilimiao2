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

public val CommonGroup.Menuunfold: ImageVector
    get() {
        if (_menuunfold != null) {
            return _menuunfold!!
        }
        _menuunfold = Builder(name = "Menuunfold", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(170.7f, 149.3f)
                horizontalLineToRelative(682.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, 85.3f)
                lineTo(170.7f, 234.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, true, true, 0.0f, -85.3f)
                close()
                moveTo(170.7f, 789.3f)
                horizontalLineToRelative(682.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, 85.3f)
                lineTo(170.7f, 874.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, -85.3f)
                close()
                moveTo(426.7f, 576.0f)
                horizontalLineToRelative(426.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, 85.3f)
                lineTo(426.7f, 661.3f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, -85.3f)
                close()
                moveTo(426.7f, 362.7f)
                horizontalLineToRelative(426.7f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, 85.3f)
                lineTo(426.7f, 448.0f)
                arcToRelative(42.7f, 42.7f, 0.0f, false, true, 0.0f, -85.3f)
                close()
                moveTo(137.0f, 535.0f)
                arcToRelative(34.1f, 34.1f, 0.0f, false, true, 0.0f, -46.1f)
                lineToRelative(109.6f, -116.7f)
                arcToRelative(29.6f, 29.6f, 0.0f, false, true, 21.6f, -9.6f)
                curveToRelative(16.9f, 0.0f, 30.5f, 14.6f, 30.5f, 32.6f)
                lineTo(298.7f, 628.8f)
                arcToRelative(33.7f, 33.7f, 0.0f, false, true, -9.0f, 23.0f)
                arcToRelative(29.2f, 29.2f, 0.0f, false, true, -43.2f, 0.0f)
                lineTo(137.0f, 535.0f)
                close()
            }
        }
        .build()
        return _menuunfold!!
    }

private var _menuunfold: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Menuunfold, contentDescription = "")
    }
}
