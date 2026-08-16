package cn.a10miaomiao.bilimiao.danmaku.platform

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff

/**
 * Android 端弹幕画布包装器
 *
 * 持有当前帧的 Android [Canvas] 引用，由 [AndroidDisplayer] 每帧通过 [resetCanvas] 更新。
 * [width] 和 [height] 在构造时固定，与 displayer 尺寸保持一致，避免每帧创建新对象。
 */
class AndroidCanvas(
    canvas: Canvas,
    override val width: Int = canvas.width,
    override val height: Int = canvas.height,
) : DanmakuCanvas {
    private var canvas: Canvas = canvas

    /**
     * 每帧更新底层 Canvas 引用。新 Canvas 的尺寸应与构造时的 [width]/[height] 一致。
     */
    fun resetCanvas(canvas: Canvas) {
        this.canvas = canvas
    }

    override fun drawText(text: String, x: Float, y: Float, paint: DanmakuPaint) {
        val ap = paint as AndroidPaint
        canvas.drawText(text, x, y, ap.paint)
    }

    override fun drawBitmap(bitmap: DanmakuBitmap, left: Float, top: Float, paint: DanmakuPaint?) {
        val ab = bitmap as AndroidBitmap
        canvas.drawBitmap(ab.bitmap, left, top, (paint as? AndroidPaint)?.paint)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: DanmakuPaint) {
        val ap = paint as AndroidPaint
        canvas.drawRect(left, top, right, bottom, ap.paint)
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint) {
        val ap = paint as AndroidPaint
        canvas.drawLine(startX, startY, stopX, stopY, ap.paint)
    }

    override fun save(): Int = canvas.save()
    override fun restore() = canvas.restore()
    override fun translate(dx: Float, dy: Float) = canvas.translate(dx, dy)
    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(left, top, right, bottom)
    }

    override fun clear(color: Int) {
        canvas.drawColor(color, PorterDuff.Mode.CLEAR)
    }

    override fun concat(matrix: FloatArray) {
        val m = android.graphics.Matrix()
        m.setValues(matrix)
        canvas.concat(m)
    }
}
