package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 弹幕绘图画布抽象，对应 Android Canvas
 */
interface DanmakuCanvas {
    val width: Int
    val height: Int

    fun drawText(text: String, x: Float, y: Float, paint: DanmakuPaint)
    fun drawBitmap(bitmap: DanmakuBitmap, left: Float, top: Float, paint: DanmakuPaint? = null)
    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: DanmakuPaint)
    fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: DanmakuPaint)

    fun save(): Int
    fun restore()
    fun translate(dx: Float, dy: Float)
    fun clipRect(left: Float, top: Float, right: Float, bottom: Float)
    fun clear(color: Int = 0)
    fun concat(matrix: FloatArray)
}
