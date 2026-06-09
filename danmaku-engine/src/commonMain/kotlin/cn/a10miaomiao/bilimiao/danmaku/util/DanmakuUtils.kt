package cn.a10miaomiao.bilimiao.danmaku.util

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

object DanmakuUtils {

    /**
     * 检测两个弹幕是否会碰撞
     * 允许不同类型弹幕的碰撞
     */
    fun willHitInDuration(
        disp: IDisplayer, d1: BaseDanmaku, d2: BaseDanmaku,
        duration: Long, currTime: Long
    ): Boolean {
        val type1 = d1.getType()
        val type2 = d2.getType()
        // 不同类型不碰撞
        if (type1 != type2) return false
        if (d1.isOutside()) return false

        val dTime = d2.getActualTime() - d1.getActualTime()
        if (dTime <= 0) return true
        if (Math.abs(dTime) >= duration || d1.isTimeOut() || d2.isTimeOut()) {
            return false
        }
        if (type1 == BaseDanmaku.TYPE_FIX_TOP || type1 == BaseDanmaku.TYPE_FIX_BOTTOM) {
            return true
        }
        return checkHitAtTime(disp, d1, d2, currTime)
                || checkHitAtTime(disp, d1, d2, d1.getActualTime() + d1.getDuration())
    }

    private fun checkHitAtTime(
        disp: IDisplayer, d1: BaseDanmaku, d2: BaseDanmaku, time: Long
    ): Boolean {
        val rect1 = d1.getRectAtTime(disp, time) ?: return false
        val rect2 = d2.getRectAtTime(disp, time) ?: return false
        return checkHit(d1.getType(), d2.getType(), rect1, rect2)
    }

    private fun checkHit(type1: Int, type2: Int, rect1: FloatArray, rect2: FloatArray): Boolean {
        if (type1 != type2) return false
        if (type1 == BaseDanmaku.TYPE_SCROLL_RL) {
            // hit if left2 < right1
            return rect2[0] < rect1[2]
        }
        if (type1 == BaseDanmaku.TYPE_SCROLL_LR) {
            // hit if right2 > left1
            return rect2[2] > rect1[0]
        }
        return false
    }

    fun isDuplicate(obj1: BaseDanmaku?, obj2: BaseDanmaku?): Boolean {
        if (obj1 === obj2) return false
        if (obj1?.text === obj2?.text) return true
        if (obj1?.text != null && obj1.text == obj2?.text) return true
        return false
    }

    fun compare(obj1: BaseDanmaku?, obj2: BaseDanmaku?): Int {
        if (obj1 === obj2) return 0
        if (obj1 == null) return -1
        if (obj2 == null) return 1

        val `val` = obj1.getTime() - obj2.getTime()
        if (`val` > 0) return 1
        else if (`val` < 0) return -1

        val r = obj1.index - obj2.index
        if (r != 0) return if (r < 0) -1 else 1

        return (obj1.hashCode() - obj2.hashCode()).coerceIn(-1, 1)
    }

    fun isOverSize(disp: IDisplayer, item: BaseDanmaku): Boolean {
        return disp.isHardwareAccelerated
                && (item.paintWidth > disp.maximumCacheWidth || item.paintHeight > disp.maximumCacheHeight)
    }

    fun fillText(danmaku: BaseDanmaku, text: CharSequence?) {
        danmaku.text = text
        if (text.isNullOrEmpty() || !text.toString().contains(BaseDanmaku.DANMAKU_BR_CHAR)) {
            return
        }
        val lines = text.toString().split(BaseDanmaku.DANMAKU_BR_CHAR).toTypedArray()
        if (lines.size > 1) {
            danmaku.lines = lines
        }
    }
}
