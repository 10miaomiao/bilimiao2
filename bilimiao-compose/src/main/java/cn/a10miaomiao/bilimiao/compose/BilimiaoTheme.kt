package cn.a10miaomiao.bilimiao.compose

import android.content.Context
import android.util.TypedValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import org.kodein.di.DI

fun getThemeColor(context: Context): Color {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    return Color(context.resources.getColor(typedValue.resourceId))
}

fun isLightTheme(context: Context): Boolean {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(R.attr.isLightTheme, typedValue, true)
    return context.resources.getBoolean(typedValue.resourceId)
}

@Composable
fun BilimiaoTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themeColor = remember { getThemeColor(context) }
    val isLightTheme = remember { isLightTheme(context) }
    MaterialTheme(
        colorScheme = if (isLightTheme) {
            lightColorScheme(
                primary = themeColor,
                secondary = themeColor,
                tertiary = themeColor,
                secondaryContainer = themeColor.copy(alpha = 0.2f),
                tertiaryContainer = themeColor.copy(alpha = 0.2f),
            )
        } else {
            darkColorScheme(
                primary = themeColor,
                secondary = themeColor,
                tertiary = themeColor,
                secondaryContainer = themeColor.copy(alpha = 0.2f),
                tertiaryContainer = themeColor.copy(alpha = 0.2f),
            )
        },
        content = content,
    )
}