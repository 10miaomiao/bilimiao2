package cn.a10miaomiao.bilimiao.danmaku.cache

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.SpecialDanmaku
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuBitmap
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuCanvas
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuPaint
import cn.a10miaomiao.bilimiao.danmaku.platform.FontMetrics
import cn.a10miaomiao.bilimiao.danmaku.platform.PaintStyle

/**
 * 弹幕绘制填充器代理
 *
 * 用于在弹幕显示前自定义文本内容和释放资源。
 */
interface CacheStufferProxy {
    /**
     * 在弹幕显示前准备绘制数据
     *
     * @param danmaku 弹幕对象
     * @param fromWorkerThread 是否在工作线程（true 时可执行耗时操作）
     */
    fun prepareDrawing(danmaku: BaseDanmaku, fromWorkerThread: Boolean)

    /**
     * 释放弹幕相关资源
     */
    fun releaseResource(danmaku: BaseDanmaku)
}

/**
 * 弹幕绘制填充器基类
 *
 * 负责弹幕文本的测量和绘制。子类可覆写绘制方法实现自定义样式。
 * 通过 [CacheStufferProxy] 可在绘制前修改弹幕内容。
 */
abstract class BaseCacheStuffer {

    companion object {
        /** 默认背景色（透明） */
        const val DEFAULT_BACKGROUND_COLOR = 0x00000000

        /** 默认阴影色（透明，无阴影） */
        const val SHADOW_COLOR = 0x00000000

        /** 默认描边宽度 */
        const val DEFAULT_STROKE_WIDTH = 2f

        /** 默认下划线高度 */
        const val UNDERLINE_HEIGHT = 2f

        /** 默认边框宽度 */
        const val BORDER_WIDTH = 2f
    }

    /** 弹幕上下文 */
    protected var mContext: DanmakuContext? = null

    /** 绘制代理 */
    protected var mProxy: CacheStufferProxy? = null

    /**
     * 设置弹幕上下文
     */
    fun setContext(context: DanmakuContext) {
        mContext = context
    }

    /**
     * 设置绘制代理
     */
    fun setProxy(proxy: CacheStufferProxy?) {
        mProxy = proxy
    }

    /**
     * 测量弹幕文本的宽高
     *
     * 测量结果会写入 danmaku.paintWidth 和 danmaku.paintHeight。
     *
     * @param danmaku 弹幕对象
     * @param paint 画笔
     * @param fromWorkerThread 是否在工作线程
     */
    abstract fun measure(danmaku: BaseDanmaku, paint: DanmakuPaint, fromWorkerThread: Boolean)

    /**
     * 绘制弹幕文本
     *
     * @param danmaku 弹幕对象
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标
     * @param fromWorkerThread 是否在工作线程
     * @param paint 画笔
     */
    abstract fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        paint: DanmakuPaint
    )

    /**
     * 绘制缓存的弹幕位图
     *
     * @param danmaku 弹幕对象
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标
     * @param paint 画笔
     * @return 是否成功绘制缓存
     */
    open fun drawCache(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint
    ): Boolean {
        val cache = danmaku.getDrawingCache() ?: return false
        val holder = cache.get() ?: return false
        // holder 应实现 draw 方法，由平台特定实现提供
        return false
    }

    /**
     * 准备弹幕绘制数据
     *
     * 在弹幕显示前调用，可用于自定义文本内容。
     * 默认实现委托给 [CacheStufferProxy]。
     *
     * @param danmaku 弹幕对象
     * @param fromWorkerThread 是否在工作线程
     */
    open fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        mProxy?.prepareDrawing(danmaku, fromWorkerThread)
    }

    /**
     * 清除缓存
     */
    abstract fun clearCaches()

    /**
     * 清除指定弹幕的缓存
     */
    open fun clearCache(danmaku: BaseDanmaku) {
        // 默认无操作，子类可覆写
    }

    /**
     * 释放弹幕资源
     */
    open fun releaseResource(danmaku: BaseDanmaku) {
        mProxy?.releaseResource(danmaku)
    }
}

/**
 * 纯文本弹幕绘制填充器
 *
 * 支持纯文本显示，处理文字描边、阴影、下划线和边框。
 * 对应原始 Android 版本的 SimpleTextCacheStuffer。
 */
class SimpleTextCacheStuffer : BaseCacheStuffer() {

    companion object {
        /** 文本高度缓存，避免重复计算 */
        private val sTextHeightCache = mutableMapOf<Float, Float>()
    }

    /**
     * 获取缓存的文本行高
     *
     * 相同字号的文本行高相同，使用缓存避免重复计算。
     */
    protected fun getCacheHeight(danmaku: BaseDanmaku, paint: DanmakuPaint): Float {
        val textSize = paint.textSize
        val cached = sTextHeightCache[textSize]
        if (cached != null) return cached
        val fontMetrics = paint.getFontMetrics()
        val textHeight = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading
        sTextHeightCache[textSize] = textHeight
        return textHeight
    }

