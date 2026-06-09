package cn.a10miaomiao.bilimiao.danmaku.platform

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

/**
 * Desktop 端基于 AWT Graphics2D 的画布实现
 */
class DesktopCanvas(private val g2d: Graphics2D, override val width: Int, override val height: Int) : DanmakuCanvas {

    constructor(image: BufferedImage) : this(image.createGraphics(), image.width, image.height)

    init {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    }

    override fun drawText(text: String, x: Float, y: Float, paint: DanmakuPaint) {
        val dp = paint as DesktopPaint
        dp.applyToGraphics(g2d)
        g2d.drawString(text, x, y)
    }

    override fun drawBitmap(bitmap: DanmakuBitmap, left: Float, top: Float, paint: DanmakuPaint?) {
        val db = bitmap as DesktopBitmap
        g2d.drawImage(db.image, left.toInt(), top.toInt(), null)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: DanmakuPaint) {
        val dp = paint as DesktopPaint
        dp.applyToGraphics(g2d)
        val w = right - left
        val h = bottom - top
        when (dp.style) {
            PaintStyle.FILL -> g2d.fillRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
            PaintStyle.STROKE -> g2d.drawRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
            PaintStyle.FILL_AND_STROKE -> {
                g2d.fillRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
                g2d.drawRect(left.toInt(), top.toInt(), w.toInt(), h.toInt())
            }
        }
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint) {
        val dp = paint as DesktopPaint
        dp.applyToGraphics(g2d)
        g2d.drawLine(startX.toInt(), startY.toInt(), stopX.toInt(), stopY.toInt())
    }

    private val transformStack = ArrayDeque<AffineTransform>()

    override fun save(): Int {
        transformStack.addLast(g2d.transform)
        return transformStack.size
    }

    override fun restore() {
        if (transformStack.isNotEmpty()) {
            g2d.transform = transformStack.removeLast()
        }
    }

    override fun translate(dx: Float, dy: Float) {
        g2d.translate(dx.toDouble(), dy.toDouble())
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        g2d.clipRect(left.toInt(), top.toInt(), (right - left).toInt(), (bottom - top).toInt())
    }

    override fun clear(color: Int) {
        val oldColor = g2d.color
        g2d.color = java.awt.Color(color, true)
        g2d.composite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.CLEAR)
        g2d.fillRect(0, 0, width, height)
        g2d.composite = java.awt.AlphaComposite.SrcOver
        g2d.color = oldColor
    }

    override fun concat(matrix: FloatArray) {
        val m = AffineTransform(
            matrix[0].toDouble(), matrix[1].toDouble(),
            matrix[3].toDouble(), matrix[4].toDouble(),
            matrix[2].toDouble(), matrix[5].toDouble()
        )
        g2d.transform(m)
    }
}
