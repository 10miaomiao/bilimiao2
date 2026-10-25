package cn.a10miaomiao.bilimiao.danmaku.task

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.PlatformClock
import cn.a10miaomiao.bilimiao.danmaku.renderer.RenderingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 弹幕引擎控制器（双时钟模型）
 *
 * 引擎维护两个彼此独立的时钟，彻底解耦「弹幕何时出场」和「弹幕怎么飞」：
 *
 * - **释放时钟**（[IDrawTask.releaseClock]，播放器时间轴）：由外部播放器位置驱动，
 *   职责只有一个——走过某条弹幕的时间点时把它放出来。它可以跟随播放器自由跳动、
 *   采样抖动、精度不高，都不会影响画面。
 * - **渲染时钟**（[DanmakuTimer]）：只在播放时按真实墙钟推进，与播放器位置**和播放倍速**
 *   都无关。弹幕被放出的瞬间把当前渲染时钟记为出场锚点
 *   （[BaseDanmaku.renderStartTime]），之后位置、超时、透明度动画全部基于
 *   `渲染时钟 - 出场锚点` 计算。
 *
 * 因此：
 * - 播放器时间再怎么抖，已出场弹幕的运动速度也不会变化；
 * - 倍速播放时弹幕的滚动速度与停留时长在视觉上保持恒定，只有出场密度随倍速提高
 *   （播放器时间走得更快，到点的弹幕更多），不会出现「弹幕提前消失」；
 * - 玩家切倍速不影响已在飞的弹幕；
 * - 只有 seek 这种硬事件会同时重锚两个时钟并清屏重建。
 *
 * 渲染循环由外部驱动（Compose 的 `withFrameNanos` → `DanmakuCanvas`），引擎自身不再
 * 启动定时循环。
 *
 * 使用方式：
 * 1. 创建实例并传入 CoroutineScope
 * 2. 调用 setConfig() 设置弹幕上下文
 * 3. 调用 setParser() 设置弹幕解析器
 * 4. 调用 prepare() 准备弹幕数据
 * 5. 播放状态变化时调用 resume() / pause()
 * 6. 播放位置变化时写入 [externalPlayerPosition]，倍速变化时调用 [setPlaybackSpeed]
 * 7. 在 UI 层的绘制回调中调用 [drawWithSync] 进行渲染
 *
 * @param scope 协程作用域（用于准备阶段的延迟重试）
 * @param danmakuVisible 弹幕是否初始可见
 */
