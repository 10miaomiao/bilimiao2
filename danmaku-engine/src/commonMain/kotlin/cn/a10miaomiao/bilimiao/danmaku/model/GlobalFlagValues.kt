package cn.a10miaomiao.bilimiao.danmaku.model

class GlobalFlagValues {
    @JvmField var MEASURE_RESET_FLAG = 0
    @JvmField var VISIBLE_RESET_FLAG = 0
    @JvmField var FILTER_RESET_FLAG = 0
    @JvmField var FIRST_SHOWN_RESET_FLAG = 0
    @JvmField var SYNC_TIME_OFFSET_RESET_FLAG = 0
    @JvmField var PREPARE_RESET_FLAG = 0

    fun resetAll() {
        VISIBLE_RESET_FLAG = 0
        MEASURE_RESET_FLAG = 0
        FILTER_RESET_FLAG = 0
        FIRST_SHOWN_RESET_FLAG = 0
        SYNC_TIME_OFFSET_RESET_FLAG = 0
        PREPARE_RESET_FLAG = 0
    }

    fun updateAll() {
        VISIBLE_RESET_FLAG++
        MEASURE_RESET_FLAG++
        FILTER_RESET_FLAG++
        FIRST_SHOWN_RESET_FLAG++
        SYNC_TIME_OFFSET_RESET_FLAG++
        PREPARE_RESET_FLAG++
    }

    fun updateVisibleFlag() { VISIBLE_RESET_FLAG++ }
    fun updateMeasureFlag() { MEASURE_RESET_FLAG++ }
    fun updateFilterFlag() { FILTER_RESET_FLAG++ }
    fun updateFirstShownFlag() { FIRST_SHOWN_RESET_FLAG++ }
    fun updateSyncOffsetTimeFlag() { SYNC_TIME_OFFSET_RESET_FLAG++ }
    fun updatePrepareFlag() { PREPARE_RESET_FLAG++ }
}
