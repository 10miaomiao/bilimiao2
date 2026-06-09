package cn.a10miaomiao.bilimiao.danmaku.platform

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff

class AndroidCanvas(private val canvas: Canvas) : DanmakuCanvas {
    override val width: Int get() = canvas.width
    override val height: Int get() = canvas.height

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
