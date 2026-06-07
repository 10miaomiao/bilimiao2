package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Size

@Composable
actual fun rememberQrCodePainter(data: String): Painter {
    return object : Painter() {
        override val intrinsicSize: Size = Size(200f, 200f)
        override fun DrawScope.onDraw() {
            drawRect(Color.LightGray, size = size)
        }
    }
}
