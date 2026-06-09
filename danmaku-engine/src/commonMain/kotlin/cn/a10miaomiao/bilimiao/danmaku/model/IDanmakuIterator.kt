package cn.a10miaomiao.bilimiao.danmaku.model

interface IDanmakuIterator {
    fun next(): BaseDanmaku
    fun hasNext(): Boolean
    fun reset()
    fun remove()
}
