package cn.a10miaomiao.bilimiao.danmaku.cache

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.Duration
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.model.R2LDanmaku
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuBitmap
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuCanvas
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuPaint
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuTypeface
import cn.a10miaomiao.bilimiao.danmaku.platform.FontMetrics
import cn.a10miaomiao.bilimiao.danmaku.platform.PaintStyle
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 验证弹幕透明度在绘制链路生效：
 * drawDanmakuLine 中 paint.color 赋值（Android Paint.setColor）会覆盖 alpha，
 * drawText 必须以弹幕上下文的 transparency 重新设置 paint.alpha。
 */
class DanmakuTransparencyDrawTest {

    private class TestPaint : DanmakuPaint {
        override var textSize: Float = 0f
        override var color: Int = 0
        override var alpha: Int = 255
        override var strokeWidth: Float = 0f
        override var isAntiAlias: Boolean = false
        override var isFakeBoldText: Boolean = false
        override var style: PaintStyle = PaintStyle.FILL

        override fun measureText(text: String): Float = text.length.toFloat() * 10
        override fun measureText(text: String, start: Int, end: Int): Float = (end - start).toFloat() * 10
        override fun getFontMetrics(): FontMetrics = FontMetrics(0f, 20f, 0f, 0f, 20f)
        override fun setTypeface(typeface: DanmakuTypeface?) {}
        override fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {}
        override fun clearShadowLayer() {}
        override fun copy(): DanmakuPaint = this
    }

    private class TestCanvas : DanmakuCanvas {
        var lastAlpha = -1
        override val width: Int get() = 1080
        override val height: Int get() = 1920

        override fun drawText(text: String, x: Float, y: Float, paint: DanmakuPaint) {
            lastAlpha = paint.alpha
        }
        override fun drawBitmap(bitmap: DanmakuBitmap, left: Float, top: Float, paint: DanmakuPaint?) {}
        override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: DanmakuPaint) {}
        override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint) {}
        override fun save(): Int = 0
        override fun restore() {}
        override fun translate(dx: Float, dy: Float) {}
        override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {}
        override fun clear(color: Int) {}
        override fun concat(matrix: FloatArray) {}
    }

    private class DrawDisplayer(
        private val context: DanmakuContext,
        private val canvas: TestCanvas,
    ) : IDisplayer {
        override val width: Int get() = 1080
        override val height: Int get() = 1920
        override val density: Float get() = 1f
        override val densityDpi: Int get() = 160
        override val scaledDensity: Float get() = 1f
        override val slopPixel: Int get() = 6
        override val strokeWidth: Float get() = 0f
        override val isHardwareAccelerated: Boolean get() = true
        override val maximumCacheWidth: Int get() = 1080
        override val maximumCacheHeight: Int get() = 1920
        override val margin: Int get() = 0
        override val allMarginTop: Int get() = 0

        override fun draw(danmaku: BaseDanmaku): Int {
            // 模拟 AndroidDisplayer.draw：paint.color 赋值（覆盖 alpha）后交给 cacheStuffer 绘制
            val paint = TestPaint()
            paint.color = danmaku.textColor
            paint.alpha = 255
            val stuffer = context.mCacheStuffer
            stuffer?.drawDanmaku(danmaku, canvas, danmaku.getLeft(), danmaku.getTop(), false, paint)
            return 0
        }
        override fun recycle(danmaku: BaseDanmaku) {}
        override fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {}
        override fun measure(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {}
        override fun resetSlopPixel(factor: Float) {}
        override fun setDensities(density: Float, densityDpi: Int, scaledDensity: Float) {}
        override fun setSize(width: Int, height: Int) {}
        override fun setDanmakuStyle(style: Int, data: FloatArray?) {}
        override fun setMargin(m: Int) {}
        override fun setAllMarginTop(m: Int) {}
        override fun clearTextHeightCache() {}
        override fun setTypeFace(typeface: DanmakuTypeface?) {}
        override fun setTransparency(alpha: Int) {}
        override fun setScaleTextSizeFactor(factor: Float) {}
        override fun setFakeBoldText(fakeBold: Boolean) {}
    }

    @Test
    fun drawText_appliesContextTransparency() {
        val canvas = TestCanvas()
        val context = DanmakuContext.create()
        context.mDisplayer = DrawDisplayer(context, canvas)
        context.setCacheStuffer(SimpleTextCacheStuffer(), null)
        context.setDanmakuTransparency(0.5f) // transparency = 127

        val danmaku = R2LDanmaku(Duration(5000))
        danmaku.text = "test"
        // 模拟 paint.color 赋值覆盖 alpha 后的颜色（Android Paint.setColor 语义）
        danmaku.textColor = 0xFFFFFFFF.toInt()

        context.mDisplayer.draw(danmaku)

        assertEquals(127, canvas.lastAlpha)
    }
}
