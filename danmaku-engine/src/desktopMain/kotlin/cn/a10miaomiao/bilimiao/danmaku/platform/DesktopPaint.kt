package cn.a10miaomiao.bilimiao.danmaku.platform

import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * Desktop 端基于 AWT Font 的画笔实现
 */
class DesktopPaint : DanmakuPaint {

    var font: Font = Font("Default", Font.PLAIN, 16)
        private set

    private var _textSize: Float = 16f
    private var _color: Int = 0xFF000000.toInt()
    private var _alpha: Int = 255
    private var _strokeWidth: Float = 0f
    private var _isAntiAlias: Boolean = true
    private var _isFakeBoldText: Boolean = false
    private var _style: PaintStyle = PaintStyle.FILL
    private var _shadowRadius: Float = 0f
    private var _shadowDx: Float = 0f
    private var _shadowDy: Float = 0f
    private var _shadowColor: Int = 0
    private var _typeface: DanmakuTypeface? = null

    private val frc = FontRenderContext(AffineTransform(), true, true)

    override var textSize: Float
        get() = _textSize
        set(value) {
            _textSize = value
            updateFont()
        }

    override var color: Int
        get() = _color
        set(value) { _color = value }

    override var alpha: Int
        get() = _alpha
        set(value) { _alpha = value }

    override var strokeWidth: Float
        get() = _strokeWidth
        set(value) { _strokeWidth = value }

    override var isAntiAlias: Boolean
        get() = _isAntiAlias
        set(value) { _isAntiAlias = value }

    override var isFakeBoldText: Boolean
        get() = _isFakeBoldText
        set(value) {
            _isFakeBoldText = value
            updateFont()
        }

    override var style: PaintStyle
        get() = _style
        set(value) { _style = value }

    private fun updateFont() {
        var style = Font.PLAIN
        if (_isFakeBoldText) style = style or Font.BOLD
        val baseFont = (_typeface as? DesktopTypeface)?.font ?: Font("Default", Font.PLAIN, 1)
        font = baseFont.deriveFont(style, _textSize)
    }

    override fun measureText(text: String): Float {
        return font.getStringBounds(text, frc).width.toFloat()
    }

    override fun measureText(text: String, start: Int, end: Int): Float {
        return font.getStringBounds(text, start, end, frc).width.toFloat()
    }

    override fun getFontMetrics(): FontMetrics {
        val fm = font.getLineMetrics("Ag", frc)
        return FontMetrics(
            ascent = -fm.ascent, // AWT ascent is positive, Android is negative
            descent = fm.descent,
            leading = fm.leading,
            top = -fm.ascent,
            bottom = fm.descent
        )
    }

    override fun setTypeface(typeface: DanmakuTypeface?) {
        _typeface = typeface
        updateFont()
    }

    override fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {
        _shadowRadius = radius
        _shadowDx = dx
        _shadowDy = dy
        _shadowColor = color
    }

    override fun clearShadowLayer() {
        _shadowRadius = 0f
        _shadowDx = 0f
        _shadowDy = 0f
        _shadowColor = 0
    }

    override fun copy(): DanmakuPaint {
        return DesktopPaint().also {
            it._textSize = this._textSize
            it._color = this._color
            it._alpha = this._alpha
            it._strokeWidth = this._strokeWidth
            it._isAntiAlias = this._isAntiAlias
            it._isFakeBoldText = this._isFakeBoldText
            it._style = this._style
            it._shadowRadius = this._shadowRadius
            it._shadowDx = this._shadowDx
            it._shadowDy = this._shadowDy
            it._shadowColor = this._shadowColor
            it._typeface = this._typeface
            it.updateFont()
        }
    }

    fun applyToGraphics(g2d: Graphics2D) {
        g2d.font = font
        val r = (_color shr 16) and 0xFF
        val g = (_color shr 8) and 0xFF
        val b = _color and 0xFF
        g2d.color = java.awt.Color(r, g, b, _alpha)
        g2d.stroke = java.awt.BasicStroke(_strokeWidth)
    }
}