    override fun measure(danmaku: BaseDanmaku, paint: DanmakuPaint, fromWorkerThread: Boolean) {
        var w = 0f
        var textHeight = 0f
        if (danmaku.lines == null) {
            if (danmaku.text == null) {
                w = 0f
            } else {
                w = paint.measureText(danmaku.text.toString())
                textHeight = getCacheHeight(danmaku, paint)
            }
            danmaku.paintWidth = w
            danmaku.paintHeight = textHeight
        } else {
            textHeight = getCacheHeight(danmaku, paint)
            for (line in danmaku.lines!!) {
                if (line.isNotEmpty()) {
                    val tw = paint.measureText(line)
                    w = maxOf(tw, w)
                }
            }
            danmaku.paintWidth = w
            danmaku.paintHeight = danmaku.lines!!.size * textHeight
        }
    }

    /**
     * 绘制描边文本
     *
     * @param danmaku 弹幕对象
     * @param lineText 单行文本（null 时使用 danmaku.text）
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标（基线位置）
     * @param paint 画笔（已设置为描边样式）
     */
    protected open fun drawStroke(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint
    ) {
        val text = lineText ?: danmaku.text?.toString() ?: return
        canvas.drawText(text, left, top, paint)
    }

    /**
     * 绘制填充文本
     *
     * @param danmaku 弹幕对象
     * @param lineText 单行文本（null 时使用 danmaku.text）
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标（基线位置）
     * @param paint 画笔（已设置为填充样式）
     * @param fromWorkerThread 是否在工作线程
     */
    protected open fun drawText(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint,
        fromWorkerThread: Boolean
    ) {
        // 特殊弹幕在工作线程绘制时设置完全不透明
        if (fromWorkerThread && danmaku is SpecialDanmaku) {
            paint.alpha = 255
        }
        val text = lineText ?: danmaku.text?.toString() ?: return
        canvas.drawText(text, left, top, paint)
    }

    /**
     * 绘制弹幕背景
     *
     * 默认无背景，子类可覆写添加背景绘制。
     */
    protected open fun drawBackground(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float
    ) {
        // 默认无背景
    }

    override fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        paint: DanmakuPaint
    ) {
        var _left = left
        var _top = top
        var textLeft = left + danmaku.padding
        var textTop = top + danmaku.padding

        // 边框偏移
        if (danmaku.borderColor != 0) {
            textLeft += BORDER_WIDTH
            textTop += BORDER_WIDTH
        }

        // 配置画笔参数
        val hasShadow = danmaku.textShadowColor != 0
        val hasBorder = danmaku.borderColor != 0
        val hasUnderline = danmaku.underlineColor != 0

        drawBackground(danmaku, canvas, _left, _top)

        val fontMetrics = paint.getFontMetrics()
        val ascentOffset = -fontMetrics.ascent

        if (danmaku.lines != null) {
            val lines = danmaku.lines!!
            if (lines.size == 1) {
                // 单行文本
                drawDanmakuLine(
                    danmaku, lines[0], canvas, textLeft, textTop + ascentOffset,
                    paint, fromWorkerThread, hasShadow
                )
            } else {
                // 多行文本
                val textHeight = (danmaku.paintHeight - 2 * danmaku.padding) / lines.size
                for (t in lines.indices) {
                    if (lines[t].isEmpty()) continue
                    drawDanmakuLine(
                        danmaku, lines[t], canvas,
                        textLeft, t * textHeight + textTop + ascentOffset,
                        paint, fromWorkerThread, hasShadow
                    )
                }
            }
        } else {
            // 无多行拆分的文本
            drawDanmakuLine(
                danmaku, null, canvas, textLeft, textTop + ascentOffset,
                paint, fromWorkerThread, hasShadow
            )
        }

        // 绘制下划线
        if (hasUnderline) {
            paint.style = PaintStyle.FILL
            paint.color = danmaku.underlineColor
            paint.strokeWidth = UNDERLINE_HEIGHT
            val bottom = _top + danmaku.paintHeight - UNDERLINE_HEIGHT
            canvas.drawLine(_left, bottom, _left + danmaku.paintWidth, bottom, paint)
        }

        // 绘制边框
        if (hasBorder) {
            paint.style = PaintStyle.STROKE
            paint.color = danmaku.borderColor
            paint.strokeWidth = BORDER_WIDTH
            canvas.drawRect(
                _left, _top,
                _left + danmaku.paintWidth, _top + danmaku.paintHeight,
                paint
            )
        }
    }

    /**
     * 绘制单行弹幕文本（描边 + 填充）
     */
    private fun drawDanmakuLine(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint,
        fromWorkerThread: Boolean,
        hasShadow: Boolean
    ) {
        if (hasShadow) {
            // 绘制描边/阴影层
            paint.style = PaintStyle.STROKE
            paint.color = danmaku.textShadowColor
            paint.strokeWidth = DEFAULT_STROKE_WIDTH
            drawStroke(danmaku, lineText, canvas, left, top, paint)
        }

        // 绘制填充层
        paint.style = PaintStyle.FILL
        paint.color = danmaku.textColor
        drawText(danmaku, lineText, canvas, left, top, paint, fromWorkerThread)
    }

    override fun clearCaches() {
        sTextHeightCache.clear()
    }
}
