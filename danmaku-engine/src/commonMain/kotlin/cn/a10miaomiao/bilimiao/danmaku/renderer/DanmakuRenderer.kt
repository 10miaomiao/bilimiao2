package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.ICacheManager
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.model.IDrawingCache

/**
 * 弹幕渲染器实现
 */
class DanmakuRenderer(
    private val mContext: DanmakuContext
) : IRenderer {

    private inner class Consumer : IDanmakus.DefaultConsumer<BaseDanmaku>() {
        var lastItem: BaseDanmaku? = null
        lateinit var disp: IDisplayer
        lateinit var renderingState: RenderingState
        var startRenderTime: Long = 0

        override fun accept(drawItem: BaseDanmaku): Int {
            lastItem = drawItem

            // 跳过超时弹幕
            if (drawItem.isTimeOut()) {
                disp.recycle(drawItem)
                return if (renderingState.isRunningDanmakus) {
                    IDanmakus.Consumer.ACTION_REMOVE
                } else {
                    IDanmakus.Consumer.ACTION_CONTINUE
                }
            }

            // 跳过偏移弹幕
            if (!renderingState.isRunningDanmakus && drawItem.isOffset()) {
                return IDanmakus.Consumer.ACTION_CONTINUE
            }

            // 应用主过滤器
            if (!drawItem.hasPassedFilter()) {
                mContext.mDanmakuFilters.filter(
                    drawItem,
                    renderingState.indexInScreen,
                    renderingState.totalSizeInScreen,
                    renderingState.timer,
                    false,
                    mContext
                )
            }

            // 跳过过滤弹幕（优先级>0的除外）
            if (drawItem.getActualTime() < startRenderTime
                || (drawItem.priority.toInt() == 0 && drawItem.isFiltered())
            ) {
                return IDanmakus.Consumer.ACTION_CONTINUE
            }

            // 跳过未到时间的弹幕，请求缓存构建
            if (drawItem.isLate()) {
                val cache: IDrawingCache<*>? = drawItem.getDrawingCache()
                if (mCacheManager != null && (cache == null || cache.get() == null)) {
                    mCacheManager!!.addDanmaku(drawItem)
                }
                return IDanmakus.Consumer.ACTION_BREAK
            }

            // 同屏弹幕密度只对滚动弹幕有效
            if (drawItem.getType() == BaseDanmaku.TYPE_SCROLL_RL) {
                renderingState.indexInScreen++
            }

            // 测量
            if (!drawItem.isMeasured()) {
                drawItem.measure(disp, false)
            }

            // 准备绘制
            if (!drawItem.isPrepared()) {
                drawItem.prepare(disp, false)
            }

            // 布局
            mDanmakusRetainer.fix(drawItem, disp, mVerifier)

            // 绘制
            if (drawItem.isShown()) {
                if (drawItem.lines == null && drawItem.getBottom() > disp.height) {
                    return IDanmakus.Consumer.ACTION_CONTINUE // 跳过底部超出弹幕
                }
                val renderingType = drawItem.draw(disp)
                if (renderingType == IRenderer.CACHE_RENDERING) {
                    renderingState.cacheHitCount++
                } else if (renderingType == IRenderer.TEXT_RENDERING) {
                    renderingState.cacheMissCount++
                    mCacheManager?.addDanmaku(drawItem)
                }
                renderingState.addCount(drawItem.getType(), 1)
                renderingState.addTotalCount(1)
                renderingState.appendToRunningDanmakus(drawItem)

                if (mOnDanmakuShownListener != null
                    && drawItem.firstShownFlag != mContext.mGlobalFlagValues.FIRST_SHOWN_RESET_FLAG
                ) {
                    drawItem.firstShownFlag = mContext.mGlobalFlagValues.FIRST_SHOWN_RESET_FLAG
                    mOnDanmakuShownListener!!.onDanmakuShown(drawItem)
                }
            }
            return IDanmakus.Consumer.ACTION_CONTINUE
        }

        override fun after() {
            renderingState.lastDanmaku = lastItem
            super.after()
        }
    }

    private var mStartTimer: DanmakuTimer? = null
    private var mVerifier: DanmakusRetainer.Verifier? = null
    private val verifier = object : DanmakusRetainer.Verifier {
        override fun skipLayout(
            danmaku: BaseDanmaku,
            fixedTop: Float,
            lines: Int,
            willHit: Boolean
        ): Boolean {
            if (danmaku.priority.toInt() == 0 && mContext.mDanmakuFilters.filterSecondary(
                    danmaku, lines, 0, mStartTimer!!, willHit, mContext
                )
            ) {
                danmaku.setVisibility(false)
                return true
            }
            return false
        }
    }

    private val mDanmakusRetainer = DanmakusRetainer(mContext.isAlignBottom())
    private var mCacheManager: ICacheManager? = null
    private var mOnDanmakuShownListener: IRenderer.OnDanmakuShownListener? = null
    private val mConsumer = Consumer()

    override fun clear() {
        clearRetainer()
        mContext.mDanmakuFilters.clear()
    }

    override fun clearRetainer() {
        mDanmakusRetainer.clear()
    }

    override fun release() {
        mDanmakusRetainer.release()
        mContext.mDanmakuFilters.clear()
    }

    override fun setVerifierEnabled(enabled: Boolean) {
        mVerifier = if (enabled) verifier else null
    }

    override fun draw(
        disp: IDisplayer,
        danmakus: IDanmakus,
        startRenderTime: Long,
        renderingState: RenderingState
    ) {
        mStartTimer = renderingState.timer
        mConsumer.disp = disp
        mConsumer.renderingState = renderingState
        mConsumer.startRenderTime = startRenderTime
        danmakus.forEachSync(mConsumer)
    }

    override fun setCacheManager(cacheManager: ICacheManager) {
        mCacheManager = cacheManager
    }

    override fun setOnDanmakuShownListener(onDanmakuShownListener: IRenderer.OnDanmakuShownListener) {
        mOnDanmakuShownListener = onDanmakuShownListener
    }

    override fun removeOnDanmakuShownListener() {
        mOnDanmakuShownListener = null
    }

    override fun alignBottom(enable: Boolean) {
        mDanmakusRetainer.alignBottom(enable)
    }
}
