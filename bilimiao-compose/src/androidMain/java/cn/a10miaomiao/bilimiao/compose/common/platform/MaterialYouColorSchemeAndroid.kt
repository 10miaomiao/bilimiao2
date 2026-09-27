package cn.a10miaomiao.bilimiao.compose.common.platform

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberMaterialYouColorScheme(isDarkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return remember(context, isDarkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else if (isDarkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    }
}
