package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.ICacheManager
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 弹幕渲染器接口
 */
interface IRenderer {

    companion object {
        const val NOTHING_RENDERING = 0
        const val CACHE_RENDERING = 1
        const val TEXT_RENDERING = 2
    }

    /**
     * 弹幕首次显示监听器
     */
    fun interface OnDanmakuShownListener {
        fun onDanmakuShown(danmaku: BaseDanmaku)
    }

    /**
     * 渲染区域
     */
    class Area {
        val mRefreshRect = FloatArray(4)
        private var mMaxHeight = 0
        private var mMaxWidth = 0

        fun setEdge(maxWidth: Int, maxHeight: Int) {
            mMaxWidth = maxWidth
            mMaxHeight = maxHeight
        }

        fun reset() {
            set(mMaxWidth.toFloat(), mMaxHeight.toFloat(), 0f, 0f)
        }

        fun resizeToMax() {
            set(0f, 0f, mMaxWidth.toFloat(), mMaxHeight.toFloat())
        }

        fun set(left: Float, top: Float, right: Float, bottom: Float) {
            mRefreshRect[0] = left
            mRefreshRect[1] = top
            mRefreshRect[2] = right
            mRefreshRect[3] = bottom
        }
    }

    fun draw(disp: IDisplayer, danmakus: IDanmakus, startRenderTime: Long, renderingState: RenderingState)

    fun clear()

    fun clearRetainer()

    fun release()

    fun setVerifierEnabled(enabled: Boolean)

    fun setCacheManager(cacheManager: ICacheManager)

    fun setOnDanmakuShownListener(onDanmakuShownListener: OnDanmakuShownListener)

    fun removeOnDanmakuShownListener()

    fun alignBottom(enable: Boolean)
}
