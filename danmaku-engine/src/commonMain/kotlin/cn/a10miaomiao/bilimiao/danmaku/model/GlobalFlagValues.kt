package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 全局重置标记
 *
 * 弹幕上缓存了某个标记值，标记递增后该弹幕需要重新测量/重排/重新过滤。
 */
class GlobalFlagValues {
    @JvmField var MEASURE_RESET_FLAG = 0
    @JvmField var VISIBLE_RESET_FLAG = 0
    @JvmField var FILTER_RESET_FLAG = 0

    fun resetAll() {
        VISIBLE_RESET_FLAG = 0
        MEASURE_RESET_FLAG = 0
        FILTER_RESET_FLAG = 0
    }

    fun updateAll() {
        VISIBLE_RESET_FLAG++
        MEASURE_RESET_FLAG++
        FILTER_RESET_FLAG++
    }

    fun updateVisibleFlag() { VISIBLE_RESET_FLAG++ }
    fun updateMeasureFlag() { MEASURE_RESET_FLAG++ }
    fun updateFilterFlag() { FILTER_RESET_FLAG++ }
}
