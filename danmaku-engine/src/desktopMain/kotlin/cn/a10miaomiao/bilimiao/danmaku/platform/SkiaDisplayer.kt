package cn.a10miaomiao.bilimiao.danmaku.platform

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuFactory
import cn.a10miaomiao.bilimiao.danmaku.model.AlphaValue
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.renderer.IRenderer
import org.jetbrains.skia.Canvas as SkiaCanvas

/**
 * Desktop 端基于 Skia Canvas 的弹幕显示器实现
 *
 * 直接渲染到 Skia Canvas（GPU 后端），完全绕过 AWT BufferedImage 中间层。
 * 每帧由 [DanmakuOverlay] 调用 [setCanvas] 注入当前 Skia Canvas，
 * 然后调用 [DanmakuEngine.drawWithSync] 进行渲染。
 *
 * 与 [DesktopDisplayer] 相比：
 * - 无 BufferedImage/IntArray/像素拷贝
 * - 无双缓冲/三缓冲交换
 * - 无 Graphics2D 创建/销毁
 * - 直接 GPU 渲染，VSync 同步
 */
class SkiaDisplayer(
    private val mContext: DanmakuContext
) : IDisplayer {

    private var _width: Int = 0
    private var _height: Int = 0
    private var _density: Float = 1f
    private var _densityDpi: Int = 96
    private var _scaledDensity: Float = 1f
    private var _slopPixel: Int = 6
    private var _strokeWidth: Float = 0f
    private var _margin: Int = 0
    private var _allMarginTop: Int = 0
    private var _transparency: Int = 255
    private var _scaleTextSizeFactor: Float = 1f
    private var _fakeBoldText: Boolean = false
    private var _typeface: DanmakuTypeface? = null

    private val _minParserDensity: Float = 1.5f

    /** 当前帧的 Skia Canvas，由外部在每帧绘制前注入 */
    private var _skiaCanvas: SkiaCanvas? = null

    /** 缓存的 DanmakuCanvas 包装，避免每帧创建 */
    private var _cachedCanvas: SkiaDanmakuCanvasWrapper? = null

    private val _paint = SkiaPaint()

    override val width: Int get() = _width
    override val height: Int get() = _height
    override val density: Float get() = maxOf(_density, _minParserDensity)
    override val densityDpi: Int get() = _densityDpi
    override val scaledDensity: Float get() = _scaledDensity
    override val slopPixel: Int get() = _slopPixel
    override val strokeWidth: Float get() = _strokeWidth
    override val isHardwareAccelerated: Boolean get() = true
    override val maximumCacheWidth: Int get() = _width
    override val maximumCacheHeight: Int get() = _height
    override val margin: Int get() = _margin
    override val allMarginTop: Int get() = _allMarginTop

    /**
     * 设置当前帧的 Skia Canvas
     *
     * 由 [DanmakuOverlay] 在 drawWithCache 的 onDraw 中调用，
     * 在调用 [DanmakuEngine.drawWithSync] 之前注入。
     */
    fun setCanvas(canvas: SkiaCanvas?) {
        _skiaCanvas = canvas
    }


    override fun draw(danmaku: BaseDanmaku): Int {
        val skiaCanvas = _skiaCanvas ?: return IRenderer.NOTHING_RENDERING


        var left = danmaku.getLeft()
        var top = danmaku.getTop()

        // 特殊弹幕坐标缩放
        if (danmaku.getType() == BaseDanmaku.TYPE_SPECIAL) {
            val factory = mContext.mDanmakuFactory
            if (factory.CURRENT_DISP_HEIGHT > 0) {
                top *= _height.toFloat() / DanmakuFactory.BILI_PLAYER_HEIGHT
                left *= _width.toFloat() / DanmakuFactory.BILI_PLAYER_WIDTH
            }
        }

        // 透明弹幕跳过
        if (danmaku.getType() == BaseDanmaku.TYPE_SPECIAL && danmaku.getAlpha() == AlphaValue.TRANSPARENT) {
            return IRenderer.NOTHING_RENDERING
        }

        // 设置画笔
        val paint = _paint
        val textSize = danmaku.textSize * _density * _scaleTextSizeFactor
        paint.textSize = textSize
        paint.color = danmaku.textColor
        if (_fakeBoldText) paint.isFakeBoldText = true
        _typeface?.let { paint.setTypeface(it) }

        // 透明度处理
        val alpha = if (danmaku.getType() == BaseDanmaku.TYPE_SPECIAL) danmaku.getAlpha() else _transparency
        paint.alpha = alpha

        if (alpha == AlphaValue.TRANSPARENT) {
            return IRenderer.NOTHING_RENDERING
        }

        // 获取或创建 canvas 包装器
        val canvas = _cachedCanvas?.takeIf { it.width == _width && it.height == _height }
            ?: SkiaDanmakuCanvasWrapper(skiaCanvas, _width, _height).also { _cachedCanvas = it }
        canvas.canvas = skiaCanvas

        // 绘制
        val cacheStuffer = mContext.mCacheStuffer
        if (cacheStuffer != null) {
            val cacheDrawn = cacheStuffer.drawCache(danmaku, canvas, left, top, paint)
            if (cacheDrawn) {
                return IRenderer.CACHE_RENDERING
            }
            cacheStuffer.drawDanmaku(danmaku, canvas, left, top, false, paint)
            return IRenderer.TEXT_RENDERING
        }

        return IRenderer.NOTHING_RENDERING
    }

    override fun recycle(danmaku: BaseDanmaku) {
        // Skia 资源由 GC 管理
    }

    override fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        mContext.mCacheStuffer?.prepare(danmaku, fromWorkerThread)
    }

    override fun measure(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        val paint = _paint
        val textSize = danmaku.textSize * _density * _scaleTextSizeFactor
        paint.textSize = textSize
        if (_fakeBoldText) paint.isFakeBoldText = true
        _typeface?.let { paint.setTypeface(it) }

        val cacheStuffer = mContext.mCacheStuffer
        if (cacheStuffer != null) {
            cacheStuffer.measure(danmaku, paint, fromWorkerThread)
        } else {
            val text = danmaku.text?.toString() ?: ""
            val textWidth = paint.measureText(text)
            val fm = paint.getFontMetrics()
            danmaku.paintWidth = textWidth + _strokeWidth * 2
            danmaku.paintHeight = fm.descent - fm.ascent + fm.leading
        }
    }

    override fun resetSlopPixel(factor: Float) {
        _slopPixel = (6 * _density * factor).toInt()
    }

    override fun setDensities(density: Float, densityDpi: Int, scaledDensity: Float) {
        _density = density
        _densityDpi = densityDpi
        _scaledDensity = scaledDensity
    }

    override fun setSize(width: Int, height: Int) {
        if (_width != width || _height != height) {
            _width = width
            _height = height
            _cachedCanvas = null
            mContext.mDanmakuFactory.notifyDispSizeChanged(mContext)
        }
    }

    override fun setDanmakuStyle(style: Int, data: FloatArray?) {
        when (style) {
            IDisplayer.DANMAKU_STYLE_SHADOW,
            IDisplayer.DANMAKU_STYLE_STROKEN,
            IDisplayer.DANMAKU_STYLE_PROJECTION -> {
                _strokeWidth = data?.firstOrNull() ?: 0f
            }
            else -> {
                _strokeWidth = 0f
            }
        }
    }

    override fun setMargin(m: Int) { _margin = m }
    override fun setAllMarginTop(m: Int) { _allMarginTop = m }
    override fun clearTextHeightCache() { /* 由 cacheStuffer 管理 */ }
    override fun setTypeFace(typeface: DanmakuTypeface?) { _typeface = typeface }
    override fun setTransparency(alpha: Int) { _transparency = alpha }
    override fun setScaleTextSizeFactor(factor: Float) { _scaleTextSizeFactor = factor }
    override fun setFakeBoldText(fakeBold: Boolean) { _fakeBoldText = fakeBold }
}

