package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 弹幕基类
 */
abstract class BaseDanmaku {

    companion object {
        const val DANMAKU_BR_CHAR = "\n"

        const val TYPE_SCROLL_RL = 1
        const val TYPE_SCROLL_LR = 6
        const val TYPE_FIX_TOP = 5
        const val TYPE_FIX_BOTTOM = 4
        const val TYPE_SPECIAL = 7
        const val TYPE_MOVEABLE_XXX = 0

        const val INVISIBLE = 0
        const val VISIBLE = 1

        const val FLAG_REQUEST_REMEASURE = 0x1
        const val FLAG_REQUEST_INVALIDATE = 0x2
    }

    /** 显示时间(毫秒) */
    private var time: Long = 0L

    /** 偏移时间 */
    @JvmField var timeOffset: Long = 0L

    /** 文本 */
    @JvmField var text: CharSequence? = null

    /** 多行文本 */
    @JvmField var lines: Array<String>? = null

    /** 库内部使用的临时引用 */
    @JvmField var obj: Any? = null

    /** 外部自定义数据引用 */
    @JvmField var tag: Any? = null

    /** 文本颜色 */
    @JvmField var textColor: Int = 0

    /** Z轴角度 */
    @JvmField var rotationZ: Float = 0f

    /** Y轴角度 */
    @JvmField var rotationY: Float = 0f

    /** 阴影/描边颜色 */
    @JvmField var textShadowColor: Int = 0

    /** 下划线颜色, 0表示无下划线 */
    @JvmField var underlineColor: Int = 0

    /** 字体大小 */
    @JvmField var textSize: Float = -1f

    /** 框的颜色, 0表示无框 */
    @JvmField var borderColor: Int = 0

    /** 内边距(像素) */
    @JvmField var padding: Int = 0

    /** 弹幕优先级, 0为低优先级, >0为高优先级不会被过滤器过滤 */
    @JvmField var priority: Byte = 0

    /** 占位宽度 */
    @JvmField var paintWidth: Float = -1f

    /** 占位高度 */
    @JvmField var paintHeight: Float = -1f

    /** 存活时间(毫秒) */
    @JvmField var duration: Duration? = null

    /** 索引/编号 */
    @JvmField var index: Int = 0

    /** 是否可见 */
    @JvmField var visibility: Int = INVISIBLE

    /** 重置位 visible */
    private var visibleResetFlag: Int = 0

    /** 重置位 measure */
    @JvmField var measureResetFlag: Int = 0

    /** 重置位 offset time */
    @JvmField var syncTimeOffsetResetFlag: Int = 0

    /** 重置位 prepare */
    @JvmField var prepareResetFlag: Int = -1

    /** 绘制用缓存 */
    @JvmField var cache: IDrawingCache<*>? = null

    /** 是否是直播弹幕 */
    @JvmField var isLive: Boolean = false

    /** 临时, 是否在同线程创建缓存 */
    @JvmField var forceBuildCacheInSameThread: Boolean = false

    /** 弹幕发布者id, 0表示游客 */
    @JvmField var userId: Int = 0

    /** 弹幕发布者hash */
    @JvmField var userHash: String? = null

    /** 是否游客 */
    @JvmField var isGuest: Boolean = false

    /** 计时器 */
    @JvmField protected var mTimer: DanmakuTimer? = null

    /** 透明度 */
    @JvmField protected var alpha: Int = AlphaValue.MAX

    @JvmField var mFilterParam: Int = 0
    @JvmField var filterResetFlag: Int = -1
    @JvmField var flags: GlobalFlagValues? = null
    @JvmField var requestFlags: Int = 0

    /** 标记是否首次显示 */
    @JvmField var firstShownFlag: Int = -1

    private val mTags = mutableMapOf<Int, Any?>()

    fun getDuration(): Long = duration?.value ?: 0L

    fun setDuration(d: Duration) {
        this.duration = d
    }

    fun draw(displayer: IDisplayer): Int {
        return displayer.draw(this)
    }

