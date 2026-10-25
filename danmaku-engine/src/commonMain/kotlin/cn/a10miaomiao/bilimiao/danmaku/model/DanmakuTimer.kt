package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 弹幕时钟（渲染时钟）
 *
 * 只按真实墙钟推进，不参考播放器位置、也不乘播放倍速：
 * 弹幕被投喂进来时把当前值记为其出场锚点，之后位置、透明度、消失全部基于
 * `弹幕时钟 - 出场锚点` 计算，因此滚动速度与停留时长在视觉上恒定。
 */
open class DanmakuTimer {

    var currMillisecond: Long = 0L
        protected set

    constructor()

    constructor(curr: Long) {
        update(curr)
    }

    open fun update(curr: Long): Long {
        val interval = curr - currMillisecond
        currMillisecond = curr
        return interval
    }

    fun add(mills: Long): Long {
        return update(currMillisecond + mills)
    }
}
