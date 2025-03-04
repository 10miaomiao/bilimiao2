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

public val CommonGroup.Playnum: ImageVector
    get() {
        if (_playnum != null) {
            return _playnum!!
        }
        _playnum = Builder(name = "Playnum", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(799.7f, 128.4f)
                lineTo(224.3f, 128.4f)
                curveToRelative(-89.5f, 0.0f, -159.9f, 70.3f, -159.9f, 159.9f)
                verticalLineToRelative(447.6f)
                curveToRelative(0.0f, 89.5f, 70.3f, 159.9f, 159.9f, 159.9f)
                horizontalLineToRelative(575.5f)
                curveToRelative(89.5f, 0.0f, 159.9f, -70.3f, 159.9f, -159.9f)
                lineTo(959.7f, 288.2f)
                curveToRelative(-0.1f, -89.5f, -70.5f, -159.8f, -160.0f, -159.8f)
                close()
                moveTo(895.6f, 735.8f)
                curveToRelative(0.0f, 54.4f, -41.6f, 95.9f, -95.9f, 95.9f)
                lineTo(224.3f, 831.7f)
                curveToRelative(-54.3f, 0.0f, -95.9f, -41.6f, -95.9f, -95.9f)
                lineTo(128.4f, 288.2f)
                curveToRelative(0.0f, -54.3f, 41.6f, -95.9f, 95.9f, -95.9f)
                horizontalLineToRelative(575.5f)
                curveToRelative(54.3f, 0.0f, 95.9f, 41.6f, 95.9f, 95.9f)
                verticalLineToRelative(447.6f)
                close()
                moveTo(895.6f, 735.8f)
                moveTo(684.6f, 483.2f)
                lineTo(428.9f, 371.3f)
                curveToRelative(-22.4f, -9.6f, -44.8f, 6.4f, -44.8f, 28.8f)
                verticalLineToRelative(223.8f)
                curveToRelative(0.0f, 22.4f, 22.4f, 38.4f, 44.8f, 28.8f)
                lineToRelative(255.8f, -111.9f)
                curveToRelative(25.5f, -9.6f, 25.5f, -48.0f, -0.1f, -57.6f)
                close()
                moveTo(684.6f, 483.2f)
            }
        }
        .build()
        return _playnum!!
    }

private var _playnum: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Playnum, contentDescription = "")
    }
}
