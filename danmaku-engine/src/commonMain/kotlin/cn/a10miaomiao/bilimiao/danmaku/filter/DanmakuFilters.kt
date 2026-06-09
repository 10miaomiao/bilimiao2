package cn.a10miaomiao.bilimiao.danmaku.filter

import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.Duration
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.platform.PlatformClock
import java.util.LinkedHashMap
import java.util.TreeMap

/**
 * 弹幕过滤器管理
 */
class DanmakuFilters {

    companion object {
        const val FILTER_TYPE_TYPE = 1
        const val FILTER_TYPE_QUANTITY = 2
        const val FILTER_TYPE_ELAPSED_TIME = 4
        const val FILTER_TYPE_TEXTCOLOR = 8
        const val FILTER_TYPE_USER_ID = 16
        const val FILTER_TYPE_USER_HASH = 32
        const val FILTER_TYPE_USER_GUEST = 64
        const val FILTER_TYPE_DUPLICATE_MERGE = 128
        const val FILTER_TYPE_MAXIMUM_LINES = 256
        const val FILTER_TYPE_OVERLAPPING = 512

        const val TAG_TYPE_DANMAKU_FILTER = "1010_Filter"
        const val TAG_QUANTITY_DANMAKU_FILTER = "1011_Filter"
        const val TAG_ELAPSED_TIME_FILTER = "1012_Filter"
        const val TAG_TEXT_COLOR_DANMAKU_FILTER = "1013_Filter"
        const val TAG_USER_ID_FILTER = "1014_Filter"
        const val TAG_USER_HASH_FILTER = "1015_Filter"
        const val TAG_GUEST_FILTER = "1016_Filter"
        const val TAG_DUPLICATE_FILTER = "1017_Filter"
        const val TAG_MAXIMUN_LINES_FILTER = "1018_Filter"
        const val TAG_OVERLAPPING_FILTER = "1019_Filter"
        const val TAG_PRIMARY_CUSTOM_FILTER = "2000_Primary_Custom_Filter"
    }

    /**
     * 弹幕过滤器接口
     */
    interface IDanmakuFilter<T> {
        /**
         * 是否过滤该弹幕
         */
        fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                   timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean

        fun setData(data: T?)
        fun reset()
        fun clear()
    }

    /**
     * 弹幕过滤器基类
     */
    abstract class BaseDanmakuFilter<T> : IDanmakuFilter<T> {
        override fun clear() {}
    }

    // ==================== 具体过滤器实现 ====================

    /**
     * 根据弹幕类型过滤
     */
    class TypeDanmakuFilter : BaseDanmakuFilter<List<Int>>() {

        private val mFilterTypes: MutableList<Int> = java.util.Collections.synchronizedList(mutableListOf<Int>())

        fun enableType(type: Int) {
            if (!mFilterTypes.contains(type)) {
                mFilterTypes.add(type)
            }
        }

