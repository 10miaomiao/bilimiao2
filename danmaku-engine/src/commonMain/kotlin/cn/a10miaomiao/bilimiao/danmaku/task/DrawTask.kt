package cn.a10miaomiao.bilimiao.danmaku.task

import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.filter.DanmakuFilters
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.PlatformClock
import cn.a10miaomiao.bilimiao.danmaku.renderer.DanmakuRenderer
import cn.a10miaomiao.bilimiao.danmaku.renderer.IRenderer
import cn.a10miaomiao.bilimiao.danmaku.renderer.RenderingState

/**
 * 弹幕绘制任务
 *
 * 核心渲染循环的实现。负责：
 * - 管理弹幕数据列表和屏幕显示窗口
 * - 协调解析器加载弹幕数据
 * - 通过渲染器绘制可见弹幕
 * - 处理播放状态变更、跳转、同步等操作
 *
 * @param timer 弹幕计时器
 * @param context 弹幕上下文配置
 * @param taskListener 任务监听器
 */
open class DrawTask(
    timer: DanmakuTimer,
    protected val mContext: DanmakuContext,
    protected var mTaskListener: IDrawTask.TaskListener?
) : IDrawTask {

    companion object {
        /**
         * 可见窗口相对释放时钟向后多留的余量（毫秒）
         *
         * 窗口起点按 `释放时钟 - 弹幕最大存活时间 - 余量` 计算，用于覆盖
         * 「播放器时钟已走过、但渲染时钟还在飞」的那一段弹幕。
         */
        private const val RELEASE_WINDOW_MARGIN = 1000L

        /**
         * 投喂时向前推的最大时长（毫秒）
         *
         * 正常时一批弹幕的墙钟跨度约 200ms（等于播放器位置采样间隔），
         * 卡顿时可能拉到数秒；截断到一小段，让这批弹幕贴着右边缘依次滑入，
         * 而不是整体迟到很久才出现。
         */
        private const val MAX_FEED_AHEAD = 400L

        /** 窗口回溯放大倍速的上限，避免极端倍速下窗口过大 */
        private const val MAX_WINDOW_SPEED_FACTOR = 8f

        /** 投喂映射时允许的最小倍速，防止除零或极端放大 */
        private const val MIN_FEED_SPEED = 0.25f
    }

    /**
     * 本次投喂区间的终点（播放器时间轴）= 当前读到的播放器位置
     *
     * 播放器位置在这里不承担任何计时职责，只是描述「这次采样覆盖了哪一段」。
     */
    override var releaseClock: Long = 0L

    /** 本次投喂区间的起点（播放器时间轴）= 上一次读到的播放器位置 */
    override var releaseFromClock: Long = 0L

    /**
     * 播放倍速：渲染时钟不跟倍速走，所以一条弹幕在屏幕上停留期间，
     * 释放时钟会前进 `duration * 倍速`。窗口回溯量必须按倍速放大，
     * 否则这些还在飞的弹幕会被挤出窗口、提前消失。
     */
    override var speedFactor: Float = 1f

    /** 显示器 */
    protected val mDisp: IDisplayer = mContext.mDisplayer

    /** 全局弹幕列表（按时间排序） */
    protected var danmakuList: IDanmakus? = null

    /** 弹幕解析器 */
    protected var mParser: BaseDanmakuParser? = null

    /** 弹幕渲染器 */
    val mRenderer: IRenderer = DanmakuRenderer(mContext)

    /** 弹幕计时器 */
    var mTimer: DanmakuTimer = timer
        protected set

    /** 当前屏幕显示窗口的弹幕列表 */
    private var danmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_LIST)

    /** 直播弹幕列表 */
    private val mLiveDanmakus: Danmakus = Danmakus(IDanmakus.ST_BY_LIST)

    /** 上次屏幕窗口的起始时间 */
    private var mLastBeginMills: Long = 0L

    /** 上次屏幕窗口的结束时间 */
    private var mLastEndMills: Long = 0L

    /** 渲染状态 */
    private val mRenderingState = RenderingState()

    /** 清除位置保留器标志 */
    protected var clearRetainerFlag: Boolean = false

    /** 准备就绪状态 */
    protected var mReadyState: Boolean = false

    /** 配置变更回调 */
    private val mConfigChangedCallback = object : DanmakuContext.ConfigChangedCallback {
        override fun onDanmakuConfigChanged(
            config: DanmakuContext,
            tag: DanmakuContext.DanmakuConfigTag,
            vararg values: Any?
        ): Boolean {
            return this@DrawTask.onDanmakuConfigChanged(config, tag, values)
        }
    }

    init {
        mRenderer.setVerifierEnabled(
            mContext.isPreventOverlappingEnabled() || mContext.isMaxLinesLimited()
        )
        initTimer(timer)

        // 注册重复合并过滤器
        val enable = mContext.isDuplicateMergingEnabled()
        if (enable != null) {
            if (enable) {
                mContext.mDanmakuFilters.registerFilter(DanmakuFilters.TAG_DUPLICATE_FILTER)
            } else {
                mContext.mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_DUPLICATE_FILTER)
            }
        }
    }

    /**
     * 初始化计时器
     */
    protected open fun initTimer(timer: DanmakuTimer) {
        mTimer = timer
    }

    @Synchronized
    override fun addDanmaku(item: BaseDanmaku) {
        val list = danmakuList ?: return
        if (item.isLive) {
            mLiveDanmakus.addItem(item)
            removeUnusedLiveDanmakusIn(10)
        }
        item.index = list.size()
        var subAdded = true
        if (mLastBeginMills <= item.getTime() && item.getTime() <= mLastEndMills) {
            synchronized(danmakus) {
                subAdded = danmakus.addItem(item)
            }
        } else if (item.isLive) {
            subAdded = false
        }
        var added = false
        synchronized(list) {
            added = list.addItem(item)
        }
        if (!subAdded || !added) {
            mLastBeginMills = 0L
            mLastEndMills = 0L
        }
    }

    /**
     * 弹幕被移除时的回调，子类可覆写（如 CacheManagingDrawTask）
     */
    protected open fun onDanmakuRemoved(danmaku: BaseDanmaku) {
        // 子类可覆写
    }

    /**
     * 移除超时的直播弹幕
     *
     * @param msec 最大处理时间（毫秒），避免阻塞过久
     */
    @Synchronized
    protected fun removeUnusedLiveDanmakusIn(msec: Int) {
        val list = danmakuList ?: return
        if (list.isEmpty() || mLiveDanmakus.isEmpty()) return
        mLiveDanmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            private val startTime = PlatformClock.uptimeMillis()

            override fun accept(t: BaseDanmaku): Int {
                val isTimeout = t.isRenderTimeOut()
                if (PlatformClock.uptimeMillis() - startTime > msec) {
                    return ACTION_BREAK
                }
                if (isTimeout) {
                    list.removeItem(t)
                    onDanmakuRemoved(t)
                    return ACTION_REMOVE
                } else {
                    return ACTION_BREAK
                }
            }
        })
    }

    @Synchronized
    override fun draw(displayer: IDisplayer): RenderingState? {
        return drawDanmakus(displayer, mTimer)
    }

    override fun reset() {
        danmakus = Danmakus()
        mRenderer.clear()
    }

    override fun seek(mills: Long) {
        reset()
        // 重置所有弹幕的出场锚点：seek 后按新位置重新投喂，清除屏幕上仍在飞的旧弹幕
        resetRenderStartTimes()
        releaseClock = mills
        releaseFromClock = mills
        mContext.mGlobalFlagValues.updateVisibleFlag()
        mRenderingState.reset()
        mLastBeginMills = 0L
        mLastEndMills = 0L
    }

    /**
     * 清除全部弹幕的出场锚点，等待按新位置重新释放
     */
    private fun resetRenderStartTimes() {
        val list = danmakuList ?: return
        // forEachSync 内部已加锁
        list.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                t.resetRenderStart()
                return IDanmakus.Consumer.ACTION_CONTINUE
            }
        })
    }

    /**
     * 把这次采样覆盖的弹幕投喂给弹幕时钟
     *
     * 播放器位置在这里只描述「这一次采样覆盖了 `(releaseFromClock, releaseClock]`
     * 这段播放器时间」。投喂时把整批弹幕**向前推一个采样周期**，再按各自在区间内的
     * 相对位置错开：
     *
     * ```
     * 出场时刻 = 弹幕时钟 + 本批墙钟跨度 - (区间终点 - 该弹幕时间) / 倍速
     * ```
     *
     * 结果所有弹幕的出场时刻都 `>= 当前弹幕时钟`，也就是**每条弹幕都从屏幕外开始**，
     * 按真实先后顺序依次滑入画面。若直接用 `弹幕时钟 - 回退量`，采样延迟会让一部分
     * 弹幕一投喂就已经"飞了一段"，看起来像从屏幕边缘内侧凭空出现。
     *
     * 除以倍速是为了把播放器时间差还原成真实墙钟时长——弹幕时钟按墙钟走，
     * 1x 时两者相同，2x 时 400ms 的视频内容对应 200ms 的墙钟。
     *
     * 早于本次区间起点的弹幕（启动/seek 遗留、之前的采样里该出场却没处理的）
     * 直接丢弃，绝不会因为「迟到」而被补放。
     *
     * @param list 当前可见窗口内的弹幕（按时间有序）
     * @param renderClock 当前弹幕时钟
     */
    private fun releaseDanmakus(list: IDanmakus?, renderClock: Long) {
        if (list == null || list.isEmpty()) return
        val from = releaseFromClock
        val to = releaseClock
        if (to <= from) return
        // 用真实倍速做映射（慢放时同样需要还原成 200ms 的墙钟跨度），
        // 不能用窗口回溯那套至少 1 倍的钳制
        val speed = speedFactor.coerceIn(MIN_FEED_SPEED, MAX_WINDOW_SPEED_FACTOR)
        // 这一批弹幕覆盖的墙钟跨度，同时作为前置量；卡顿时截断，避免整批弹幕迟到太久
        val feedSpan = ((to - from).toFloat() / speed).toLong().coerceIn(0L, MAX_FEED_AHEAD)
        // forEachSync 内部已加锁
        list.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                if (t.renderStartTime != BaseDanmaku.RENDER_START_NONE) {
                    return IDanmakus.Consumer.ACTION_CONTINUE
                }
                val time = t.getTime()
                if (time > to) {
                    // 还没到这次采样的区间；列表按时间有序，后面的只会更晚
                    return IDanmakus.Consumer.ACTION_BREAK
                }
                if (time <= from) {
                    // 早于本次区间：它在之前的采样里就该出场了，不再补放
                    t.discardRenderStart()
                } else {
                    // 回退量上限就是本批跨度，保证 anchor >= renderClock（从屏幕外开始）
                    val backMs = ((to - time).toFloat() / speed).toLong()
                        .coerceIn(0L, feedSpan)
                    t.anchorRenderStart(renderClock + feedSpan - backMs)
                }
                return IDanmakus.Consumer.ACTION_CONTINUE
            }
        })
    }

    override fun start() {
        mContext.registerConfigChangedCallback(mConfigChangedCallback)
    }

    override fun quit() {
        mContext.unregisterAllConfigChangedCallbacks()
        mRenderer.release()
    }

    override fun prepare() {
        val parser = mParser ?: return
        loadDanmakus(parser)
        mLastBeginMills = 0L
        mLastEndMills = 0L
        mTaskListener?.ready()
        mReadyState = true
    }

    /**
     * 通过解析器加载弹幕数据
     */
    protected open fun loadDanmakus(parser: BaseDanmakuParser) {
        danmakuList = parser
            .setConfig(mContext)
            .setDisplayer(mDisp)
            .setTimer(mTimer)
            .getDanmakus()
        mContext.mGlobalFlagValues.resetAll()
    }

    override fun setParser(parser: BaseDanmakuParser) {
        mParser = parser
        mReadyState = false
    }

    /**
     * 核心渲染循环
     *
     * 1. 清除位置保留器（如有标志）
     * 2. 按播放器位置算出可见窗口
     * 3. 把到点的弹幕投喂给弹幕时钟
     * 4. 绘制屏幕窗口内的弹幕
     *
     * @param disp 显示器
     * @param timer 弹幕时钟
     * @return 渲染状态，无弹幕数据时返回 null
     */
    protected open fun drawDanmakus(disp: IDisplayer, timer: DanmakuTimer): RenderingState? {
        if (clearRetainerFlag) {
            mRenderer.clearRetainer()
            clearRetainerFlag = false
        }
        val list = danmakuList ?: return null
        val renderingState = mRenderingState

        // 双时钟：窗口按释放时钟（播放器时间轴）计算，决定「哪些弹幕可能该出场」；
        // 已出场弹幕由渲染时钟独立走完自己的生命周期。
        val renderClock = timer.currMillisecond
        val releaseNow = releaseClock
        val maxDuration = mContext.mDanmakuFactory.MAX_DANMAKU_DURATION

        // 渲染时钟不乘倍速，所以已出场弹幕对应的视频时间点会落后释放时钟
        // 最多 `maxDuration * 倍速`，回溯源必须按倍速放大才不会把它们挤出窗口
        val backFactor = speedFactor.coerceIn(1f, MAX_WINDOW_SPEED_FACTOR)
        val beginMills = releaseNow - (maxDuration * backFactor).toLong() - RELEASE_WINDOW_MARGIN
        val endMills = releaseNow + maxDuration

        // 获取或复用屏幕窗口弹幕列表
        var screenDanmakus = danmakus
        if (mLastBeginMills > beginMills || releaseNow > mLastEndMills) {
            val newSub = list.sub(beginMills, endMills)
            if (newSub != null) {
                screenDanmakus = newSub
                danmakus = screenDanmakus
            }
            mLastBeginMills = beginMills
            mLastEndMills = endMills
        }

        // 到点的弹幕在此刻被投喂到弹幕时钟
        releaseDanmakus(screenDanmakus, renderClock)

        beginTracing(renderingState, screenDanmakus)
        if (screenDanmakus != null && !screenDanmakus.isEmpty()) {
            mRenderer.draw(disp, screenDanmakus, renderingState)
        }
        endTracing(renderingState)
        return renderingState
    }

    override fun requestClear() {
        mLastBeginMills = 0L
        mLastEndMills = 0L
    }

    override fun requestClearRetainer() {
        clearRetainerFlag = true
    }

    /**
     * 处理弹幕配置变更
     */
    open fun onDanmakuConfigChanged(
        config: DanmakuContext,
        tag: DanmakuContext.DanmakuConfigTag,
        values: Array<out Any?>
    ): Boolean {
        return handleOnDanmakuConfigChanged(config, tag, values)
    }

    /**
     * 处理具体的配置变更逻辑
     */
    protected open fun handleOnDanmakuConfigChanged(
        config: DanmakuContext,
        tag: DanmakuContext.DanmakuConfigTag?,
        values: Array<out Any?>
    ): Boolean {
        var handled = false
        when (tag) {
            null, DanmakuContext.DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN -> {
                handled = true
            }
            DanmakuContext.DanmakuConfigTag.DUPLICATE_MERGING_ENABLED -> {
                val enable = values.getOrNull(0) as? Boolean
                if (enable != null) {
                    if (enable) {
                        mContext.mDanmakuFilters.registerFilter(DanmakuFilters.TAG_DUPLICATE_FILTER)
                    } else {
                        mContext.mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_DUPLICATE_FILTER)
                    }
                    handled = true
                }
            }
            DanmakuContext.DanmakuConfigTag.SCALE_TEXTSIZE,
            DanmakuContext.DanmakuConfigTag.SCROLL_SPEED_FACTOR,
            DanmakuContext.DanmakuConfigTag.DANMAKU_MARGIN -> {
                requestClearRetainer()
                handled = false
            }
            DanmakuContext.DanmakuConfigTag.MAXIMUN_LINES,
            DanmakuContext.DanmakuConfigTag.OVERLAPPING_ENABLE -> {
                mRenderer.setVerifierEnabled(
                    mContext.isPreventOverlappingEnabled() || mContext.isMaxLinesLimited()
                )
                handled = true
            }
            DanmakuContext.DanmakuConfigTag.ALIGN_BOTTOM -> {
                val enable = values.getOrNull(0) as? Boolean
                if (enable != null) {
                    mRenderer.alignBottom(enable)
                    handled = true
                }
            }
            else -> {
                // 其他配置变更不做特殊处理
            }
        }
        return handled
    }

    /**
     * 开始渲染追踪
     */
    private fun beginTracing(
        renderingState: RenderingState,
        screenDanmakus: IDanmakus?
    ) {
        renderingState.reset()
        renderingState.timer.update(PlatformClock.uptimeMillis())
        renderingState.indexInScreen = 0
        renderingState.totalSizeInScreen = screenDanmakus?.size() ?: 0
    }

    /**
     * 结束渲染追踪
     */
    private fun endTracing(renderingState: RenderingState) {
        renderingState.consumingTime = renderingState.timer.update(PlatformClock.uptimeMillis())
    }
}
