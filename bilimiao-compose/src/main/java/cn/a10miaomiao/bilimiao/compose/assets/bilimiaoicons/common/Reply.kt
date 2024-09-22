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
                moveTo(843.13f, 684.33f)
                arcToRelative(32.5f, 32.5f, 0.0f, false, true, 0.0f, -65.0f)
                arcToRelative(33.49f, 33.49f, 0.0f, false, false, 33.46f, -33.45f)
                verticalLineTo(268.34f)
                arcToRelative(33.5f, 33.5f, 0.0f, false, false, -33.46f, -33.45f)
                horizontalLineTo(318.5f)
                arcToRelative(33.49f, 33.49f, 0.0f, false, false, -33.45f, 33.45f)
                arcToRelative(32.5f, 32.5f, 0.0f, false, true, -65.0f, 0.0f)
                arcToRelative(98.56f, 98.56f, 0.0f, false, true, 98.45f, -98.45f)
                horizontalLineToRelative(524.63f)
                arcToRelative(98.57f, 98.57f, 0.0f, false, true, 98.46f, 98.45f)
                verticalLineToRelative(317.54f)
                arcToRelative(98.56f, 98.56f, 0.0f, false, true, -98.46f, 98.45f)
                close()
                moveTo(321.4f, 901.68f)
                arcToRelative(59.12f, 59.12f, 0.0f, false, true, -59.0f, -58.68f)
                lineToRelative(-0.22f, -33.72f)
                horizontalLineToRelative(-61.36f)
                arcTo(106.0f, 106.0f, 0.0f, false, true, 95.0f, 703.44f)
                verticalLineTo(387.11f)
                arcToRelative(106.0f, 106.0f, 0.0f, false, true, 105.82f, -105.84f)
                horizontalLineToRelative(532.47f)
                arcToRelative(106.0f, 106.0f, 0.0f, false, true, 105.83f, 105.84f)
                verticalLineToRelative(316.33f)
                arcToRelative(106.0f, 106.0f, 0.0f, false, true, -105.83f, 105.84f)
                horizontalLineToRelative(-218.0f)
                lineToRelative(-166.88f, 85.81f)
                arcToRelative(59.0f, 59.0f, 0.0f, false, true, -27.01f, 6.59f)
                close()
                moveTo(200.82f, 346.27f)
                arcTo(40.89f, 40.89f, 0.0f, false, false, 160.0f, 387.11f)
                verticalLineToRelative(316.33f)
                arcToRelative(40.89f, 40.89f, 0.0f, false, false, 40.84f, 40.84f)
                horizontalLineToRelative(80.72f)
                arcToRelative(45.66f, 45.66f, 0.0f, false, true, 45.54f, 45.25f)
                lineToRelative(0.28f, 43.29f)
                lineToRelative(162.38f, -83.5f)
                arcToRelative(45.75f, 45.75f, 0.0f, false, true, 20.83f, -5.0f)
                horizontalLineToRelative(222.7f)
                arcToRelative(40.88f, 40.88f, 0.0f, false, false, 40.83f, -40.84f)
                verticalLineTo(387.11f)
                arcToRelative(40.88f, 40.88f, 0.0f, false, false, -40.83f, -40.84f)
                close()
                moveTo(301.55f, 547.87f)
                moveToRelative(-45.72f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, 91.44f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, -91.44f, 0.0f)
                close()
                moveTo(467.34f, 547.87f)
                moveToRelative(-45.72f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, 91.44f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, -91.44f, 0.0f)
                close()
                moveTo(633.13f, 547.87f)
                moveToRelative(-45.72f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, 91.44f, 0.0f)
                arcToRelative(45.72f, 45.72f, 0.0f, true, false, -91.44f, 0.0f)
                close()
            }
        }
        .build()
        return _reply!!
    }

private var _reply: ImageVector? = null
