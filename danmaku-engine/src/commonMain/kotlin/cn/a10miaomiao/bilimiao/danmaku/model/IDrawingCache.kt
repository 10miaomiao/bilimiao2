package cn.a10miaomiao.bilimiao.danmaku.model

interface IDrawingCache<T> {
    fun build(w: Int, h: Int, density: Int, checkSizeEquals: Boolean, bitsPerPixel: Int)
    fun erase()
    fun get(): T?
    fun destroy()
    fun size(): Int
    fun width(): Int
    fun height(): Int
    fun hasReferences(): Boolean
    fun increaseReference()
    fun decreaseReference()
}
