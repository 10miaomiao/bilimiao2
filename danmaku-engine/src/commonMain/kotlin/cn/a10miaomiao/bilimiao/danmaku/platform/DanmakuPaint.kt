package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 弹幕画笔抽象，对应 Android TextPaint
 */
interface DanmakuPaint {
    var textSize: Float
    var color: Int
    var alpha: Int
    var strokeWidth: Float
    var isAntiAlias: Boolean
    var isFakeBoldText: Boolean
    var style: PaintStyle

    fun measureText(text: String): Float
    fun measureText(text: String, start: Int, end: Int): Float
    fun getFontMetrics(): FontMetrics
    fun setTypeface(typeface: DanmakuTypeface?)
    fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int)
    fun clearShadowLayer()
    fun copy(): DanmakuPaint
}

enum class PaintStyle {
    FILL, STROKE, FILL_AND_STROKE
}

data class FontMetrics(
    val ascent: Float,
    val descent: Float,
    val leading: Float,
    val top: Float,
    val bottom: Float
) {
    val height: Float get() = bottom - top
}
