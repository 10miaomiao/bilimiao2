package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorLong

object ColorUtils {

    fun parse(
        colorString: String
    ): Color {
        return Color(colorString.toColorInt())
    }

}