    fun isMeasured(): Boolean {
        return paintWidth > -1 && paintHeight > -1
                && measureResetFlag == flags?.MEASURE_RESET_FLAG
    }

    open fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        displayer.measure(this, fromWorkerThread)
        this.measureResetFlag = flags?.MEASURE_RESET_FLAG ?: 0
    }

    fun isPrepared(): Boolean {
        return this.prepareResetFlag == flags?.PREPARE_RESET_FLAG
    }

    fun prepare(displayer: IDisplayer, fromWorkerThread: Boolean) {
        displayer.prepare(this, fromWorkerThread)
        this.prepareResetFlag = flags?.PREPARE_RESET_FLAG ?: 0
    }

    fun getDrawingCache(): IDrawingCache<*>? = cache

    open fun isShown(): Boolean {
        return this.visibility == VISIBLE
                && visibleResetFlag == flags?.VISIBLE_RESET_FLAG
    }

    fun isTimeOut(): Boolean {
        return mTimer == null || isTimeOut(mTimer!!.currMillisecond)
    }

    fun isTimeOut(ctime: Long): Boolean {
        return ctime - getActualTime() >= (duration?.value ?: 0L)
    }

    fun isOutside(): Boolean {
        return mTimer == null || isOutside(mTimer!!.currMillisecond)
    }

    fun isOutside(ctime: Long): Boolean {
        val dtime = ctime - getActualTime()
        return dtime <= 0 || dtime >= (duration?.value ?: 0L)
    }

    fun isLate(): Boolean {
        return mTimer == null || mTimer!!.currMillisecond < getActualTime()
    }

    fun hasPassedFilter(): Boolean {
        if (filterResetFlag != flags?.FILTER_RESET_FLAG) {
            mFilterParam = 0
            return false
        }
        return true
    }

    fun isFiltered(): Boolean {
        return filterResetFlag == flags?.FILTER_RESET_FLAG && mFilterParam != 0
    }

    fun isFilteredBy(flag: Int): Boolean {
        return filterResetFlag == flags?.FILTER_RESET_FLAG && (mFilterParam and flag) == flag
    }

    fun setVisibility(b: Boolean) {
        if (b) {
            this.visibleResetFlag = flags?.VISIBLE_RESET_FLAG ?: 0
            this.visibility = VISIBLE
        } else {
            this.visibility = INVISIBLE
        }
    }

    abstract fun layout(displayer: IDisplayer, x: Float, y: Float)
    abstract fun getRectAtTime(displayer: IDisplayer, currTime: Long): FloatArray?
    abstract fun getLeft(): Float
    abstract fun getTop(): Float
    abstract fun getRight(): Float
    abstract fun getBottom(): Float
    abstract fun getType(): Int

    fun getTimer(): DanmakuTimer? = mTimer
    fun setTimer(timer: DanmakuTimer?) { mTimer = timer }
    fun getAlpha(): Int = alpha

    fun setTag(tag: Any?) { this.tag = tag }
    fun setTag(key: Int, tag: Any?) { mTags[key] = tag }
    fun getTag(key: Int): Any? = mTags[key]

    fun setTimeOffset(offset: Long) {
        this.timeOffset = offset
        this.syncTimeOffsetResetFlag = flags?.SYNC_TIME_OFFSET_RESET_FLAG ?: 0
    }

    fun setTime(t: Long) {
        this.time = t
        this.timeOffset = 0
    }

    fun getTime(): Long = time

    fun getActualTime(): Long {
        if (flags == null || flags!!.SYNC_TIME_OFFSET_RESET_FLAG != this.syncTimeOffsetResetFlag) {
            this.timeOffset = 0
            return time
        }
        return time + timeOffset
    }

    fun isOffset(): Boolean {
        if (flags == null || flags!!.SYNC_TIME_OFFSET_RESET_FLAG != this.syncTimeOffsetResetFlag) {
            this.timeOffset = 0
            return false
        }
        return timeOffset != 0L
    }
}
