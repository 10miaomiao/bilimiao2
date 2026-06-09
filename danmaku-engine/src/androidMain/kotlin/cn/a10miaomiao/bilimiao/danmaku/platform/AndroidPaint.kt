package cn.a10miaomiao.bilimiao.danmaku.platform

import android.graphics.Paint
import android.graphics.Typeface

class AndroidPaint(val paint: Paint = Paint()) : DanmakuPaint {

    override var textSize: Float
        get() = paint.textSize
        set(value) { paint.textSize = value }

    override var color: Int
        get() = paint.color
        set(value) { paint.color = value }

    override var alpha: Int
        get() = paint.alpha
        set(value) { paint.alpha = value }

    override var strokeWidth: Float
        get() = paint.strokeWidth
        set(value) { paint.strokeWidth = value }

    override var isAntiAlias: Boolean
        get() = paint.isAntiAlias
        set(value) { paint.isAntiAlias = value }

    override var isFakeBoldText: Boolean
        get() = paint.isFakeBoldText
        set(value) { paint.isFakeBoldText = value }

    override var style: PaintStyle
        get() = when (paint.style) {
            Paint.Style.FILL -> PaintStyle.FILL
            Paint.Style.STROKE -> PaintStyle.STROKE
            Paint.Style.FILL_AND_STROKE -> PaintStyle.FILL_AND_STROKE
        }
        set(value) {
            paint.style = when (value) {
                PaintStyle.FILL -> Paint.Style.FILL
                PaintStyle.STROKE -> Paint.Style.STROKE
                PaintStyle.FILL_AND_STROKE -> Paint.Style.FILL_AND_STROKE
            }
        }

    override fun measureText(text: String): Float = paint.measureText(text)
    override fun measureText(text: String, start: Int, end: Int): Float = paint.measureText(text, start, end)

    override fun getFontMetrics(): FontMetrics {
        val fm = paint.fontMetrics
        return FontMetrics(
            ascent = fm.ascent,
            descent = fm.descent,
            leading = fm.leading,
            top = fm.top,
            bottom = fm.bottom
        )
    }

    override fun setTypeface(typeface: DanmakuTypeface?) {
        paint.typeface = (typeface as? AndroidTypeface)?.typeface
    }

    override fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {
        paint.setShadowLayer(radius, dx, dy, color)
    }

    override fun clearShadowLayer() {
        paint.clearShadowLayer()
    }

    override fun copy(): DanmakuPaint {
        return AndroidPaint(Paint(paint))
    }
}
