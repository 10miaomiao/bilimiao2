package cn.a10miaomiao.bilimiao.danmaku.task

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.renderer.RenderingState

/**
 * 弹幕绘制任务接口
 *
 * 双时钟模型下，绘制任务的职责只有三件事：
 * 1. 接收外部投喂的播放器位置区间，把到点的弹幕换算成弹幕时钟上的出场时刻
 * 2. 按弹幕时钟推进绘制（位置、透明度、消失都由弹幕时钟决定）
 * 3. seek 时清屏并按新位置重新投喂
 */
interface IDrawTask {

    /**
     * 本次投喂区间终点（播放器时间轴，毫秒）= 当前读到的播放器位置
     *
     * 播放器位置不参与任何计时：它只用来描述「这一次采样覆盖了播放器的哪一段时间」，
     * 绘制任务把这批到点的弹幕换算成弹幕时钟的出场时刻，之后弹幕怎么飞、何时消失
     * 完全由弹幕时钟负责。
     */
    var releaseClock: Long

    /**
     * 本次投喂区间起点（播放器时间轴，毫秒）= 上一次读到的播放器位置
     *
     * 与 [releaseClock] 一起构成左开右闭区间 `(releaseFromClock, releaseClock]`，
     * 区间内的弹幕就是这次要投喂给弹幕时钟的弹幕。
     */
    var releaseFromClock: Long

    /**
     * 播放倍速
     *
     * 渲染时钟按墙钟速度推进、不乘倍速，所以倍速播放时一条弹幕在屏幕上停留的
     * 墙钟时长不变，但它对应的视频时间跨度会被拉长 `倍速` 倍。窗口回溯量必须
     * 同步放大，否则这些已经在飞的弹幕会被窗口挤出、提前消失；
     * 投喂时也需要按倍速把视频时间差还原成墙钟时长。
     */
    var speedFactor: Float

    /**
     * 添加单条弹幕
     *
     * @param item 弹幕对象
     */
    fun addDanmaku(item: BaseDanmaku)

    /**
     * 执行弹幕绘制
     *
     * @param displayer 显示器
     * @return 渲染状态
     */
    fun draw(displayer: IDisplayer): RenderingState?

    /**
     * 重置绘制任务状态
     */
    fun reset()

    /**
     * 跳转到指定时间
     *
     * @param mills 目标时间（毫秒）
     */
    fun seek(mills: Long)

    /**
     * 启动绘制任务（注册配置变更回调）
     */
    fun start()

    /**
     * 退出绘制任务，释放资源
     */
    fun quit()

    /**
     * 准备绘制任务，加载弹幕数据
     */
    fun prepare()

    /**
     * 请求清除渲染状态
     */
    fun requestClear()

    /**
     * 请求清除弹幕位置保留器
     */
    fun requestClearRetainer()

    /**
     * 设置弹幕解析器
     *
     * @param parser 弹幕解析器
     */
    fun setParser(parser: BaseDanmakuParser)

    /**
     * 绘制任务监听器
     */
    interface TaskListener {
        /**
         * 弹幕数据准备完成
         */
        fun ready()
    }
}
