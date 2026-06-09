package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.util.DanmakuUtils

/**
 * 弹幕位置管理器，负责为弹幕分配显示位置避免碰撞
 */
class DanmakusRetainer(alignBottom: Boolean) {

    /**
     * 弹幕位置管理器接口
     */
    interface IDanmakusRetainer {
        fun fix(drawItem: BaseDanmaku, disp: IDisplayer, verifier: Verifier?)
        fun clear()
    }

    /**
     * 校验器接口，用于二次过滤
     */
    fun interface Verifier {
        fun skipLayout(danmaku: BaseDanmaku, fixedTop: Float, lines: Int, willHit: Boolean): Boolean
    }

    private var rldrInstance: IDanmakusRetainer? = null
    private var lrdrInstance: IDanmakusRetainer? = null
    private var ftdrInstance: IDanmakusRetainer? = null
    private var fbdrInstance: IDanmakusRetainer? = null

    init {
        alignBottom(alignBottom)
    }

    fun alignBottom(alignBottom: Boolean) {
        rldrInstance = if (alignBottom) AlignBottomRetainer() else AlignTopRetainer()
        lrdrInstance = if (alignBottom) AlignBottomRetainer() else AlignTopRetainer()
        if (ftdrInstance == null) {
            ftdrInstance = FTDanmakusRetainer()
        }
        if (fbdrInstance == null) {
            fbdrInstance = AlignBottomRetainer()
        }
    }

    fun fix(danmaku: BaseDanmaku, disp: IDisplayer, verifier: DanmakusRetainer.Verifier?) {
        when (danmaku.getType()) {
            BaseDanmaku.TYPE_SCROLL_RL -> rldrInstance!!.fix(danmaku, disp, verifier)
            BaseDanmaku.TYPE_SCROLL_LR -> lrdrInstance!!.fix(danmaku, disp, verifier)
            BaseDanmaku.TYPE_FIX_TOP -> ftdrInstance!!.fix(danmaku, disp, verifier)
            BaseDanmaku.TYPE_FIX_BOTTOM -> fbdrInstance!!.fix(danmaku, disp, verifier)
            BaseDanmaku.TYPE_SPECIAL -> danmaku.layout(disp, 0f, 0f)
        }
    }

    fun clear() {
        rldrInstance?.clear()
        lrdrInstance?.clear()
        ftdrInstance?.clear()
        fbdrInstance?.clear()
    }

    fun release() {
        clear()
    }

    /**
     * 顶部对齐位置管理器
     * 从上到下排列弹幕，找到不碰撞的位置
     */
    private open class AlignTopRetainer : IDanmakusRetainer {

        private inner class RetainerConsumer : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            lateinit var disp: IDisplayer
            var lines = 0
            var insertItem: BaseDanmaku? = null
            var firstItem: BaseDanmaku? = null
            var lastItem: BaseDanmaku? = null
            var minRightRow: BaseDanmaku? = null
            var drawItem: BaseDanmaku? = null
            var overwriteInsert = false
            var shown = false
            var willHit = false

            override fun before() {
                lines = 0
                insertItem = null
                firstItem = null
                lastItem = null
                minRightRow = null
                overwriteInsert = false
                shown = false
                willHit = false
            }

