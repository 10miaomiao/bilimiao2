package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
actual fun rememberQrCodePainter(data: String): Painter {
    return io.github.alexzhirkevich.qrose.rememberQrCodePainter(data)
}
