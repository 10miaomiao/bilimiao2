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

    /** 同步模式下正在运行的弹幕列表 */
    private var mRunningDanmakus: IDanmakus? = null

    /** 上次屏幕窗口的起始时间 */
    private var mLastBeginMills: Long = 0L

    /** 上次屏幕窗口的结束时间 */
    private var mLastEndMills: Long = 0L

    /** 渲染状态 */
    private val mRenderingState = RenderingState()

    /** 清除位置保留器标志 */
    protected var clearRetainerFlag: Boolean = false

    /** 渲染起始时间 */
    private var mStartRenderTime: Long = 0L

    /** 准备就绪状态 */
    protected var mReadyState: Boolean = false

    /** 播放状态 */
    protected var mPlayState: Int = 0

    /** 是否隐藏弹幕 */
    private var mIsHidden: Boolean = false

    /** 最后一条弹幕 */
    private var mLastDanmaku: BaseDanmaku? = null

    /** 是否请求渲染（即使隐藏也触发一次绘制） */
    private var mRequestRender: Boolean = false

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
        // 初始化渲染器回调
        mRenderer.setOnDanmakuShownListener(object : IRenderer.OnDanmakuShownListener {
            override fun onDanmakuShown(danmaku: BaseDanmaku) {
                mTaskListener?.onDanmakuShown(danmaku)
            }
        })
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
        if (mLastBeginMills <= item.getActualTime() && item.getActualTime() <= mLastEndMills) {
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
        if (added) {
            mTaskListener?.onDanmakuAdd(item)
        }
        if (mLastDanmaku == null || item.getActualTime() > mLastDanmaku!!.getActualTime()) {
            mLastDanmaku = item
        }
    }

    override fun invalidateDanmaku(item: BaseDanmaku, remeasure: Boolean) {
        item.requestFlags = item.requestFlags or BaseDanmaku.FLAG_REQUEST_INVALIDATE
        if (remeasure) {
            item.paintWidth = -1f
            item.paintHeight = -1f
            item.requestFlags = item.requestFlags or BaseDanmaku.FLAG_REQUEST_REMEASURE
            item.measureResetFlag++
        }
    }

    @Synchronized
    override fun removeAllDanmakus(isClearDanmakusOnScreen: Boolean) {
        val list = danmakuList ?: return
        if (list.isEmpty()) return
        synchronized(list) {
            if (!isClearDanmakusOnScreen) {
                val beginMills = mTimer.currMillisecond - mContext.mDanmakuFactory.MAX_DANMAKU_DURATION - 100
                val endMills = mTimer.currMillisecond + mContext.mDanmakuFactory.MAX_DANMAKU_DURATION
                val tempDanmakus = list.subnew(beginMills, endMills)
                if (tempDanmakus != null) {
                    danmakus = tempDanmakus
                }
            }
            list.clear()
        }
    }

    /**
     * 弹幕被移除时的回调，子类可覆写（如 CacheManagingDrawTask）
     */
    protected open fun onDanmakuRemoved(danmaku: BaseDanmaku) {
        // 子类可覆写
    }

    @Synchronized
    override fun removeAllLiveDanmakus() {
        if (danmakus.isEmpty()) return
        synchronized(danmakus) {
            danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
                override fun accept(t: BaseDanmaku): Int {
                    if (t.isLive) {
                        onDanmakuRemoved(t)
                        return ACTION_REMOVE
                    }
                    return ACTION_CONTINUE
                }
            })
        }
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
                val isTimeout = t.isTimeOut()
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

    override fun getVisibleDanmakusOnTime(time: Long): IDanmakus {
        val beginMills = time - mContext.mDanmakuFactory.MAX_DANMAKU_DURATION - 100
        val endMills = time + mContext.mDanmakuFactory.MAX_DANMAKU_DURATION
        var subDanmakus: IDanmakus? = null
        // 避免 ConcurrentModificationException，最多重试 3 次
        var i = 0
        while (i++ < 3) {
            try {
                subDanmakus = danmakuList?.subnew(beginMills, endMills)
                break
            } catch (_: Exception) {
            }
        }
        val visibleDanmakus = Danmakus()
        if (subDanmakus != null && !subDanmakus.isEmpty()) {
            subDanmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
                override fun accept(t: BaseDanmaku): Int {
                    if (t.isShown() && !t.isOutside()) {
                        visibleDanmakus.addItem(t)
                    }
                    return ACTION_CONTINUE
                }
            })
        }
        return visibleDanmakus
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
        mContext.mGlobalFlagValues.updateVisibleFlag()
        mContext.mGlobalFlagValues.updateFirstShownFlag()
        mContext.mGlobalFlagValues.updateSyncOffsetTimeFlag()
        mContext.mGlobalFlagValues.updatePrepareFlag()
        mRunningDanmakus = Danmakus(IDanmakus.ST_BY_LIST)
        mStartRenderTime = if (mills < 1000) 0 else mills
        mRenderingState.reset()
        mRenderingState.endTime = mStartRenderTime
        mLastBeginMills = 0L
        mLastEndMills = 0L

        if (danmakuList != null) {
            val last = danmakuList!!.last()
            if (last != null && !last.isTimeOut()) {
                mLastDanmaku = last
            }
        }
    }

    override fun clearDanmakusOnScreen(currMillis: Long) {
        reset()
        mContext.mGlobalFlagValues.updateVisibleFlag()
        mContext.mGlobalFlagValues.updateFirstShownFlag()
        mStartRenderTime = currMillis
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

    override fun onPlayStateChanged(state: Int) {
        mPlayState = state
    }

    /**
     * 通过解析器加载弹幕数据
     */
    protected open fun loadDanmakus(parser: BaseDanmakuParser) {
        danmakuList = parser
            .setConfig(mContext)
            .setDisplayer(mDisp)
            .setTimer(mTimer)
            .setListener(object : BaseDanmakuParser.Listener {
                override fun onDanmakuAdd(danmaku: BaseDanmaku) {
                    mTaskListener?.onDanmakuAdd(danmaku)
                }
            })
            .getDanmakus()
        mContext.mGlobalFlagValues.resetAll()
        if (danmakuList != null) {
            mLastDanmaku = danmakuList!!.last()
        }
    }

    override fun setParser(parser: BaseDanmakuParser) {
        mParser = parser
        mReadyState = false
    }

    /**
     * 核心渲染循环
     *
     * 1. 清除位置保留器（如有标志）
     * 2. 计算当前时间窗口
     * 3. 获取屏幕窗口内的弹幕
     * 4. 先绘制同步模式下的运行弹幕
     * 5. 绘制屏幕窗口内的弹幕
     * 6. 返回渲染状态
     *
     * @param disp 显示器
     * @param timer 计时器
     * @return 渲染状态，无弹幕数据时返回 null
     */
    protected open fun drawDanmakus(disp: IDisplayer, timer: DanmakuTimer): RenderingState? {
        if (clearRetainerFlag) {
            mRenderer.clearRetainer()
            clearRetainerFlag = false
        }
        val list = danmakuList ?: return null

        // 隐藏状态且未请求渲染时直接返回
        if (mIsHidden && !mRequestRender) {
            return mRenderingState
        }

        mRequestRender = false
        val renderingState = mRenderingState

        // 计算可见时间窗口
        val beginMills = timer.currMillisecond - mContext.mDanmakuFactory.MAX_DANMAKU_DURATION - 100
        val endMills = timer.currMillisecond + mContext.mDanmakuFactory.MAX_DANMAKU_DURATION

        // 获取或复用屏幕窗口弹幕列表
        var screenDanmakus = danmakus
        if (mLastBeginMills > beginMills || timer.currMillisecond > mLastEndMills) {
            val newSub = list.sub(beginMills, endMills)
            if (newSub != null) {
                screenDanmakus = newSub
                danmakus = screenDanmakus
            }
            mLastBeginMills = beginMills
            mLastEndMills = endMills
        }

        // 开始追踪
        val runningDanmakus = mRunningDanmakus
        beginTracing(renderingState, runningDanmakus, screenDanmakus)

        // 先绘制同步模式下的运行弹幕
        if (runningDanmakus != null && !runningDanmakus.isEmpty()) {
            mRenderingState.isRunningDanmakus = true
            mRenderer.draw(disp, runningDanmakus, 0L, mRenderingState)
        }

        // 绘制屏幕窗口内的弹幕
        mRenderingState.isRunningDanmakus = false
        if (screenDanmakus != null && !screenDanmakus.isEmpty()) {
            mRenderer.draw(mDisp, screenDanmakus, mStartRenderTime, renderingState)
            endTracing(renderingState)
            if (renderingState.nothingRendered) {
                if (mLastDanmaku != null && mLastDanmaku!!.isTimeOut()) {
                    mLastDanmaku = null
                    mTaskListener?.onDanmakusDrawingFinished()
                }
                if (renderingState.beginTime == RenderingState.UNKNOWN_TIME) {
                    renderingState.beginTime = beginMills
                }
                if (renderingState.endTime == RenderingState.UNKNOWN_TIME) {
                    renderingState.endTime = endMills
                }
            }
            return renderingState
        } else {
            renderingState.nothingRendered = true
            renderingState.beginTime = beginMills
            renderingState.endTime = endMills
            return renderingState
        }
    }

    override fun requestClear() {
        mLastBeginMills = 0L
        mLastEndMills = 0L
        mIsHidden = false
    }

    override fun requestClearRetainer() {
        clearRetainerFlag = true
    }

    override fun requestSync(fromTimeMills: Long, toTimeMills: Long, offsetMills: Long) {
        // 获取当前屏幕上的运行弹幕
        val runningDanmakus = mRenderingState.obtainRunningDanmakus()
        mRunningDanmakus = runningDanmakus
        // 设置每条弹幕的时间偏移
        runningDanmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                if (t.isOutside()) {
                    return ACTION_REMOVE
                }
                t.setTimeOffset(offsetMills + t.timeOffset)
                if (t.timeOffset == 0L) {
                    return ACTION_REMOVE
                }
                return ACTION_CONTINUE
            }
        })
        mStartRenderTime = toTimeMills
    }

    /**
     * 处理弹幕配置变更
     */
    open fun onDanmakuConfigChanged(
        config: DanmakuContext,
        tag: DanmakuContext.DanmakuConfigTag,
        values: Array<out Any?>
    ): Boolean {
        val handled = handleOnDanmakuConfigChanged(config, tag, values)
        mTaskListener?.onDanmakuConfigChanged()
        return handled
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

    override fun requestHide() {
        mIsHidden = true
    }

    override fun requestRender() {
        mRequestRender = true
    }

    /**
     * 开始渲染追踪
     */
    private fun beginTracing(
        renderingState: RenderingState,
        runningDanmakus: IDanmakus?,
        screenDanmakus: IDanmakus?
    ) {
        renderingState.reset()
        renderingState.timer.update(PlatformClock.uptimeMillis())
        renderingState.indexInScreen = 0
        renderingState.totalSizeInScreen =
            (runningDanmakus?.size() ?: 0) + (screenDanmakus?.size() ?: 0)
    }

    /**
     * 结束渲染追踪
     */
    private fun endTracing(renderingState: RenderingState) {
        renderingState.nothingRendered = (renderingState.totalDanmakuCount == 0)
        if (renderingState.nothingRendered) {
            renderingState.beginTime = RenderingState.UNKNOWN_TIME
        }
        val lastDanmaku = renderingState.lastDanmaku
        renderingState.lastDanmaku = null
        renderingState.endTime = if (lastDanmaku != null) {
            lastDanmaku.getActualTime()
        } else {
            RenderingState.UNKNOWN_TIME
        }
        renderingState.consumingTime = renderingState.timer.update(PlatformClock.uptimeMillis())
    }
}
