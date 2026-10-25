package cn.a10miaomiao.bilimiao.danmaku.model

import cn.a10miaomiao.bilimiao.danmaku.util.DanmakuUtils

/**
 * 弹幕集合接口
 */
interface IDanmakus {

    abstract class Consumer<Progress, Result> {
        companion object {
            const val ACTION_CONTINUE = 0
            const val ACTION_BREAK = 1
            const val ACTION_REMOVE = 2
            const val ACTION_REMOVE_AND_BREAK = 3
        }

        abstract fun accept(t: Progress): Int
        open fun before() {}
        open fun after() {}
        open fun result(): Result? = null
    }

    abstract class DefaultConsumer<Progress> : Consumer<Progress, Unit>()

    companion object {
        const val ST_BY_TIME = 0
        const val ST_BY_YPOS = 1
        const val ST_BY_YPOS_DESC = 2
        const val ST_BY_LIST = 4
    }

    fun addItem(item: BaseDanmaku): Boolean
    fun removeItem(item: BaseDanmaku): Boolean
    fun sub(startTime: Long, endTime: Long): IDanmakus?
    fun size(): Int
    fun clear()
    fun first(): BaseDanmaku?
    fun last(): BaseDanmaku?
    fun contains(item: BaseDanmaku): Boolean
    fun isEmpty(): Boolean
    fun setSubItemsDuplicateMergingEnabled(enable: Boolean)
    fun getCollection(): Collection<BaseDanmaku>
    fun forEachSync(consumer: Consumer<in BaseDanmaku, *>)
    fun forEach(consumer: Consumer<in BaseDanmaku, *>)
    fun obtainSynchronizer(): Any

    open class BaseComparator(
        protected var mDuplicateMergingEnable: Boolean
    ) : Comparator<BaseDanmaku> {

        fun setDuplicateMergingEnabled(enable: Boolean) {
            mDuplicateMergingEnable = enable
        }

        override fun compare(obj1: BaseDanmaku, obj2: BaseDanmaku): Int {
            if (mDuplicateMergingEnable && DanmakuUtils.isDuplicate(obj1, obj2)) {
                return 0
            }
            return DanmakuUtils.compare(obj1, obj2)
        }
    }

    class TimeComparator(duplicateMergingEnabled: Boolean) : BaseComparator(duplicateMergingEnabled) {
        override fun compare(obj1: BaseDanmaku, obj2: BaseDanmaku): Int {
            return super.compare(obj1, obj2)
        }
    }

    class YPosComparator(duplicateMergingEnabled: Boolean) : BaseComparator(duplicateMergingEnabled) {
        override fun compare(obj1: BaseDanmaku, obj2: BaseDanmaku): Int {
            if (mDuplicateMergingEnable && DanmakuUtils.isDuplicate(obj1, obj2)) {
                return 0
            }
            val result = obj1.getTop().compareTo(obj2.getTop())
            if (result != 0) return result
            // y 相同的两条弹幕不能返回 0：轨道集合是 TreeSet，
            // compare 返回 0 会被当成重复元素直接丢掉，导致这条弹幕下次
            // 又要重新分配轨道（表现为上下跳行）。用时间+编号兜底保证稳定且唯一。
            return DanmakuUtils.compare(obj1, obj2)
        }
    }

    class YPosDescComparator(duplicateMergingEnabled: Boolean) : BaseComparator(duplicateMergingEnabled) {
        override fun compare(obj1: BaseDanmaku, obj2: BaseDanmaku): Int {
            if (mDuplicateMergingEnable && DanmakuUtils.isDuplicate(obj1, obj2)) {
                return 0
            }
            val result = obj2.getTop().compareTo(obj1.getTop())
            if (result != 0) return result
            return DanmakuUtils.compare(obj1, obj2)
        }
    }
}
