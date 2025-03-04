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

public val CommonGroup.Like: ImageVector
    get() {
        if (_like != null) {
            return _like!!
        }
        _like = Builder(name = "Like", defaultWidth = 210.54688.dp, defaultHeight = 200.0.dp,
                viewportWidth = 1078.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(658.25f, 1024.0f)
                curveTo(418.61f, 1024.0f, 36.57f, 1024.0f, 36.57f, 1024.0f)
                curveTo(12.18f, 1024.0f, 0.0f, 1007.51f, 0.0f, 987.43f)
                lineTo(0.0f, 438.89f)
                curveTo(0.0f, 418.82f, 17.08f, 402.32f, 36.57f, 402.32f)
                lineTo(255.98f, 402.32f)
                curveTo(275.84f, 402.32f, 311.39f, 409.27f, 323.89f, 422.98f)
                curveTo(366.39f, 387.84f, 478.95f, 278.39f, 478.95f, 105.38f)
                curveTo(478.95f, 1.71f, 581.56f, 1.16f, 581.56f, 1.16f)
                curveTo(581.56f, 1.16f, 767.37f, -30.88f, 767.37f, 244.31f)
                curveTo(767.37f, 304.03f, 731.38f, 402.32f, 731.38f, 402.32f)
                lineTo(841.09f, 402.32f)
                curveTo(841.09f, 402.32f, 1150.8f, 348.71f, 1060.51f, 694.88f)
                curveTo(978.99f, 1007.47f, 899.31f, 1024.0f, 658.25f, 1024.0f)
                lineTo(658.25f, 1024.0f)
                close()
                moveTo(73.14f, 475.46f)
                lineTo(73.14f, 950.86f)
                lineTo(219.42f, 950.86f)
                lineTo(255.98f, 950.86f)
                lineTo(255.98f, 475.46f)
                lineTo(255.98f, 475.46f)
                lineTo(73.14f, 475.46f)
                close()
                moveTo(841.09f, 475.46f)
                lineTo(585.11f, 475.46f)
                curveTo(585.11f, 475.46f, 698.95f, 367.51f, 698.95f, 226.94f)
                curveTo(698.95f, 86.37f, 637.69f, 70.64f, 598.67f, 70.64f)
                curveTo(559.66f, 70.64f, 547.37f, 83.11f, 547.37f, 105.38f)
                curveTo(547.37f, 365.32f, 329.12f, 512.03f, 329.12f, 512.03f)
                lineTo(329.12f, 950.86f)
                curveTo(329.12f, 950.86f, 473.83f, 950.86f, 658.25f, 950.86f)
                curveTo(842.66f, 950.86f, 920.78f, 945.71f, 987.37f, 694.88f)
                curveTo(1060.58f, 419.22f, 841.09f, 475.46f, 841.09f, 475.46f)
                lineTo(841.09f, 475.46f)
                close()
            }
        }
        .build()
        return _like!!
    }

private var _like: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CommonGroup.Like, contentDescription = "")
    }
}
