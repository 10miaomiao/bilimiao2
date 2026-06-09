package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus

/**
 * 渲染状态
 */
class RenderingState {
    companion object {
        const val UNKNOWN_TIME = -1L
    }

    var isRunningDanmakus = false
    var timer = DanmakuTimer()
    var indexInScreen = 0
    var totalSizeInScreen = 0
    var lastDanmaku: BaseDanmaku? = null

    var r2lDanmakuCount = 0
    var l2rDanmakuCount = 0
    var ftDanmakuCount = 0
    var fbDanmakuCount = 0
    var specialDanmakuCount = 0
    var totalDanmakuCount = 0
    var lastTotalDanmakuCount = 0
    var consumingTime = 0L
    var beginTime = 0L
    var endTime = 0L
    var nothingRendered = false
    var sysTime = 0L
    var cacheHitCount = 0L
    var cacheMissCount = 0L

    private var runningDanmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_LIST)
    private var mIsObtaining = false

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
        lastTotalDanmakuCount = totalDanmakuCount
        r2lDanmakuCount = 0
        l2rDanmakuCount = 0
        ftDanmakuCount = 0
        fbDanmakuCount = 0
        specialDanmakuCount = 0
        totalDanmakuCount = 0
        sysTime = 0
        beginTime = 0
        endTime = 0
        consumingTime = 0
        nothingRendered = false
        synchronized(this) {
            runningDanmakus.clear()
        }
    }

    fun set(other: RenderingState?) {
        if (other == null) return
        lastTotalDanmakuCount = other.lastTotalDanmakuCount
        r2lDanmakuCount = other.r2lDanmakuCount
        l2rDanmakuCount = other.l2rDanmakuCount
        ftDanmakuCount = other.ftDanmakuCount
        fbDanmakuCount = other.fbDanmakuCount
        specialDanmakuCount = other.specialDanmakuCount
        totalDanmakuCount = other.totalDanmakuCount
        consumingTime = other.consumingTime
        beginTime = other.beginTime
        endTime = other.endTime
        nothingRendered = other.nothingRendered
        sysTime = other.sysTime
        cacheHitCount = other.cacheHitCount
        cacheMissCount = other.cacheMissCount
    }

    fun appendToRunningDanmakus(danmaku: BaseDanmaku) {
        if (!mIsObtaining) {
            runningDanmakus.addItem(danmaku)
        }
    }

    fun obtainRunningDanmakus(): IDanmakus {
        mIsObtaining = true
        val danmakus: IDanmakus
        synchronized(this) {
            danmakus = runningDanmakus
            runningDanmakus = Danmakus(IDanmakus.ST_BY_LIST)
        }
        mIsObtaining = false
        return danmakus
    }
}
