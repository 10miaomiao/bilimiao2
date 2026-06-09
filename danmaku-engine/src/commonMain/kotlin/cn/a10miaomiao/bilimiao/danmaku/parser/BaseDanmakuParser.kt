package cn.a10miaomiao.bilimiao.danmaku.parser

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 弹幕解析器基类
 *
 * 提供链式调用的加载、配置、解析流程。子类需实现 [parse] 方法完成实际解析逻辑。
 */
abstract class BaseDanmakuParser {

    /**
     * 解析器监听器
     */
    interface Listener {
        /**
         * 弹幕添加回调
         */
        fun onDanmakuAdd(danmaku: BaseDanmaku)

        /**
         * 弹幕数据变更回调
         */
        fun onDanmakuDataChanged() {}

        /**
         * 弹幕解析完成回调
         */
        fun onDanmakusParsed() {}
    }

    /** 数据源 */
    protected var mDataSource: IDataSource<*>? = null

    /** 计时器 */
    protected var mTimer: DanmakuTimer? = null

    /** 显示器宽度 */
    protected var mDispWidth: Int = 0

    /** 显示器高度 */
    protected var mDispHeight: Int = 0

    /** 显示器密度 */
    protected var mDispDensity: Float = 0f

    /** 缩放密度 */
    protected var mScaledDensity: Float = 0f

    /** 已解析的弹幕集合 */
    private var mDanmakus: IDanmakus? = null

    /** 显示器 */
    protected var mDisp: IDisplayer? = null

    /** 弹幕上下文 */
    protected var mContext: DanmakuContext? = null

    /** 监听器 */
    protected var mListener: Listener? = null

    /**
     * 设置显示器，同时更新视口状态
     */
    fun setDisplayer(disp: IDisplayer): BaseDanmakuParser {
        mDisp = disp
        mDispWidth = disp.width
        mDispHeight = disp.height
        mDispDensity = disp.density
        mScaledDensity = disp.scaledDensity
        mContext?.let { ctx ->
            ctx.mDanmakuFactory.updateViewportState(
                mDispWidth.toFloat(), mDispHeight.toFloat(), getViewportSizeFactor()
            )
            ctx.mDanmakuFactory.updateMaxDanmakuDuration()
        }
        return this
    }

    /**
     * 获取显示器
     */
    fun getDisplayer(): IDisplayer? = mDisp

    /**
     * 设置监听器
     */
    fun setListener(listener: Listener): BaseDanmakuParser {
        mListener = listener
        return this
    }

    /**
     * 计算视口缩放因子，影响滚动弹幕的速度
     */
    protected fun getViewportSizeFactor(): Float {
        return 1f / (mDispDensity - 0.6f)
    }

    /**
     * 加载数据源
     */
    fun load(source: IDataSource<*>): BaseDanmakuParser {
        mDataSource = source
        return this
    }

    /**
     * 设置计时器
     */
    fun setTimer(timer: DanmakuTimer): BaseDanmakuParser {
        mTimer = timer
        return this
    }

    /**
     * 获取计时器
     */
    fun getTimer(): DanmakuTimer? = mTimer

    /**
     * 获取弹幕集合
     *
     * 首次调用时执行解析，之后返回缓存结果。
     * 解析完成后会释放数据源并更新工厂的最大弹幕时长。
     */
    fun getDanmakus(): IDanmakus? {
        if (mDanmakus != null) return mDanmakus
        mContext?.mDanmakuFactory?.resetDurationsData()
        mDanmakus = parse()
        releaseDataSource()
        mContext?.mDanmakuFactory?.updateMaxDanmakuDuration()
        mListener?.onDanmakusParsed()
        return mDanmakus
    }

    /**
     * 释放数据源
     */
    protected fun releaseDataSource() {
        mDataSource?.release()
        mDataSource = null
    }

    /**
     * 执行解析，由子类实现
     *
     * @return 解析后的弹幕集合
     */
    protected abstract fun parse(): IDanmakus?

    /**
     * 释放资源
     */
    fun release() {
        releaseDataSource()
    }

    /**
     * 设置弹幕上下文配置
     */
    fun setConfig(config: DanmakuContext): BaseDanmakuParser {
        mContext = config
        return this
    }
}
