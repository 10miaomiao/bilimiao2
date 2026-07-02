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

    /**
     * 实际 UI 缩放密度（Compose density），绘制阶段用于逻辑像素→物理像素缩放。
     *
     * 桌面端此值反映系统 UI 缩放（100% = 1.0，150% = 1.5，200% = 2.0），
     * 而非安卓的真实物理屏幕密度（通常 1.5~3.0）。
     */
    private var _density: Float = 1f
    private var _densityDpi: Int = 96
    private var _scaledDensity: Float = 1f

    /**
     * 解析器可见密度的下限。
     *
     * B站弹幕 XML 字号（如 25）在 [BiliDanmakuParser] 中通过 (density - 0.6f) 缩放，
     * 该启发式针对安卓手机真实屏幕密度（通常 1.5~3.0）调校。桌面端 Compose 的 density
     * 直接反映系统 UI 缩放，100% 时为 1.0，代入后 (1.0 - 0.6) = 0.4 会让基础字号被压到 40%，
     * 叠加绘制阶段再次乘以 density，最终像素尺寸过小。
     *
     * 此处令 [density] getter 返回 max(_density, _minParserDensity)，保证 (density - 0.6)
     * 不低于 0.9（等效 240dpi 安卓设备的基础字号）；density ≥ 1.5（150% 缩放及以上）不受影响，
     * 保持原有渲染效果。绘制阶段仍使用原始 [_density]，DPI 缩放线性生效。
     */
    private val _minParserDensity: Float = 1.5f

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

    // 同步锁：保护快照引用交换
    private val _lock = Any()
    // 延迟创建标记：setSize() 只记录尺寸，由渲染线程在下一帧执行 createSurface()
    @Volatile private var _pendingWidth: Int = -1
    @Volatile private var _pendingHeight: Int = -1

    // 三缓冲：彻底隔离渲染线程与 UI 线程
    // _writeBuffer: 渲染线程写入（UI 线程不读取）
    // _snapshotBuffer: UI 线程读取（渲染线程不写入）
    // _spareBuffer: 空闲 buffer，下一帧成为 writeBuffer（被复用，零分配）
    private var _writeBuffer: IntArray? = null
    @Volatile private var _snapshotBuffer: IntArray? = null
    private var _spareBuffer: IntArray? = null
    @Volatile private var _snapshotWidth: Int = 0
    @Volatile private var _snapshotHeight: Int = 0

    // 缓存的 canvas，避免每条弹幕创建一个 DesktopCanvas
    private var _cachedCanvas: DesktopCanvas? = null

    private val _paint = DesktopPaint()

    override val width: Int get() = _width
    override val height: Int get() = _height

    /**
     * 解析器与部分引擎逻辑可见的密度。返回原始 [_density] 与 [_minParserDensity] 的较大者，
     * 避免低 DPI 下 (density - 0.6) 字号缩放过小（见 [_minParserDensity] 说明）。
     */
    override val density: Float get() = maxOf(_density, _minParserDensity)
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
     * 将当前 back buffer 的已绘制内容复制到快照
     * 由渲染线程在每帧绘制完成后、swapBuffers 之前调用
     *
     * 真正的三缓冲：
     * 1. 渲染线程写入 _writeBuffer（UI 线程不读取）
     * 2. 原子交换 _writeBuffer ↔ _snapshotBuffer
     * 3. 旧 snapshot 成为下一帧的 _spareBuffer（被复用，零分配）
     */
    fun snapshotPixels() {
        val src = _backImage ?: return
        val srcData = (src.raster.dataBuffer as java.awt.image.DataBufferInt).data
        // 渲染线程写入 writeBuffer
        var wb = _writeBuffer
        if (wb == null || wb.size != srcData.size) {
            wb = IntArray(srcData.size)
            _writeBuffer = wb
        }
        System.arraycopy(srcData, 0, wb, 0, srcData.size)
        // 原子交换：writeBuffer 成为 snapshot，旧 snapshot 成为 spare
        synchronized(_lock) {
            _spareBuffer = _snapshotBuffer
            _snapshotBuffer = wb
            _writeBuffer = _spareBuffer // spare 成为下一帧的 writeBuffer
            _snapshotWidth = src.width
            _snapshotHeight = src.height
        }
    }

    /**
     * 获取渲染图像的像素快照引用
     *
     * 三缓冲保证安全：返回的 IntArray 在下一帧 snapshotPixels() 写入 _writeBuffer 时不会被修改，
     * 因为 _writeBuffer 是独立的 buffer（spare 或新分配的）。
     */
    fun getImagePixels(): IntArray? = _snapshotBuffer

    /** 快照宽度 */
    val snapshotWidth: Int get() = _snapshotWidth

    /** 快照高度 */
    val snapshotHeight: Int get() = _snapshotHeight

    // 直接渲染模式的缓存资源
    private var _renderImage: BufferedImage? = null
    private var _renderG2d: Graphics2D? = null
    private var _renderW: Int = 0
    private var _renderH: Int = 0

    /**
     * 在主线程直接渲染弹幕（带缓存，尺寸不变时复用 BufferedImage 和 Graphics2D）
     */
    fun renderDanmaku(engine: cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine, w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        // 尺寸变化时重建缓存
        if (w != _renderW || h != _renderH) {
            _renderG2d?.dispose()
            _renderImage = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
            _renderG2d = _renderImage!!.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
                setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            }
            _renderW = w
            _renderH = h
            // 同步尺寸到 displayer 内部状态
            _width = w
            _height = h
            _cachedCanvas = null
            mContext.mDanmakuFactory.notifyDispSizeChanged(mContext)
        }

        val image = _renderImage ?: return
        val g2d = _renderG2d ?: return

        // 清除画布
        g2d.composite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.CLEAR)
        g2d.fillRect(0, 0, w, h)
        g2d.composite = java.awt.AlphaComposite.SrcOver

        // 设置 Graphics2D 并渲染
        _backG2d = g2d
        _backImage = image
        _cachedCanvas = null
        engine.drawWithSync(this)
    }

    /**
     * 获取最近一次渲染的像素数据
     */
    fun getRenderPixels(): IntArray? {
        val image = _renderImage ?: return null
        return (image.raster.dataBuffer as java.awt.image.DataBufferInt).data
    }

    /**
     * 设置当前帧的 Graphics2D 和 image（直接渲染模式使用）
     */
    fun setGraphics(g2d: Graphics2D, image: BufferedImage) {
        _backG2d = g2d
        _backImage = image
        _cachedCanvas = null
    }

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
        // TYPE_INT_ARGB 的 int 值在小端(x86)内存中字节序为 B,G,R,A，与 Skia BGRA_8888 一致
        _frontImage = BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB)
        _backImage = BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB)
        _frontG2d = createGraphics(_frontImage!!)
        _backG2d = createGraphics(_backImage!!)
        _cachedCanvas = null // 重置缓存的 canvas
        // 不清空快照缓冲区：snapshotPixels() 会在尺寸变化时自动重新分配
        // 避免 _snapshotBuffer = null 导致 UI 线程读到 null 像素显示空白帧
    }

    /**
     * 应用延迟的尺寸变更（由渲染线程在每帧开始时调用）
     * 返回 true 表示发生了尺寸变更
     */
    fun applyPendingSize(): Boolean {
        val pw: Int
        val ph: Int
        synchronized(_lock) {
            pw = _pendingWidth
            ph = _pendingHeight
            if (pw < 0 || ph < 0) return false
            _pendingWidth = -1
            _pendingHeight = -1
        }
        // 在渲染线程安全地重建表面
        _width = pw
        _height = ph
        createSurface()
        _cachedCanvas = null // Graphics2D 已变，重置缓存
        mContext.mDanmakuFactory.notifyDispSizeChanged(mContext)
        return true
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
            val canvas = _cachedCanvas ?: DesktopCanvas(g2d, _width, _height).also { _cachedCanvas = it }
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
     * 交换前后缓冲区
     */
    fun swapBuffers() {
        synchronized(_lock) {
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
        // 避免浮点精度抖动导致每帧重建：同时检查 _width/_height 和 _pendingWidth/_pendingHeight
        if ((_width != width || _height != height) && (_pendingWidth != width || _pendingHeight != height)) {
            synchronized(_lock) {
                _pendingWidth = width
                _pendingHeight = height
            }
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