/**
 * 可复用的 SkiaCanvas 包装器，避免每帧创建新的 [SkiaCanvas] 对象。
 * 内部持有对当前帧 Skia Canvas 的引用，每帧通过 [canvas] 属性更新。
 */
internal class SkiaDanmakuCanvasWrapper(
    var canvas: SkiaCanvas,
    override val width: Int,
    override val height: Int,
) : DanmakuCanvas {

    override fun drawText(text: String, x: Float, y: Float, paint: DanmakuPaint) {
        val sp = paint as SkiaPaint
        canvas.drawString(text, x, y, sp.font, sp.paint)
    }

    override fun drawBitmap(bitmap: DanmakuBitmap, left: Float, top: Float, paint: DanmakuPaint?) {
        val sb = bitmap as SkiaBitmap
        val skiaPaint = (paint as? SkiaPaint)?.paint
        canvas.drawImage(sb.image, left, top, skiaPaint)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: DanmakuPaint) {
        val sp = paint as SkiaPaint
        canvas.drawRect(org.jetbrains.skia.Rect.makeLTRB(left, top, right, bottom), sp.paint)
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint) {
        val sp = paint as SkiaPaint
        canvas.drawLine(startX, startY, stopX, stopY, sp.paint)
    }

    override fun save(): Int = canvas.save()
    override fun restore() { canvas.restore() }
    override fun translate(dx: Float, dy: Float) { canvas.translate(dx, dy) }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(org.jetbrains.skia.Rect.makeLTRB(left, top, right, bottom))
    }

    override fun clear(color: Int) {
        canvas.clear(color)
    }

    override fun concat(matrix: FloatArray) {
        canvas.concat(org.jetbrains.skia.Matrix33(
            matrix[0], matrix[1], matrix[2],
            matrix[3], matrix[4], matrix[5],
            matrix[6], matrix[7], matrix[8]
        ))
    }
}
