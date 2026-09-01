package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.filter.DanmakuFilters
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.Duration
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.model.R2LDanmaku
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuTypeface
import cn.a10miaomiao.bilimiao.danmaku.task.DrawTask
import cn.a10miaomiao.bilimiao.danmaku.task.IDrawTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 验证"最大行数"设置在完整渲染链路（renderer -> retainer -> verifier -> filter）中生效。
 * 回归覆盖：DanmakuEngine.prepareDrawTask 未调用 DrawTask.start() 导致
 * setMaximumLines 的配置变更通知无法到达 DrawTask、verifier 不启用的问题。
 */
class MaximumLinesRenderTest {

    private class MeasureDisplayer : IDisplayer {
        override val width: Int get() = 1080
        override val height: Int get() = 1920
        override val density: Float get() = 1f
        override val densityDpi: Int get() = 160
        override val scaledDensity: Float get() = 1f
        override val slopPixel: Int get() = 6
        override val strokeWidth: Float get() = 0f
        override val isHardwareAccelerated: Boolean get() = true
        override val maximumCacheWidth: Int get() = 1080
        override val maximumCacheHeight: Int get() = 1920
        override val margin: Int get() = 0
        override val allMarginTop: Int get() = 0

        override fun draw(danmaku: BaseDanmaku): Int = 0
        override fun recycle(danmaku: BaseDanmaku) {}
        override fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {}
        override fun measure(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
            danmaku.paintWidth = 300f
            danmaku.paintHeight = 30f
        }
        override fun resetSlopPixel(factor: Float) {}
        override fun setDensities(density: Float, densityDpi: Int, scaledDensity: Float) {}
        override fun setSize(width: Int, height: Int) {}
        override fun setDanmakuStyle(style: Int, data: FloatArray?) {}
        override fun setMargin(m: Int) {}
        override fun setAllMarginTop(m: Int) {}
        override fun clearTextHeightCache() {}
        override fun setTypeFace(typeface: DanmakuTypeface?) {}
        override fun setTransparency(alpha: Int) {}
        override fun setScaleTextSizeFactor(factor: Float) {}
        override fun setFakeBoldText(fakeBold: Boolean) {}
    }

    private fun buildDanmakus(timer: DanmakuTimer, times: List<Long>): IDanmakus {
        val danmakus = Danmakus(IDanmakus.ST_BY_LIST)
        times.forEach { time ->
            val d = R2LDanmaku(Duration(5000))
            d.text = "test danmaku"
            d.setTime(time)
            d.setTimer(timer)
            danmakus.addItem(d)
        }
        return danmakus
    }

    private fun render(
        context: DanmakuContext,
        disp: IDisplayer,
        danmakus: IDanmakus,
        timer: DanmakuTimer,
    ): List<BaseDanmaku> {
        val renderer = DanmakuRenderer(context)
        renderer.setVerifierEnabled(true)
        val renderingState = RenderingState()
        renderingState.timer = timer
        renderer.draw(disp, danmakus, 0L, renderingState)
        val shown = mutableListOf<BaseDanmaku>()
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                if (t.isShown()) shown.add(t)
                return ACTION_CONTINUE
            }
        })
        return shown
    }

    @Test
    fun maximumLines1_occupiesOnlyOneLineAndFiltersMiddleDanmaku() {
        val context = DanmakuContext.create()
        val disp = MeasureDisplayer()
        context.mDisplayer = disp
        context.setMaximumLines(mapOf(BaseDanmaku.TYPE_SCROLL_RL to 1))

        val timer = DanmakuTimer(4000)
        val flags = context.mGlobalFlagValues

        // 过滤器数据与 filterSecondary 必须生效
        val filter = context.mDanmakuFilters
            .get(DanmakuFilters.TAG_MAXIMUN_LINES_FILTER, false) as DanmakuFilters.MaximumLinesFilter
        val probe = R2LDanmaku(Duration(5000))
        probe.setTime(1000)
        probe.setTimer(timer)
        assertTrue(filter.filter(probe, 3, 10, timer, false, context))

        val danmakus = buildDanmakus(timer, listOf(1000, 2000, 3000))
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                t.flags = flags
                return ACTION_CONTINUE
            }
        })

        val shown = render(context, disp, danmakus, timer)
        // 行数限制=1：同轨道（y 坐标）同时最多 1 条；新弹幕不与旧弹幕碰撞时替换旧位置
        val shownY = shown.map { it.getTop() }.toSet()
        assertEquals(setOf(0f), shownY)

        // 中间那条弹幕必须被行数过滤器拦截
        var middleFiltered = false
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                if (t.getTime() == 2000L) middleFiltered = t.isFiltered()
                return ACTION_CONTINUE
            }
        })
        assertTrue(middleFiltered)
    }

    @Test
    fun withoutMaximumLines_showsAllDanmakus() {
        val context = DanmakuContext.create()
        val disp = MeasureDisplayer()
        context.mDisplayer = disp

        val timer = DanmakuTimer(4000)
        val danmakus = buildDanmakus(timer, listOf(1000, 2000, 3000))
        val flags = context.mGlobalFlagValues
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                t.flags = flags
                return ACTION_CONTINUE
            }
        })

        val shown = render(context, disp, danmakus, timer)
        assertEquals(3, shown.size)
    }

    /**
     * 回归验证：DrawTask.start() 注册配置变更回调后，
     * setMaximumLines 的通知能到达 DrawTask 并启用 verifier，
     * 渲染时行数限制生效（修复前 prepareDrawTask 未调用 start()，此链路断裂）。
     */
    @Test
    fun setMaximumLines_viaDrawTaskNotification_enablesVerifier() {
        val context = DanmakuContext.create()
        val disp = MeasureDisplayer()
        context.mDisplayer = disp
        val timer = DanmakuTimer(4000)

        val task = DrawTask(timer, context, object : IDrawTask.TaskListener {
            override fun ready() {}
            override fun onDanmakuAdd(danmaku: BaseDanmaku) {}
            override fun onDanmakuShown(danmaku: BaseDanmaku) {}
            override fun onDanmakusDrawingFinished() {}
            override fun onDanmakuConfigChanged() {}
        })
        task.start() // 注册配置变更回调（引擎 prepareDrawTask 修复后必调）
        context.setMaximumLines(mapOf(BaseDanmaku.TYPE_SCROLL_RL to 1))

        val flags = context.mGlobalFlagValues
        val danmakus = buildDanmakus(timer, listOf(1000, 2000, 3000))
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                t.flags = flags
                return ACTION_CONTINUE
            }
        })

        val renderingState = RenderingState()
        renderingState.timer = timer
        task.mRenderer.draw(disp, danmakus, 0L, renderingState)

        val shownY = mutableSetOf<Float>()
        danmakus.forEachSync(object : IDanmakus.DefaultConsumer<BaseDanmaku>() {
            override fun accept(t: BaseDanmaku): Int {
                if (t.isShown()) shownY.add(t.getTop())
                return ACTION_CONTINUE
            }
        })
        // verifier 已启用：滚动弹幕只占 1 行
        assertEquals(setOf(0f), shownY)
    }
}
