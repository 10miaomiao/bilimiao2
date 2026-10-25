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

        /** 出场锚定状态：尚未出场 */
        const val RENDER_START_NONE = -1L

        /** 出场锚定状态：播放器时钟越过时已走完存活时间，永久丢弃（不再放出） */
        const val RENDER_START_DISCARDED = Long.MAX_VALUE
    }

    /** 显示时间(毫秒) */
    private var time: Long = 0L

    /** 文本 */
    @JvmField var text: CharSequence? = null

    /** 多行文本 */
    @JvmField var lines: Array<String>? = null

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

    /** 是否是直播弹幕 */
    @JvmField var isLive: Boolean = false

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

    /**
     * 出场锚定时刻（渲染时钟轴，毫秒）
     *
     * 双时钟模型下，"什么时候放出这条弹幕"由播放器时钟（释放时钟）决定，
     * 而"放出之后怎么飞、什么时候消失"由独立的渲染时钟决定：弹幕被放出的
     * 那一刻把当前渲染时钟记在这里，之后位置、超时、透明度动画全部基于
     * `渲染时钟 - renderStartTime` 计算。
     *
     * 这样播放器时间再怎么抖动都只影响出场时刻（几十毫秒，肉眼无感），
     * 不会改变已出场弹幕的运动速度，弹幕动画始终匀速平滑。
     *
     * 取值为 [RENDER_START_NONE] 表示尚未出场，[RENDER_START_DISCARDED] 表示已丢弃。
     */
    @JvmField var renderStartTime: Long = RENDER_START_NONE

    fun getDuration(): Long = duration?.value ?: 0L

    fun setDuration(d: Duration) {
        this.duration = d
    }

    fun draw(displayer: IDisplayer) {
        displayer.draw(this)
    }

    fun isMeasured(): Boolean {
        return paintWidth > -1 && paintHeight > -1
                && measureResetFlag == flags?.MEASURE_RESET_FLAG
    }

    open fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        displayer.measure(this, fromWorkerThread)
        this.measureResetFlag = flags?.MEASURE_RESET_FLAG ?: 0
    }

    open fun isShown(): Boolean {
        return this.visibility == VISIBLE
                && visibleResetFlag == flags?.VISIBLE_RESET_FLAG
    }

    // region 渲染时钟轴（双时钟模型）

    /** 是否已确定出场时刻（可参与渲染） */
    fun isRenderAnchored(): Boolean {
        return renderStartTime >= 0 && renderStartTime != RENDER_START_DISCARDED
    }

    /** 是否已过期丢弃（不再放出） */
    fun isRenderDiscarded(): Boolean = renderStartTime == RENDER_START_DISCARDED

    /**
     * 渲染时钟轴上的出场时刻
     *
     * 没有 fallback：未锚定的弹幕返回 [RENDER_START_NONE]，所有可见性判定都会把它
     * 当作「不该显示」。绝不能退化成用视频时间当锚点——投喂前（启动 / seek 后的
     * 那一帧）视频时间和弹幕时钟根本不同轴，那样会让弹幕凭空出现在屏幕中间。
     */
    fun getRenderStartTime(): Long = renderStartTime

    /** 渲染时钟轴上已经过的时长 */
    fun getRenderElapsed(renderClock: Long): Long = renderClock - renderStartTime

    /** 渲染时钟轴上是否尚未到出场时刻 */
    fun isRenderWaiting(renderClock: Long): Boolean = renderClock < renderStartTime

    /** 渲染时钟轴上是否已超时（已走完存活时间）；未出场不算超时 */
    fun isRenderTimeOut(renderClock: Long): Boolean {
        if (isRenderDiscarded()) return true
        if (!isRenderAnchored()) return false
        return renderClock - renderStartTime >= getDuration()
    }

    /** 渲染时钟轴上是否已超时（使用当前计时器时间） */
    fun isRenderTimeOut(): Boolean {
        return mTimer == null || isRenderTimeOut(mTimer!!.currMillisecond)
    }

    /** 渲染时钟轴上是否不该显示（未出场或已超时） */
    fun isRenderOutside(renderClock: Long): Boolean {
        if (!isRenderAnchored()) return true
        val elapsed = renderClock - renderStartTime
        return elapsed <= 0 || elapsed >= getDuration()
    }

    /** 渲染时钟轴上是否尚未出场或已超时（使用当前计时器时间） */
    fun isRenderOutside(): Boolean {
        return mTimer == null || isRenderOutside(mTimer!!.currMillisecond)
    }

    /**
     * 在渲染时钟轴上锚定出场时刻
     *
     * @param startTime 出场时刻（渲染时钟轴）
     */
    fun anchorRenderStart(startTime: Long) {
        renderStartTime = startTime
    }

    /** 清除出场锚点，等待重新放出（seek 时使用） */
    fun resetRenderStart() {
        renderStartTime = RENDER_START_NONE
    }

    /** 标记为永久丢弃，不再放出 */
    fun discardRenderStart() {
        renderStartTime = RENDER_START_DISCARDED
    }

    // endregion

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

    /** 弹幕在视频时间轴上的时间（投喂依据，也是列表排序依据） */
    fun setTime(t: Long) {
        this.time = t
    }

    fun getTime(): Long = time
}
