package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer

/**
 * 单帧渲染状态
 *
 * 只保留过滤器需要的统计信息（同屏序号、同屏总数、各类型计数）和渲染耗时用的计时器。
 * 早期为「空闲休眠」「运行中弹幕同步」等机制准备的字段（beginTime/endTime/runningDanmakus
 * 等）已随那些机制一起移除。
 */
class RenderingState {

    /** 渲染计时器（墙钟），供过滤器判断渲染耗时 */
    var timer = DanmakuTimer()

    /** 当前弹幕在本帧屏幕内的序号 */
    var indexInScreen = 0

    /** 本帧屏幕内弹幕总数 */
    var totalSizeInScreen = 0

    var lastDanmaku: BaseDanmaku? = null

    var r2lDanmakuCount = 0
    var l2rDanmakuCount = 0
    var ftDanmakuCount = 0
    var fbDanmakuCount = 0
    var specialDanmakuCount = 0
    var totalDanmakuCount = 0
    var consumingTime = 0L

    fun addTotalCount(count: Int): Int {
        totalDanmakuCount += count
        return totalDanmakuCount
    }

    fun addCount(type: Int, count: Int): Int {
        return when (type) {
            BaseDanmaku.TYPE_SCROLL_RL -> {
                r2lDanmakuCount += count
                r2lDanmakuCount
            }
            BaseDanmaku.TYPE_SCROLL_LR -> {
                l2rDanmakuCount += count
                l2rDanmakuCount
            }
            BaseDanmaku.TYPE_FIX_TOP -> {
                ftDanmakuCount += count
                ftDanmakuCount
            }
            BaseDanmaku.TYPE_FIX_BOTTOM -> {
                fbDanmakuCount += count
                fbDanmakuCount
            }
            BaseDanmaku.TYPE_SPECIAL -> {
                specialDanmakuCount += count
                specialDanmakuCount
            }
            else -> 0
        }
    }

    fun reset() {
        r2lDanmakuCount = 0
        l2rDanmakuCount = 0
        ftDanmakuCount = 0
        fbDanmakuCount = 0
        specialDanmakuCount = 0
        totalDanmakuCount = 0
        consumingTime = 0
    }
}
