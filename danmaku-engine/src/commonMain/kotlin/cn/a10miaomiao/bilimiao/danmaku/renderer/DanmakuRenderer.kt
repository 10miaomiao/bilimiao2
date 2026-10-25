package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

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

        override fun accept(drawItem: BaseDanmaku): Int {
            lastItem = drawItem

            // 还没被投喂（启动 / seek 后的那一帧）：不参与渲染。
            // 这类弹幕没有出场锚点，任何位置计算都会退化成错误的基准。
            if (!drawItem.isRenderAnchored()) {
                return IDanmakus.Consumer.ACTION_CONTINUE
            }

            // 跳过超时弹幕
            if (drawItem.isRenderTimeOut()) {
                disp.recycle(drawItem)
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
            // 这里不需要再按「seek 前旧弹幕」过滤：投喂模型下早于本次投喂区间的弹幕
            // 已经被丢弃（见 DrawTask.releaseDanmakus）
            if (drawItem.priority.toInt() == 0 && drawItem.isFiltered()) {
                return IDanmakus.Consumer.ACTION_CONTINUE
            }

            // 跳过还没到出场时刻的弹幕；列表按时间有序，后面的只会更晚
            if (drawItem.isRenderWaiting(renderingState.timer.currMillisecond)) {
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

            // 布局
            mDanmakusRetainer.fix(drawItem, disp, mVerifier)

            // 绘制
            if (drawItem.isShown()) {
                if (drawItem.lines == null && drawItem.getBottom() > disp.height) {
                    return IDanmakus.Consumer.ACTION_CONTINUE // 跳过底部超出弹幕
                }
                drawItem.draw(disp)
                renderingState.addCount(drawItem.getType(), 1)
                renderingState.addTotalCount(1)
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
        renderingState: RenderingState
    ) {
        mStartTimer = renderingState.timer
        mConsumer.disp = disp
        mConsumer.renderingState = renderingState
        danmakus.forEachSync(mConsumer)
    }

    override fun alignBottom(enable: Boolean) {
        mDanmakusRetainer.alignBottom(enable)
    }
}
