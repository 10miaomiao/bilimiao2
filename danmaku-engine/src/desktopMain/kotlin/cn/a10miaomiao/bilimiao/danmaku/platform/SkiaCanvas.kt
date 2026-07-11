package cn.a10miaomiao.bilimiao.danmaku.platform

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

/**
 * Desktop 端基于 Skia Canvas 的画布实现
 *
 * 直接绘制到 Skia Canvas（GPU 后端），绕过 AWT BufferedImage。
 */
class SkiaCanvas(
    private val canvas: Canvas,
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
        canvas.drawRect(Rect.makeLTRB(left, top, right, bottom), sp.paint)
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint) {
        val sp = paint as SkiaPaint
        canvas.drawLine(startX, startY, stopX, stopY, sp.paint)
    }

    override fun save(): Int {
        return canvas.save()
    }

    override fun restore() {
        canvas.restore()
    }

    override fun translate(dx: Float, dy: Float) {
        canvas.translate(dx, dy)
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(Rect.makeLTRB(left, top, right, bottom))
    }

    override fun clear(color: Int) {
        canvas.clear(color)
    }

    override fun concat(matrix: FloatArray) {
        // Android Matrix 9 值数组: [scaleX, skewX, transX, skewY, scaleY, transY, persp0, persp1, persp2]
        // Matrix33 行主序: [scaleX, skewX, transX, skewY, scaleY, transY, persp0, persp1, persp2]
        canvas.concat(Matrix33(
            matrix[0], matrix[1], matrix[2],
            matrix[3], matrix[4], matrix[5],
            matrix[6], matrix[7], matrix[8]
        ))
    }
}
