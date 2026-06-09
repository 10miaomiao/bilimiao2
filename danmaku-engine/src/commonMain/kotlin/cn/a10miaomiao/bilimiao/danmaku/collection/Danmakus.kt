package cn.a10miaomiao.bilimiao.danmaku.collection

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.Danmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import java.util.LinkedList
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger

/**
 * 弹幕集合实现
 */
class Danmakus : IDanmakus {

    var items: MutableCollection<BaseDanmaku>? = null
        private set

    private var subItems: Danmakus? = null
    private var startItem: BaseDanmaku? = null
    private var endItem: BaseDanmaku? = null
    private var startSubItem: BaseDanmaku? = null
    private var endSubItem: BaseDanmaku? = null

    private val mSize = AtomicInteger(0)
    private var mSortType: Int = IDanmakus.ST_BY_TIME
    private var mComparator: IDanmakus.BaseComparator? = null
    private var mDuplicateMergingEnabled: Boolean = false
    private val mLockObject = Any()

    constructor() : this(IDanmakus.ST_BY_TIME, false)

    constructor(sortType: Int) : this(sortType, false)

    constructor(sortType: Int, duplicateMergingEnabled: Boolean) : this(sortType, duplicateMergingEnabled, null)

    constructor(sortType: Int, duplicateMergingEnabled: Boolean, baseComparator: IDanmakus.BaseComparator?) {
        var comparator: IDanmakus.BaseComparator? = null
        if (sortType == IDanmakus.ST_BY_TIME) {
            comparator = baseComparator ?: IDanmakus.TimeComparator(duplicateMergingEnabled)
        } else if (sortType == IDanmakus.ST_BY_YPOS) {
            comparator = IDanmakus.YPosComparator(duplicateMergingEnabled)
        } else if (sortType == IDanmakus.ST_BY_YPOS_DESC) {
            comparator = IDanmakus.YPosDescComparator(duplicateMergingEnabled)
        }
        if (sortType == IDanmakus.ST_BY_LIST) {
            items = LinkedList()
        } else {
            mDuplicateMergingEnabled = duplicateMergingEnabled
            comparator!!.setDuplicateMergingEnabled(duplicateMergingEnabled)
            items = TreeSet(comparator)
            mComparator = comparator
        }
        mSortType = sortType
        mSize.set(0)
    }

    constructor(items: MutableCollection<BaseDanmaku>) {
        setItems(items)
    }

    constructor(duplicateMergingEnabled: Boolean) : this(IDanmakus.ST_BY_TIME, duplicateMergingEnabled)

    fun setItems(newItems: Collection<BaseDanmaku>?) {
        var actualItems = newItems
        if (mDuplicateMergingEnabled && mSortType != IDanmakus.ST_BY_LIST) {
            synchronized(mLockObject) {
                this.items?.clear()
                this.items?.addAll(actualItems ?: emptyList())
                actualItems = this.items
            }
        } else {
            this.items = actualItems as? MutableCollection<BaseDanmaku>
        }
        if (actualItems is List<*>) {
            mSortType = IDanmakus.ST_BY_LIST
        }
        mSize.set(actualItems?.size ?: 0)
    }

    override fun addItem(item: BaseDanmaku): Boolean {
        synchronized(mLockObject) {
            if (items != null) {
                try {
                    if (items!!.add(item)) {
                        mSize.incrementAndGet()
                        return true
                    }
                } catch (_: Exception) {
                }
            }
        }
        return false
    }

    override fun removeItem(item: BaseDanmaku): Boolean {
        if (item.isOutside()) {
            item.setVisibility(false)
        }
        synchronized(mLockObject) {
            if (items?.remove(item) == true) {
                mSize.decrementAndGet()
                return true
            }
        }
        return false
    }

    private fun subset(startTime: Long, endTime: Long): Collection<BaseDanmaku>? {
        if (mSortType == IDanmakus.ST_BY_LIST || items == null || items!!.isEmpty()) {
            return null
        }
        if (subItems == null) {
            subItems = Danmakus(mDuplicateMergingEnabled)
        }
        if (startSubItem == null) {
            startSubItem = createItem("start")
        }
        if (endSubItem == null) {
            endSubItem = createItem("end")
        }
        startSubItem!!.setTime(startTime)
        endSubItem!!.setTime(endTime)
        @Suppress("UNCHECKED_CAST")
        return (items as TreeSet<BaseDanmaku>).subSet(startSubItem!!, endSubItem!!)
    }

