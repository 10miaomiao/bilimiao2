package cn.a10miaomiao.bilimiao.danmaku.platform

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuFactory
import cn.a10miaomiao.bilimiao.danmaku.model.AlphaValue
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.renderer.IRenderer
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * Desktop 端弹幕显示器实现
 *
 * 将弹幕渲染到 BufferedImage，供 Compose 绘制。
 */
class DesktopDisplayer(
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

    // 双缓冲：front 供 UI 读取，back 供渲染线程写入
    private var _frontImage: BufferedImage? = null
    private var _backImage: BufferedImage? = null
    private var _frontG2d: Graphics2D? = null
    private var _backG2d: Graphics2D? = null

    private val _paint = DesktopPaint()

    override val width: Int get() = _width
    override val height: Int get() = _height
    override val density: Float get() = _density
    override val densityDpi: Int get() = _densityDpi
    override val scaledDensity: Float get() = _scaledDensity
    override val slopPixel: Int get() = _slopPixel
    override val strokeWidth: Float get() = _strokeWidth
    override val isHardwareAccelerated: Boolean get() = false
    override val maximumCacheWidth: Int get() = _width
    override val maximumCacheHeight: Int get() = _height
    override val margin: Int get() = _margin
    override val allMarginTop: Int get() = _allMarginTop

    /**
     * 获取当前渲染图像（front buffer，供 UI 线程读取）
     */
    fun getImage(): BufferedImage? = _frontImage

    /**
     * 创建绘图表面
     */
    private fun createGraphics(image: BufferedImage): Graphics2D {
        return image.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        }
    }

    private fun createSurface() {
        if (_width <= 0 || _height <= 0) return
        _frontImage = BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB)
        _backImage = BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB)
        _frontG2d = createGraphics(_frontImage!!)
        _backG2d = createGraphics(_backImage!!)
    }

    /**
     * 清除画布（back buffer）
     */
    fun clearCanvas() {
        val g2d = _backG2d ?: return
        val image = _backImage ?: return
        g2d.composite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.CLEAR)
        g2d.fillRect(0, 0, image.width, image.height)
        g2d.composite = java.awt.AlphaComposite.SrcOver
    }

    override fun draw(danmaku: BaseDanmaku): Int {
        val g2d = _backG2d ?: return IRenderer.NOTHING_RENDERING

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

        // 绘制
        val cacheStuffer = mContext.mCacheStuffer
        if (cacheStuffer != null) {
            val canvas = DesktopCanvas(g2d, _width, _height)
            val cacheDrawn = cacheStuffer.drawCache(danmaku, canvas, left, top, paint)
            if (cacheDrawn) {
                return IRenderer.CACHE_RENDERING
            }
            // 无缓存，直接绘制文本
            cacheStuffer.drawDanmaku(danmaku, canvas, left, top, false, paint)
            return IRenderer.TEXT_RENDERING
        }

        return IRenderer.NOTHING_RENDERING
    }

    /**
     * 交换前后缓冲区，使渲染结果对 UI 线程可见
     */
    fun swapBuffers() {
        synchronized(this) {
            val tmpImage = _frontImage
            val tmpG2d = _frontG2d
            _frontImage = _backImage
            _frontG2d = _backG2d
            _backImage = tmpImage
            _backG2d = tmpG2d
        }
    }

    override fun recycle(danmaku: BaseDanmaku) {
        // Desktop 端无需特殊回收
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
            createSurface()
            // 通知工厂视口尺寸变更，重新计算弹幕时长
            mContext.mDanmakuFactory.notifyDispSizeChanged(mContext)
        }
    }

    override fun setDanmakuStyle(style: Int, data: FloatArray?) {
        when (style) {
            IDisplayer.DANMAKU_STYLE_SHADOW -> {
                _strokeWidth = data?.firstOrNull() ?: 0f
            }
            IDisplayer.DANMAKU_STYLE_STROKEN -> {
                _strokeWidth = data?.firstOrNull() ?: 0f
            }
            IDisplayer.DANMAKU_STYLE_PROJECTION -> {
                _strokeWidth = data?.firstOrNull() ?: 0f
            }
            else -> {
                _strokeWidth = 0f
            }
        }
    }

    override fun setMargin(m: Int) {
        _margin = m
    }

    override fun setAllMarginTop(m: Int) {
        _allMarginTop = m
    }

    override fun clearTextHeightCache() {
        // 由 cacheStuffer 管理
    }

    override fun setTypeFace(typeface: DanmakuTypeface?) {
        _typeface = typeface
    }

    override fun setTransparency(alpha: Int) {
        _transparency = alpha
    }

    override fun setScaleTextSizeFactor(factor: Float) {
        _scaleTextSizeFactor = factor
    }

    override fun setFakeBoldText(fakeBold: Boolean) {
        _fakeBoldText = fakeBold
    }
}
