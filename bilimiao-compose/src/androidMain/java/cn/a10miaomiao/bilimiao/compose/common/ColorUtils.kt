package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {

    fun parse(
        colorString: String
    ): Color {
        return Color(colorString.toColorInt())
    }

}