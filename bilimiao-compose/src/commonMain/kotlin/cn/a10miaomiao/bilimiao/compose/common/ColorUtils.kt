package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.ui.graphics.Color

fun String.toColorInt(): Int {
    val colorStr = if (startsWith("#")) this else "#$this"
    val hex = colorStr.substring(1)
    return when (hex.length) {
        6 -> {
            val rgb = hex.toLong(16)
            (0xFF000000 or rgb).toInt()
        }
        8 -> {
            hex.toLong(16).toInt()
        }
        else -> throw IllegalArgumentException("Unknown color: $this")
    }
}

object ColorUtils {

    fun parse(
        colorString: String
    ): Color {
        return Color(colorString.toColorInt())
    }

}