            override fun accept(item: BaseDanmaku): Int {
                if (mCancelFixingFlag) {
                    return IDanmakus.Consumer.ACTION_BREAK
                }
                lines++
                if (item === drawItem) {
                    insertItem = item
                    lastItem = null
                    shown = true
                    willHit = false
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                if (firstItem == null) {
                    firstItem = item
                }

                if (drawItem!!.paintHeight + item.getTop() > disp.height) {
                    overwriteInsert = true
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                if (minRightRow == null) {
                    minRightRow = item
                } else {
                    if (minRightRow!!.getRight() >= item.getRight()) {
                        minRightRow = item
                    }
                }

                // 检查碰撞
                willHit = DanmakuUtils.willHitInDuration(
                    disp, item, drawItem!!,
                    drawItem!!.getDuration(), drawItem!!.getTimer()!!.currMillisecond
                )
                if (!willHit) {
                    insertItem = item
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                lastItem = item
                return IDanmakus.Consumer.ACTION_CONTINUE
            }
        }

        protected var mVisibleDanmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_YPOS)
        protected var mCancelFixingFlag = false
        protected var mConsumer = RetainerConsumer()

        override fun fix(drawItem: BaseDanmaku, disp: IDisplayer, verifier: DanmakusRetainer.Verifier?) {
            if (drawItem.isOutside()) return

            var topPos = disp.allMarginTop.toFloat()
            var lines = 0
            var shown = drawItem.isShown()
            var willHit = !shown && !mVisibleDanmakus.isEmpty()
            var isOutOfVerticalEdge = false
            var removeItem: BaseDanmaku? = null
            val margin = disp.margin

            if (!shown) {
                mCancelFixingFlag = false
                // 确定弹幕位置
                var insertItem: BaseDanmaku? = null
                var firstItem: BaseDanmaku? = null
                var lastItem: BaseDanmaku? = null
                var minRightRow: BaseDanmaku? = null
                var overwriteInsert = false

                mConsumer.disp = disp
                mConsumer.drawItem = drawItem
                mVisibleDanmakus.forEachSync(mConsumer)
                lines = mConsumer.lines
                insertItem = mConsumer.insertItem
                firstItem = mConsumer.firstItem
                lastItem = mConsumer.lastItem
                minRightRow = mConsumer.minRightRow
                overwriteInsert = mConsumer.overwriteInsert
                shown = mConsumer.shown
                willHit = mConsumer.willHit

                var checkEdge = true
                if (insertItem != null) {
                    if (lastItem != null) {
                        topPos = lastItem.getBottom() + margin
                    } else {
                        topPos = insertItem.getTop()
                    }
                    if (insertItem !== drawItem) {
                        removeItem = insertItem
                        shown = false
                    }
                } else if (overwriteInsert && minRightRow != null) {
                    topPos = minRightRow.getTop()
                    checkEdge = false
                    shown = false
                } else if (lastItem != null) {
                    topPos = lastItem.getBottom() + margin
                    willHit = false
                } else if (firstItem != null) {
                    topPos = firstItem.getTop()
                    removeItem = firstItem
                    shown = false
                } else {
                    topPos = disp.allMarginTop.toFloat()
                }

                if (checkEdge) {
                    isOutOfVerticalEdge = isOutVerticalEdge(
                        overwriteInsert, drawItem, disp, topPos, firstItem, lastItem
                    )
                }
                if (isOutOfVerticalEdge) {
                    topPos = disp.allMarginTop.toFloat()
                    willHit = true
                    lines = 1
                } else if (removeItem != null) {
                    lines--
                }
                if (topPos == disp.allMarginTop.toFloat()) {
                    shown = false
                }
            }

            if (verifier != null && verifier.skipLayout(drawItem, topPos, lines, willHit)) {
                return
            }

            if (isOutOfVerticalEdge) {
                clear()
            }

            drawItem.layout(disp, drawItem.getLeft(), topPos)

            if (!shown) {
                if (removeItem != null) {
                    mVisibleDanmakus.removeItem(removeItem)
                }
                mVisibleDanmakus.addItem(drawItem)
            }
        }

        protected open fun isOutVerticalEdge(
            overwriteInsert: Boolean,
            drawItem: BaseDanmaku,
            disp: IDisplayer,
            topPos: Float,
            firstItem: BaseDanmaku?,
            lastItem: BaseDanmaku?
        ): Boolean {
            if (topPos < disp.allMarginTop
                || (firstItem != null && firstItem.getTop() > 0)
                || topPos + drawItem.paintHeight > disp.height
            ) {
                return true
            }
            return false
        }

        override fun clear() {
            mCancelFixingFlag = true
            mVisibleDanmakus.clear()
        }
    }

    /**
     * 顶部固定弹幕位置管理器
     * 继承 AlignTopRetainer，但只检查底部边界
     */
    private open class FTDanmakusRetainer : AlignTopRetainer() {
        override fun isOutVerticalEdge(
            overwriteInsert: Boolean,
            drawItem: BaseDanmaku,
            disp: IDisplayer,
            topPos: Float,
            firstItem: BaseDanmaku?,
            lastItem: BaseDanmaku?
        ): Boolean {
            return topPos + drawItem.paintHeight > disp.height
        }
    }

    /**
     * 底部对齐位置管理器
     * 从下到上排列弹幕
     */
    private class AlignBottomRetainer : FTDanmakusRetainer() {

