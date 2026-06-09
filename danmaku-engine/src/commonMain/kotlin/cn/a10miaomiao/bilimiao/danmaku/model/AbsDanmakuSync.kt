package cn.a10miaomiao.bilimiao.danmaku.model

abstract class AbsDanmakuSync {

    companion object {
        const val SYNC_STATE_HALT = 1
        const val SYNC_STATE_PLAYING = 2
    }

    abstract fun getUptimeMillis(): Long
    abstract fun getSyncState(): Int

    open fun getThresholdTimeMills(): Long = 1500L
    open fun isSyncPlayingState(): Boolean = false
}