class DanmakuEngine(
    private val scope: CoroutineScope,
    danmakuVisible: Boolean = true
) {

    companion object {
        /** 允许的倍速范围 */
        private const val MIN_SPEED = 0.25f
        private const val MAX_SPEED = 16f
    }

    // region 状态

    /** 引擎是否处于停止状态（暂停 / 未启动） */
    var quitFlag: Boolean = true
        private set

    /** 弹幕数据是否准备完成 */
    private var mReady: Boolean = false

    /**
     * 外部播放器位置（毫秒），由绘制方在每次绘制前**直接读取播放器**后写入
     *
     * 引擎不复制、不插值、不同步这个时钟：它准不准、抖不抖、采样粒度多粗，
     * 都只影响「弹幕什么时候出场」，不会影响任何已出场弹幕的运动。
     */
    @Volatile
    var externalPlayerPosition: Long = -1L

    /**
     * 播放倍速
     *
     * 只用于两处：识别 seek 跳变时的正常增量上限，以及可见窗口的回溯放大。
     * 不参与渲染时钟推进，因此不会改变弹幕的滚动速度。
     */
    var playbackSpeed: Float = 1f
        private set

    // endregion

    // region 依赖

    private var mContext: DanmakuContext? = null
    private var mParser: BaseDanmakuParser? = null
    private var drawTask: IDrawTask? = null

    // endregion

    // region 时钟

    /** 渲染时钟：弹幕运动与消失的唯一时间基准（只按墙钟单调递增） */
    private val timer = DanmakuTimer()

    /** 渲染时钟上一帧对应的墙钟时刻（0 表示尚未开始推进） */
    private var renderWallTime: Long = 0L

    /** 上一次读到的播放器位置，用于判断位置是否有变化 */
    private var lastPlayerPosition: Long = -1L

    /** 投喂游标：上一次投喂到的播放器位置，下次从这里继续投喂 */
    private var lastFedPosition: Long = -1L

    /** 渲染时钟是否已经与播放器位置对齐过（只在引擎生命周期内对齐一次） */
    private var clockInitialized: Boolean = false

    /** 渲染状态 */
    private val mRenderingState = RenderingState()

    // endregion

    // region 配置

    fun setConfig(config: DanmakuContext) {
        mContext = config
    }

    fun getConfig(): DanmakuContext? = mContext

    fun setParser(parser: BaseDanmakuParser) {
        mParser = parser
    }

    /**
     * 设置播放倍速
     *
     * 倍速只影响弹幕的出场密度（播放器时间走得更快，到点的弹幕更多），
     * 不影响弹幕的滚动速度：渲染时钟始终按真实墙钟推进，
     * 所以已出场弹幕的速度、停留时长保持恒定，切换倍速也不会造成位置跳变。
     *
     * @param speed 播放倍速（1.0 表示原速）
     */
    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(MIN_SPEED, MAX_SPEED)
    }

    // endregion

    // region 生命周期

    /**
     * 准备弹幕数据
     *
     * 加载并解析弹幕；解析器或上下文未设置时会延迟重试。
     */
    fun prepare() {
        mReady = false
        doPrepare()
    }

    private fun doPrepare() {
        val parser = mParser
        val context = mContext
        if (parser == null || context == null) {
            // 延迟重试
            scope.launch {
                delay(100)
                doPrepare()
            }
            return
        }
        prepareDrawTask(parser, context) {
            mReady = true
        }
    }

    private fun prepareDrawTask(
        parser: BaseDanmakuParser,
        context: DanmakuContext,
        onReady: () -> Unit
    ) {
        if (drawTask != null) {
            onReady()
            return
        }
        val disp = context.mDisplayer
        disp.resetSlopPixel(context.scaleTextSize)
        val task: IDrawTask = DrawTask(timer, context, object : IDrawTask.TaskListener {
            override fun ready() {
                onReady()
            }
        })
        task.setParser(parser)
        task.prepare()
        drawTask = task
        // 注册配置变更回调。
        // 注意：task.prepare() 内部触发的 ready 回调里 drawTask 尚未赋值（为 null），
        // 因此这里必须显式调用 task.start() 注册回调，
        // 否则 setMaximumLines 等配置变化的 notify 无法到达 DrawTask（如最大行数过滤失效）。
        task.start()
    }

    /**
     * 恢复播放
     */
    fun resume() {
        quitFlag = false
        if (!mReady) {
            scope.launch {
                delay(100)
                resume()
            }
            return
        }
        mRenderingState.reset()
        // 渲染时钟从暂停位置继续推进；播放器位置每帧重新从播放器读取，
        // 不需要在这里做任何锚定
        renderWallTime = 0L
        drawTask?.start()
    }

    /**
     * 暂停播放
     *
     * 渲染时钟停止推进，屏幕上的弹幕冻结在当前位置。
     */
    fun pause() {
        quitFlag = true
        renderWallTime = 0L
    }

    /**
     * 退出引擎，释放所有资源
     */
    fun release() {
        quitFlag = true
        lastPlayerPosition = -1L
        lastFedPosition = -1L
        clockInitialized = false
        renderWallTime = 0L
        drawTask?.quit()
        drawTask = null
        mParser?.release()
        mParser = null
    }

    /**
     * 是否处于停止状态
     */
    fun isStop(): Boolean = quitFlag

    // endregion

    // region 弹幕操作

    /**
     * 添加单条弹幕
     */
    fun addDanmaku(item: BaseDanmaku) {
        val task = drawTask ?: return
        val context = mContext ?: return
        item.flags = context.mGlobalFlagValues
        item.setTimer(timer)
        task.addDanmaku(item)
    }

    /**
     * 通知显示器尺寸变更
     */
    fun notifyDispSizeChanged(width: Int, height: Int) {
        val context = mContext ?: return
        val disp = context.mDisplayer
        if (disp.width != width || disp.height != height) {
            disp.setSize(width, height)
            context.mDanmakuFactory.notifyDispSizeChanged(context)
            context.mGlobalFlagValues.updateMeasureFlag()
            context.mGlobalFlagValues.updateVisibleFlag()
            drawTask?.requestClearRetainer()
        }
    }

    // endregion

    // region 渲染

    /**
     * 绘制弹幕到画布
     *
     * 此方法应在 UI 线程的绘制回调中调用（如 Compose 的 DrawScope 或 Canvas）。
     *
     * @param canvas 画布
     * @return 渲染状态
     */
    fun draw(canvas: IDisplayer): RenderingState? {
        return drawTask?.draw(canvas) ?: mRenderingState
    }

    /**
     * 读取播放器位置并绘制弹幕（外部渲染循环专用）
     *
     * 每帧调用一次：
     * 1. 播放器位置：直接使用外部读到的真实位置，不做任何推算/插值/同步
     * 2. 渲染时钟：按真实墙钟恒定推进（不乘倍速），与播放器位置无关
     * 3. 绘制：由 [DrawTask] 按播放器位置放出弹幕、按渲染时钟绘制运动
     *
     * @param canvas 画布
     * @return 渲染状态
     */
    fun drawWithSync(canvas: IDisplayer): RenderingState? {
        if (!quitFlag) {
            val now = PlatformClock.uptimeMillis()
            applyPlayerPosition()
            advanceRenderClock(now)
        }
        return draw(canvas)
    }

    /**
     * 把播放器的真实位置交给绘制任务去投喂弹幕
     *
     * 这里既没有时钟推算，也没有 seek 推断：位置怎么来就怎么用，
     * 只维护投喂游标——这次要投喂的是 `(上次位置, 本次位置]` 这段播放器时间。
     * 拖动进度条由播放器通过 [seekTo] 显式通知。
     */
    private fun applyPlayerPosition() {
        val position = externalPlayerPosition
        if (position < 0) return

        val previous = lastPlayerPosition
        // 播放器位置没变：这一段没有新弹幕要投喂（位置采样粒度比帧率粗）
        if (position == previous) return
        lastPlayerPosition = position

        if (!clockInitialized) {
            // 引擎启动后的第一次对齐：只把投喂游标对齐到播放器位置，
            // 不补放启动前就该出场的弹幕
            clockInitialized = true
            lastFedPosition = position
            drawTask?.let { task ->
                task.releaseFromClock = position
                task.releaseClock = position
                task.speedFactor = playbackSpeed
            }
            return
        }

        // 把这次采样覆盖的播放器时间段交给绘制任务，由它换算成弹幕时钟上的出场时刻
        drawTask?.let { task ->
            task.releaseFromClock = lastFedPosition
            task.releaseClock = position
            task.speedFactor = playbackSpeed
        }
        lastFedPosition = position
    }

    /**
     * 播放器显式通知 seek
     *
     * 由播放器侧调用（订阅 `BasePlayerDelegate.seekFlow` 后转发），弹幕层不再
     * 靠比对播放器位置去推断——播放器位置是低频采样上报的，采样延迟、卡顿、
     * 切分段都会造成跳变假象，推断必然误判，误判的代价是屏幕上正在飞的弹幕被整体重置。
     *
     * seek 会清屏并把投喂游标对齐到目标位置；弹幕时钟不动（它只按墙钟单调递增）。
     *
     * @param ms 目标位置（毫秒）
     */
    fun seekTo(ms: Long) {
        val now = PlatformClock.uptimeMillis()
        lastPlayerPosition = ms
        clockInitialized = true
        applySeek(ms, now)
    }

    /**
     * 渲染时钟按真实墙钟恒定推进
     *
     * 刻意不乘播放倍速：弹幕的滚动速度和停留时长保持视觉恒定，
     * 倍速只提高出场密度（释放时钟跟着播放器走），这样不会出现「弹幕提前消失」，
     * 也不会因为播放器采样抖动而变速。切换倍速不影响已经在飞的弹幕。
     */
    private fun advanceRenderClock(now: Long) {
        if (renderWallTime == 0L) {
            renderWallTime = now
            return
        }
        val wallDelta = now - renderWallTime
        if (wallDelta <= 0) return
        renderWallTime = now
        timer.add(wallDelta)
    }

    /**
     * 应用 seek：清屏、重置出场锚点，按新位置重新投喂
     *
     * 刻意**不**动弹幕时钟：它只按墙钟单调递增，一旦被 seek 目标位置回拨，
     * 屏幕上还在飞的弹幕 `弹幕时钟 - 出场锚点` 会变小，位置整体向右跳回去
     * （表现为「弹幕往后退」）。弹幕的出场锚点本来就是相对的，不需要和视频时间对齐。
     */
    private fun applySeek(target: Long, now: Long) {
        renderWallTime = now
        // 投喂游标对齐到目标位置，避免把跳过的这段视频的弹幕补放出来
        lastFedPosition = target
        // 清屏并重置全部弹幕的出场锚点，按新位置重新投喂
        drawTask?.seek(target)
    }

    // endregion
}
