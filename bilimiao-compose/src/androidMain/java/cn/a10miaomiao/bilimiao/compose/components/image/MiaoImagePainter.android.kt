package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
actual fun rememberMiaoImagePainter(
    model: Any?,
    onSuccess: () -> Unit,
): Painter? = rememberCoilImagePainter(model, onSuccess)
