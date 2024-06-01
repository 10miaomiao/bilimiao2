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

public val CommonGroup.Menufold: ImageVector
    get() {
        if (_menufold != null) {
            return _menufold!!
        }
        _menufold = Builder(name = "Menufold", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(170.67f, 149.33f)
                horizontalLineToRelative(682.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, 85.33f)
                lineTo(170.67f, 234.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, true, true, 0.0f, -85.33f)
                close()
                moveTo(170.67f, 789.33f)
                horizontalLineToRelative(682.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, 85.33f)
                lineTo(170.67f, 874.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, -85.33f)
                close()
                moveTo(426.67f, 576.0f)
                horizontalLineToRelative(426.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, 85.33f)
                lineTo(426.67f, 661.33f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, -85.33f)
                close()
                moveTo(426.67f, 362.67f)
                horizontalLineToRelative(426.67f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, 85.33f)
                lineTo(426.67f, 448.0f)
                arcToRelative(42.67f, 42.67f, 0.0f, false, true, 0.0f, -85.33f)
                close()
                moveTo(289.71f, 535.04f)
                lineTo(180.14f, 651.78f)
                arcToRelative(29.23f, 29.23f, 0.0f, false, true, -43.18f, 0.0f)
                arcToRelative(33.71f, 33.71f, 0.0f, false, true, -8.96f, -23.04f)
                lineTo(128.0f, 395.26f)
                curveToRelative(0.0f, -17.96f, 13.65f, -32.56f, 30.55f, -32.56f)
                curveToRelative(8.11f, 0.0f, 15.87f, 3.41f, 21.59f, 9.56f)
                lineTo(289.71f, 488.96f)
                arcToRelative(34.13f, 34.13f, 0.0f, false, true, 0.0f, 46.08f)
                close()
            }
        }
        .build()
        return _menufold!!
    }

private var _menufold: ImageVector? = null
