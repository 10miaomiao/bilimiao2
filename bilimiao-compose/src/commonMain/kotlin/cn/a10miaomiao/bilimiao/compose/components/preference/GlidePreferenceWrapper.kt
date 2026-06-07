package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

inline fun LazyListScope.glidePreference(
    key: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    item(key = key, contentType = "GlidePreference") {
        GlidePreference(modifier = modifier)
    }
}

@Composable
expect fun GlidePreference(
    modifier: Modifier = Modifier,
)
