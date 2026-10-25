package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
actual fun MiaoAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: Painter?,
    error: Painter?,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = error,
    )
}
