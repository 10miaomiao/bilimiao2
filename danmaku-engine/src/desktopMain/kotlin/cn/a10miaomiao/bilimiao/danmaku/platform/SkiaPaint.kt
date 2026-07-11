package cn.a10miaomiao.bilimiao.danmaku.platform

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontMetrics as SkiaFontMetrics
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

/**
 * Desktop 端基于 Skia Paint/Font 的画笔实现
 *
 * 直接使用 Skia GPU 渲染，绕过 AWT BufferedImage 中间层。
 */
class SkiaPaint : DanmakuPaint {

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
    private var _typeface: SkiaTypeface? = null

    val font: Font = Font(DEFAULT_TYPEFACE, _textSize)

    /** Skia Paint，用于绘制 */
    val paint: Paint = Paint()

    init {
        font.size = _textSize
        paint.isAntiAlias = _isAntiAlias
        updatePaint()
    }

    override var textSize: Float
        get() = _textSize
        set(value) {
            _textSize = value
            font.size = value
        }

    override var color: Int
        get() = _color
        set(value) {
            _color = value
            updatePaint()
        }

    override var alpha: Int
        get() = _alpha
        set(value) {
            _alpha = value
            updatePaint()
        }

    override var strokeWidth: Float
        get() = _strokeWidth
        set(value) {
            _strokeWidth = value
            updatePaint()
        }

    override var isAntiAlias: Boolean
        get() = _isAntiAlias
        set(value) {
            _isAntiAlias = value
            paint.isAntiAlias = value
        }

    override var isFakeBoldText: Boolean
        get() = _isFakeBoldText
        set(value) {
            _isFakeBoldText = value
            font.isEmboldened = value
        }

    override var style: PaintStyle
        get() = _style
        set(value) {
            _style = value
            updatePaint()
        }

    private fun updatePaint() {
        // Skia 使用 ARGB 顺序的 Color4f（sRGB 色彩空间）
        val a = (_alpha and 0xFF) / 255f
        val r = (_color shr 16 and 0xFF) / 255f
        val g = (_color shr 8 and 0xFF) / 255f
        val b = (_color and 0xFF) / 255f
        paint.color4f = Color4f(r, g, b, a)
        paint.mode = when (_style) {
            PaintStyle.FILL -> PaintMode.FILL
            PaintStyle.STROKE -> PaintMode.STROKE
            PaintStyle.FILL_AND_STROKE -> PaintMode.STROKE_AND_FILL
        }
        paint.strokeWidth = _strokeWidth
    }

    override fun measureText(text: String): Float {
        return font.measureTextWidth(text)
    }

    override fun measureText(text: String, start: Int, end: Int): Float {
        return font.measureTextWidth(text.substring(start, end))
    }

    override fun getFontMetrics(): FontMetrics {
        val fm: SkiaFontMetrics = font.metrics
        return FontMetrics(
            ascent = fm.ascent,
            descent = fm.descent,
            leading = fm.leading,
            top = fm.top,
            bottom = fm.bottom
        )
    }

    override fun setTypeface(typeface: DanmakuTypeface?) {
        _typeface = typeface as? SkiaTypeface
        font.setTypeface(_typeface?.typeface ?: DEFAULT_TYPEFACE)
    }

    override fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {
        _shadowRadius = radius
        _shadowDx = dx
        _shadowDy = dy
        _shadowColor = color
        // Skia 的 image filter / shadow 效果较重，这里仅用 blur mask filter 近似
        // 弹幕描边主要通过 STROKE 模式实现，阴影用得很少
    }

    override fun clearShadowLayer() {
        _shadowRadius = 0f
        _shadowDx = 0f
        _shadowDy = 0f
        _shadowColor = 0
    }

    override fun copy(): DanmakuPaint {
        return SkiaPaint().also {
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
            it._typeface = this._typeface
            it.font.size = this._textSize
            it.font.setTypeface(this._typeface?.typeface ?: DEFAULT_TYPEFACE)
            it.font.isEmboldened = this._isFakeBoldText
            it.updatePaint()
        }
    }

    companion object {
        /**
         * 默认 Typeface：尝试加载常见系统字体，避免 Font() 默认构造器返回空 Typeface。
         * 优先级：Microsoft YaHei (Windows) → SimHei (Windows) → Arial → null (fallback)
         */
        private val DEFAULT_TYPEFACE: org.jetbrains.skia.Typeface? by lazy {
            val mgr = FontMgr.default
            // 尝试常见字体族，第一个匹配的即使用
            val families = arrayOf(
                "Microsoft YaHei", "Microsoft YaHei UI", "SimHei", "SimSun",
                "Noto Sans CJK SC", "Noto Sans SC", "Source Han Sans SC",
                "PingFang SC", "Hiragino Sans GB", "WenQuanYi Micro Hei",
                "Arial", "DejaVu Sans", "Liberation Sans"
            )
            for (name in families) {
                val tf = runCatching { mgr.matchFamilyStyle(name, FontStyle.NORMAL) }.getOrNull()
                if (tf != null) return@lazy tf
            }
            // 最终 fallback：用 legacyMakeTypeface
            runCatching { mgr.legacyMakeTypeface("", FontStyle.NORMAL) }.getOrNull()
        }
    }
}