    override fun subnew(startTime: Long, endTime: Long): IDanmakus? {
        val subset = subset(startTime, endTime) ?: return null
        if (subset.isEmpty()) return null
        return Danmakus(LinkedList(subset))
    }

    override fun sub(startTime: Long, endTime: Long): IDanmakus? {
        if (items == null || items!!.isEmpty()) return null
        if (subItems == null) {
            if (mSortType == IDanmakus.ST_BY_LIST) {
                subItems = Danmakus(IDanmakus.ST_BY_LIST)
                synchronized(mLockObject) {
                    subItems!!.setItems(items!!)
                }
            } else {
                subItems = Danmakus(mDuplicateMergingEnabled)
            }
        }
        if (mSortType == IDanmakus.ST_BY_LIST) {
            return subItems
        }
        if (startItem == null) startItem = createItem("start")
        if (endItem == null) endItem = createItem("end")

        val currentSubItems = subItems
        if (currentSubItems != null) {
            val dtime = startTime - startItem!!.getActualTime()
            if (dtime >= 0 && endTime <= endItem!!.getActualTime()) {
                return currentSubItems
            }
        }

        startItem!!.setTime(startTime)
        endItem!!.setTime(endTime)
        synchronized(mLockObject) {
            @Suppress("UNCHECKED_CAST")
            subItems!!.setItems((items as TreeSet<BaseDanmaku>).subSet(startItem!!, endItem!!))
        }
        return subItems
    }

    private fun createItem(text: String): BaseDanmaku = Danmaku(text)

    override fun size(): Int = mSize.get()

    override fun clear() {
        synchronized(mLockObject) {
            items?.clear()
            mSize.set(0)
        }
        if (subItems != null) {
            subItems = null
            startItem = createItem("start")
            endItem = createItem("end")
        }
    }

    override fun first(): BaseDanmaku? {
        if (items != null && items!!.isNotEmpty()) {
            if (mSortType == IDanmakus.ST_BY_LIST) {
                return (items as LinkedList<BaseDanmaku>).peek()
            }
            return (items as TreeSet<BaseDanmaku>).first()
        }
        return null
    }

    override fun last(): BaseDanmaku? {
        if (items != null && items!!.isNotEmpty()) {
            if (mSortType == IDanmakus.ST_BY_LIST) {
                return (items as LinkedList<BaseDanmaku>).peekLast()
            }
            return (items as TreeSet<BaseDanmaku>).last()
        }
        return null
    }

    override fun contains(item: BaseDanmaku): Boolean {
        return items?.contains(item) == true
    }

    override fun isEmpty(): Boolean {
        return items == null || items!!.isEmpty()
    }

    override fun setSubItemsDuplicateMergingEnabled(enable: Boolean) {
        mDuplicateMergingEnabled = enable
        startItem = null
        endItem = null
        if (subItems == null) {
            subItems = Danmakus(enable)
        }
        subItems!!.mDuplicateMergingEnabled = enable
        subItems!!.mComparator?.setDuplicateMergingEnabled(enable)
    }

    override fun getCollection(): MutableCollection<BaseDanmaku> = items ?: mutableListOf()

    override fun forEachSync(consumer: IDanmakus.Consumer<in BaseDanmaku, *>) {
        synchronized(mLockObject) {
            forEach(consumer)
        }
    }

    override fun forEach(consumer: IDanmakus.Consumer<in BaseDanmaku, *>) {
        consumer.before()
        val it = items?.iterator() ?: return
        while (it.hasNext()) {
            val next = it.next()
            val action = consumer.accept(next)
            when (action) {
                IDanmakus.Consumer.ACTION_BREAK -> break
                IDanmakus.Consumer.ACTION_REMOVE -> {
                    it.remove()
                    mSize.decrementAndGet()
                }
                IDanmakus.Consumer.ACTION_REMOVE_AND_BREAK -> {
                    it.remove()
                    mSize.decrementAndGet()
                    break
                }
            }
        }
        consumer.after()
    }

    override fun obtainSynchronizer(): Any = mLockObject
}
