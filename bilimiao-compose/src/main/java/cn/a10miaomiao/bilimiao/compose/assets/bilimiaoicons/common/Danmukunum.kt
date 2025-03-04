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

public val CommonGroup.Danmukunum: ImageVector
    get() {
        if (_danmukunum != null) {
            return _danmukunum!!
        }
        _danmukunum = Builder(name = "Danmukunum", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(800.0f, 128.0f)
                lineTo(224.0f, 128.0f)
                curveToRelative(-89.6f, 0.0f, -160.0f, 70.4f, -160.0f, 160.0f)
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
                moveTo(240.0f, 384.0f)
                horizontalLineToRelative(64.0f)
                verticalLineToRelative(64.0f)
                horizontalLineToRelative(-64.0f)
                close()
                moveTo(368.0f, 384.0f)
                horizontalLineToRelative(384.0f)
                verticalLineToRelative(64.0f)
                lineTo(368.0f, 448.0f)
                close()
                moveTo(432.0f, 576.0f)
                horizontalLineToRelative(352.0f)
                verticalLineToRelative(64.0f)
                lineTo(432.0f, 640.0f)
                close()
                moveTo(304.0f, 576.0f)
                horizontalLineToRelative(64.0f)
                verticalLineToRelative(64.0f)
                horizontalLineToRelative(-64.0f)
                close()
            }
        }
        .build()
        return _danmukunum!!
    }

private var _danmukunum: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Danmukunum, contentDescription = "")
    }
}