        fun disableType(type: Int) {
            mFilterTypes.remove(type)
        }

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = mFilterTypes.contains(danmaku.getType())
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_TYPE
            }
            return filtered
        }

        override fun setData(data: List<Int>?) {
            reset()
            if (data != null) {
                for (i in data) {
                    enableType(i)
                }
            }
        }

        override fun reset() {
            mFilterTypes.clear()
        }
    }

    /**
     * 根据同屏数量过滤弹幕
     */
    class QuantityDanmakuFilter : BaseDanmakuFilter<Int>() {

        private var mMaximumSize = -1
        private var mLastSkipped: BaseDanmaku? = null
        private var mFilterFactor = 1f

        private fun needFilter(danmaku: BaseDanmaku, orderInScreen: Int,
                               totalSizeInScreen: Int, timer: DanmakuTimer?,
                               fromCachingTask: Boolean, context: DanmakuContext): Boolean {
            if (mMaximumSize <= 0 || danmaku.getType() != BaseDanmaku.TYPE_SCROLL_RL) {
                return false
            }

            if (mLastSkipped == null || mLastSkipped!!.isTimeOut()) {
                mLastSkipped = danmaku
                return false
            }

            val gapTime = danmaku.getActualTime() - mLastSkipped!!.getActualTime()
            val maximumScrollDuration: Duration? = context.mDanmakuFactory.MAX_Duration_Scroll_Danmaku
            if (gapTime >= 0 && maximumScrollDuration != null
                && gapTime < (maximumScrollDuration.value * mFilterFactor)
            ) {
                return true
            }

            if (orderInScreen > mMaximumSize) {
                return true
            }
            mLastSkipped = danmaku
            return false
        }

        @Synchronized
        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = needFilter(danmaku, index, totalsizeInScreen, timer, fromCachingTask, config)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_QUANTITY
            }
            return filtered
        }

        override fun setData(data: Int?) {
            reset()
            if (data == null) return
            if (data != mMaximumSize) {
                mMaximumSize = data + data / 5
                mFilterFactor = 1f / mMaximumSize.toFloat()
            }
        }

        @Synchronized
        override fun reset() {
            mLastSkipped = null
        }

        override fun clear() {
            reset()
        }
    }

    /**
     * 根据绘制耗时过滤弹幕
     */
    class ElapsedTimeFilter : BaseDanmakuFilter<Any>() {

        private val mMaxTime = 20L // 绘制超过20ms就跳过，默认保持接近50fps

        @Synchronized
        private fun needFilter(danmaku: BaseDanmaku, orderInScreen: Int,
                               totalsizeInScreen: Int, timer: DanmakuTimer?,
                               fromCachingTask: Boolean): Boolean {
            if (timer == null || !danmaku.isOutside()) {
                return false
            }

            val elapsedTime = PlatformClock.uptimeMillis() - timer.currMillisecond
            return elapsedTime >= mMaxTime
        }

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = needFilter(danmaku, index, totalsizeInScreen, timer, fromCachingTask)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_ELAPSED_TIME
            }
            return filtered
        }

        override fun setData(data: Any?) {
            reset()
        }

        @Synchronized
        override fun reset() {}

        override fun clear() {
            reset()
        }
    }

    /**
     * 根据文本颜色白名单过滤
     */
    class TextColorFilter : BaseDanmakuFilter<List<Int>>() {

        val mWhiteList: MutableList<Int> = mutableListOf()

        private fun addToWhiteList(color: Int) {
            if (!mWhiteList.contains(color)) {
                mWhiteList.add(color)
            }
        }

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = !mWhiteList.contains(danmaku.textColor)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_TEXTCOLOR
            }
            return filtered
        }

        override fun setData(data: List<Int>?) {
            reset()
            if (data != null) {
                for (i in data) {
                    addToWhiteList(i)
                }
            }
        }

        override fun reset() {
            mWhiteList.clear()
        }
    }

    /**
     * 根据用户标识黑名单过滤
     */
    abstract class UserFilter<T> : BaseDanmakuFilter<List<T>>() {

        val mBlackList: MutableList<T> = mutableListOf()

        private fun addToBlackList(id: T) {
            if (!mBlackList.contains(id)) {
                mBlackList.add(id)
            }
        }

        override fun setData(data: List<T>?) {
            reset()
            if (data != null) {
                for (i in data) {
                    addToBlackList(i)
                }
            }
        }

        override fun reset() {
            mBlackList.clear()
        }
    }

    /**
     * 根据用户Id黑名单过滤
     */
    class UserIdFilter : UserFilter<Int>() {

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = mBlackList.contains(danmaku.userId)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_USER_ID
            }
            return filtered
        }
    }

    /**
     * 根据用户hash黑名单过滤
     */
    class UserHashFilter : UserFilter<String>() {

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = danmaku.userHash != null && mBlackList.contains(danmaku.userHash)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_USER_HASH
            }
            return filtered
        }
    }

    /**
     * 屏蔽游客弹幕
     */
    class GuestFilter : BaseDanmakuFilter<Boolean>() {

        private var mBlock = false

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = mBlock && danmaku.isGuest
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_USER_GUEST
            }
            return filtered
        }

        override fun setData(data: Boolean?) {
            mBlock = data ?: false
        }

        override fun reset() {
            mBlock = false
        }
    }

    /**
     * 合并重复弹幕过滤器
     */
    class DuplicateMergingFilter : BaseDanmakuFilter<Unit>() {

        private val blockedDanmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_LIST)
        private val currentDanmakus: LinkedHashMap<String, BaseDanmaku> = LinkedHashMap()
        private val passedDanmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_LIST)

        private fun removeTimeoutDanmakus(danmakus: IDanmakus, limitTime: Long) {
            val startTime = PlatformClock.uptimeMillis()
            danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
                override fun accept(t: BaseDanmaku): Int {
                    return try {
                        if (PlatformClock.uptimeMillis() - startTime > limitTime) {
                            ACTION_BREAK
                        } else if (t.isTimeOut()) {
                            ACTION_REMOVE
                        } else {
                            ACTION_BREAK
                        }
                    } catch (e: Exception) {
                        ACTION_BREAK
                    }
                }
            })
        }

        private fun removeTimeoutDanmakus(danmakus: LinkedHashMap<String, BaseDanmaku>, limitTime: Int) {
            val it = danmakus.entries.iterator()
            val startTime = PlatformClock.uptimeMillis()
            while (it.hasNext()) {
                try {
                    val entry = it.next()
                    val item = entry.value
                    if (item.isTimeOut()) {
                        it.remove()
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    break
                }
                if (PlatformClock.uptimeMillis() - startTime > limitTime) {
                    break
                }
            }
        }

        @Synchronized
        fun needFilter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                       timer: DanmakuTimer?, fromCachingTask: Boolean): Boolean {
            removeTimeoutDanmakus(blockedDanmakus, 2)
            removeTimeoutDanmakus(passedDanmakus, 2)
            removeTimeoutDanmakus(currentDanmakus, 3)
            if (blockedDanmakus.contains(danmaku) && !danmaku.isOutside()) {
                return true
            }
            if (passedDanmakus.contains(danmaku)) {
                return false
            }
            val text = danmaku.text?.toString()
            if (currentDanmakus.containsKey(text)) {
                currentDanmakus[text!!] = danmaku
                blockedDanmakus.removeItem(danmaku)
                blockedDanmakus.addItem(danmaku)
                return true
            } else {
                if (text != null) {
                    currentDanmakus[text] = danmaku
                }
                passedDanmakus.addItem(danmaku)
                return false
            }
        }

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            val filtered = needFilter(danmaku, index, totalsizeInScreen, timer, fromCachingTask)
            if (filtered) {
                danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_DUPLICATE_MERGE
            }
            return filtered
        }

        override fun setData(data: Unit?) {}

        @Synchronized
        override fun reset() {
            passedDanmakus.clear()
            blockedDanmakus.clear()
            currentDanmakus.clear()
        }

        override fun clear() {
            reset()
        }
    }

    /**
     * 最大行数过滤器
     */
    class MaximumLinesFilter : BaseDanmakuFilter<Map<Int, Int>>() {

        private var mMaximumLinesPairs: Map<Int, Int>? = null

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            var filtered = false
            if (mMaximumLinesPairs != null) {
                val maxLines = mMaximumLinesPairs!![danmaku.getType()]
                filtered = (maxLines != null && index >= maxLines)
                if (filtered) {
                    danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_MAXIMUM_LINES
                }
            }
            return filtered
        }

        override fun setData(data: Map<Int, Int>?) {
            mMaximumLinesPairs = data
        }

        override fun reset() {
            mMaximumLinesPairs = null
        }
    }

    /**
     * 重叠过滤器
     */
    class OverlappingFilter : BaseDanmakuFilter<Map<Int, Boolean>>() {

        private var mEnabledPairs: Map<Int, Boolean>? = null

        override fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                            timer: DanmakuTimer?, fromCachingTask: Boolean, config: DanmakuContext): Boolean {
            var filtered = false
            if (mEnabledPairs != null) {
                val enabledValue = mEnabledPairs!![danmaku.getType()]
                filtered = enabledValue != null && enabledValue && fromCachingTask
                if (filtered) {
                    danmaku.mFilterParam = danmaku.mFilterParam or FILTER_TYPE_OVERLAPPING
                }
            }
            return filtered
        }

        override fun setData(data: Map<Int, Boolean>?) {
            mEnabledPairs = data
        }

        override fun reset() {
            mEnabledPairs = null
        }
    }

    // ==================== 过滤器管理 ====================

    val filterException = Exception("not support this filter tag")

    private val filters: MutableMap<String, IDanmakuFilter<*>> =
        java.util.Collections.synchronizedSortedMap(TreeMap())
    private val filtersSecondary: MutableMap<String, IDanmakuFilter<*>> =
        java.util.Collections.synchronizedSortedMap(TreeMap())

    private var mFilterArray: Array<IDanmakuFilter<*>?> = arrayOfNulls(0)
    private var mFilterArraySecondary: Array<IDanmakuFilter<*>?> = arrayOfNulls(0)

    fun get(tag: String): IDanmakuFilter<*> = get(tag, true)

    fun get(tag: String, primary: Boolean): IDanmakuFilter<*> {
        val f = if (primary) filters[tag] else filtersSecondary[tag]
        return f ?: registerFilter(tag, primary)
    }

    fun registerFilter(tag: String): IDanmakuFilter<*> = registerFilter(tag, true)

    fun registerFilter(tag: String, primary: Boolean): IDanmakuFilter<*> {
        var filter = filters[tag]
        if (filter == null) {
            filter = when (tag) {
                TAG_TYPE_DANMAKU_FILTER -> TypeDanmakuFilter()
                TAG_QUANTITY_DANMAKU_FILTER -> QuantityDanmakuFilter()
                TAG_ELAPSED_TIME_FILTER -> ElapsedTimeFilter()
                TAG_TEXT_COLOR_DANMAKU_FILTER -> TextColorFilter()
                TAG_USER_ID_FILTER -> UserIdFilter()
                TAG_USER_HASH_FILTER -> UserHashFilter()
                TAG_GUEST_FILTER -> GuestFilter()
                TAG_DUPLICATE_FILTER -> DuplicateMergingFilter()
                TAG_MAXIMUN_LINES_FILTER -> MaximumLinesFilter()
                TAG_OVERLAPPING_FILTER -> OverlappingFilter()
                else -> throwFilterException()
            }
        }
        if (filter == null) {
            throwFilterException()
            throw filterException
        }
        filter.setData(null)
        if (primary) {
            filters[tag] = filter
            mFilterArray = filters.values.toTypedArray()
        } else {
            filtersSecondary[tag] = filter
            mFilterArraySecondary = filtersSecondary.values.toTypedArray()
        }
        return filter
    }

    fun registerFilter(filter: BaseDanmakuFilter<*>) {
        filters["${TAG_PRIMARY_CUSTOM_FILTER}_${filter.hashCode()}"] = filter
        mFilterArray = filters.values.toTypedArray()
    }

    fun unregisterFilter(tag: String) {
        unregisterFilter(tag, true)
    }

    fun unregisterFilter(tag: String, primary: Boolean) {
        val f = if (primary) filters.remove(tag) else filtersSecondary.remove(tag)
        if (f != null) {
            f.clear()
            if (primary) {
                mFilterArray = filters.values.toTypedArray()
            } else {
                mFilterArraySecondary = filtersSecondary.values.toTypedArray()
            }
        }
    }

    fun unregisterFilter(filter: BaseDanmakuFilter<*>) {
        filters.remove("${TAG_PRIMARY_CUSTOM_FILTER}_${filter.hashCode()}")
        mFilterArray = filters.values.toTypedArray()
    }

    /**
     * 运行所有主过滤器，第一个匹配的设置 mFilterParam 位掩码
     */
    fun filter(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
               timer: DanmakuTimer?, fromCachingTask: Boolean, context: DanmakuContext) {
        for (f in mFilterArray) {
            if (f != null) {
                val filtered = f.filter(danmaku, index, totalsizeInScreen, timer, fromCachingTask, context)
                danmaku.filterResetFlag = context.mGlobalFlagValues.FILTER_RESET_FLAG
                if (filtered) {
                    break
                }
            }
        }
    }

    /**
     * 运行所有次级过滤器
     */
    fun filterSecondary(danmaku: BaseDanmaku, index: Int, totalsizeInScreen: Int,
                        timer: DanmakuTimer?, fromCachingTask: Boolean, context: DanmakuContext): Boolean {
        for (f in mFilterArraySecondary) {
            if (f != null) {
                val filtered = f.filter(danmaku, index, totalsizeInScreen, timer, fromCachingTask, context)
                danmaku.filterResetFlag = context.mGlobalFlagValues.FILTER_RESET_FLAG
                if (filtered) {
                    return true
                }
            }
        }
        return false
    }

    fun clear() {
        for (f in mFilterArray) {
            f?.clear()
        }
        for (f in mFilterArraySecondary) {
            f?.clear()
        }
    }

    fun reset() {
        for (f in mFilterArray) {
            f?.reset()
        }
        for (f in mFilterArraySecondary) {
            f?.reset()
        }
    }

    fun release() {
        clear()
        filters.clear()
        mFilterArray = arrayOfNulls(0)
        filtersSecondary.clear()
        mFilterArraySecondary = arrayOfNulls(0)
    }

    private fun throwFilterException(): Nothing {
        throw filterException
    }
}
