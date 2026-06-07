package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.a10miaomiao.bilimiao.compose.common.preference.Preference

@Composable
actual fun GlidePreference(
    modifier: Modifier,
) {
    Preference(
        modifier = modifier,
        title = {
            Text("图片缓存")
        },
        summary = {
            Text("桌面版不支持此功能")
        },
    )
}
