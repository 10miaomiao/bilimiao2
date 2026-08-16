package cn.a10miaomiao.bilimiao.danmaku.platform

import android.graphics.Canvas
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuFactory
import cn.a10miaomiao.bilimiao.danmaku.model.AlphaValue
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.renderer.IRenderer

/**
 * Android 端弹幕显示器实现
 *
 * 直接渲染到 Android [Canvas]（由 Compose DrawScope 注入），
 * 与桌面端 [SkiaDisplayer] 对应，复用同一套 [DanmakuEngine] 渲染逻辑。
 *
 * 使用方式：每帧由 Composable 在 DrawScope 中调用 [setCanvas] 注入当前 Canvas，
 * 然后调用 [DanmakuEngine.drawWithSync] 进行渲染，渲染完成后调用 [setCanvas](null) 释放引用。
 */
class AndroidDisplayer(
    private val mContext: DanmakuContext,
) : IDisplayer {

    private var _width: Int = 0
    private var _height: Int = 0
    private var _density: Float = 1f
    private var _densityDpi: Int = 160
    private var _scaledDensity: Float = 1f
    private var _slopPixel: Int = 6
    private var _strokeWidth: Float = 0f
    private var _margin: Int = 0
    private var _allMarginTop: Int = 0
    private var _transparency: Int = 255
    private var _scaleTextSizeFactor: Float = 1f
    private var _fakeBoldText: Boolean = false
    private var _typeface: DanmakuTypeface? = null

    /** 当前帧的 Android Canvas，由外部在每帧绘制前注入 */
    private var _androidCanvas: Canvas? = null

    /** 缓存的 DanmakuCanvas 包装，避免每帧创建 */
    private var _cachedCanvas: AndroidCanvas? = null

    private val _paint = AndroidPaint()

    override val width: Int get() = _width
    override val height: Int get() = _height
    override val density: Float get() = _density
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
     * 设置当前帧的 Android Canvas
     *
     * 由 Composable 在 DrawScope.draw 中调用，
     * 在调用 [DanmakuEngine.drawWithSync] 之前注入，渲染结束后传 null 释放引用。
     */
    fun setCanvas(canvas: Canvas?) {
        _androidCanvas = canvas
    }

    override fun draw(danmaku: BaseDanmaku): Int {
        val androidCanvas = _androidCanvas ?: return IRenderer.NOTHING_RENDERING

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
        val textSize = danmaku.textSize * _scaleTextSizeFactor
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

        // 获取或创建 canvas 包装器（尺寸变化时重建）
        val canvas = _cachedCanvas?.takeIf { it.width == _width && it.height == _height }
            ?: AndroidCanvas(androidCanvas, _width, _height).also { _cachedCanvas = it }
        // 每帧更新底层 Canvas 引用
        canvas.resetCanvas(androidCanvas)

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
        // Android Bitmap 资源由 cacheStuffer 管理
    }

    override fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        mContext.mCacheStuffer?.prepare(danmaku, fromWorkerThread)
    }

    override fun measure(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        val paint = _paint
        val textSize = danmaku.textSize * _scaleTextSizeFactor
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
