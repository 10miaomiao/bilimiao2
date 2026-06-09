package cn.a10miaomiao.bilimiao.danmaku.model

open class DanmakuTimer {

    var currMillisecond: Long = 0L
        protected set

    var lastInterval: Long = 0L
        protected set

    constructor()

    constructor(curr: Long) {
        update(curr)
    }

    open fun update(curr: Long): Long {
        lastInterval = curr - currMillisecond
        currMillisecond = curr
        return lastInterval
    }

    fun add(mills: Long): Long {
        return update(currMillisecond + mills)
    }
}