        private inner class BottomRetainerConsumer : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            lateinit var disp: IDisplayer
            var lines = 0
            var removeItem: BaseDanmaku? = null
            var firstItem: BaseDanmaku? = null
            var drawItem: BaseDanmaku? = null
            var willHit = false
            var topPos = 0f

            override fun before() {
                lines = 0
                removeItem = null
                firstItem = null
                willHit = false
            }

            override fun accept(item: BaseDanmaku): Int {
                if (mCancelFixingFlag) {
                    return IDanmakus.Consumer.ACTION_BREAK
                }
                lines++
                if (item === drawItem) {
                    removeItem = null
                    willHit = false
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                if (firstItem == null) {
                    firstItem = item
                    if (firstItem!!.getBottom() != disp.height.toFloat()) {
                        return IDanmakus.Consumer.ACTION_BREAK
                    }
                }

                if (topPos < disp.allMarginTop) {
                    removeItem = null
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                // 检查碰撞
                willHit = DanmakuUtils.willHitInDuration(
                    disp, item, drawItem!!,
                    drawItem!!.getDuration(), drawItem!!.getTimer()!!.currMillisecond
                )
                if (!willHit) {
                    removeItem = item
                    return IDanmakus.Consumer.ACTION_BREAK
                }

                topPos = item.getTop() - disp.margin - drawItem!!.paintHeight
                return IDanmakus.Consumer.ACTION_CONTINUE
            }

        }

        private val mBottomConsumer = BottomRetainerConsumer()
        private var mBottomVisibleDanmakus: IDanmakus = Danmakus(IDanmakus.ST_BY_YPOS_DESC)

        override fun fix(drawItem: BaseDanmaku, disp: IDisplayer, verifier: DanmakusRetainer.Verifier?) {
            if (drawItem.isOutside()) return

            var shown = drawItem.isShown()
            var topPos = if (shown) drawItem.getTop() else -1f
            var lines = 0
            var willHit = !shown && !mBottomVisibleDanmakus.isEmpty()
            var isOutOfVerticalEdge = false

            if (topPos < disp.allMarginTop) {
                topPos = disp.height - drawItem.paintHeight
            }

            var removeItem: BaseDanmaku? = null
            var firstItem: BaseDanmaku? = null

            if (!shown) {
                mCancelFixingFlag = false
                mBottomConsumer.topPos = topPos
                mBottomConsumer.disp = disp
                mBottomConsumer.drawItem = drawItem
                mBottomVisibleDanmakus.forEachSync(mBottomConsumer)
                topPos = mBottomConsumer.topPos
                lines = mBottomConsumer.lines
                firstItem = mBottomConsumer.firstItem
                removeItem = mBottomConsumer.removeItem
                willHit = mBottomConsumer.willHit

                isOutOfVerticalEdge = isOutVerticalEdge(false, drawItem, disp, topPos, firstItem, null)
                if (isOutOfVerticalEdge) {
                    topPos = disp.height - drawItem.paintHeight
                    willHit = true
                    lines = 1
                } else {
                    if (topPos >= disp.allMarginTop) {
                        willHit = false
                    }
                    if (removeItem != null) {
                        lines--
                    }
                }
            }

            if (verifier != null && verifier.skipLayout(drawItem, topPos, lines, willHit)) {
                return
            }

            if (isOutOfVerticalEdge) {
                clear()
            }

            drawItem.layout(disp, drawItem.getLeft(), topPos)

            if (!shown) {
                if (removeItem != null) {
                    mBottomVisibleDanmakus.removeItem(removeItem)
                }
                mBottomVisibleDanmakus.addItem(drawItem)
            }
        }

        override fun isOutVerticalEdge(
            overwriteInsert: Boolean,
            drawItem: BaseDanmaku,
            disp: IDisplayer,
            topPos: Float,
            firstItem: BaseDanmaku?,
            lastItem: BaseDanmaku?
        ): Boolean {
            if (topPos < disp.allMarginTop
                || (firstItem != null && firstItem.getBottom() != disp.height.toFloat())
            ) {
                return true
            }
            return false
        }

        override fun clear() {
            mCancelFixingFlag = true
            mBottomVisibleDanmakus.clear()
        }
    }
}
