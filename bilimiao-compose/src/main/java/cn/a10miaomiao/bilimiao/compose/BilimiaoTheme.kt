package cn.a10miaomiao.bilimiao.compose

import android.content.Context
import android.util.TypedValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.kodein.di.DI

fun getThemeColor(context: Context): Color {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    return Color(context.resources.getColor(typedValue.resourceId))
}

@Composable
fun BilimiaoTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themeColor = remember { getThemeColor(context) }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = themeColor,
            secondary = themeColor,
            tertiary = themeColor,
        ),
        content = content,
    )
}