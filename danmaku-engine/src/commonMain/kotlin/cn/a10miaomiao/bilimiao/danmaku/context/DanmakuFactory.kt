package cn.a10miaomiao.bilimiao.danmaku.context

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.Duration
import cn.a10miaomiao.bilimiao.danmaku.model.FBDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.FTDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.model.L2RDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.R2LDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.SpecialDanmaku

/**
 * 弹幕工厂，负责创建各种类型的弹幕实例并管理弹幕时长参数
 */
open class DanmakuFactory protected constructor() {

    companion object {
        /** B站旧播放器宽度 */
        const val OLD_BILI_PLAYER_WIDTH = 539f

        /** B站播放器宽度 */
        const val BILI_PLAYER_WIDTH = 682f

        /** B站旧播放器高度 */
        const val OLD_BILI_PLAYER_HEIGHT = 385f

        /** B站播放器高度 */
        const val BILI_PLAYER_HEIGHT = 438f

        /** B站原始分辨率下弹幕存活时间 */
        const val COMMON_DANMAKU_DURATION = 3800L

        /** 弹幕中等文字大小 */
        const val DANMAKU_MEDIUM_TEXTSIZE = 25

        /** 最小弹幕存活时间 */
        const val MIN_DANMAKU_DURATION = 4000L

        /** 高密度下最大弹幕存活时间 */
        const val MAX_DANMAKU_DURATION_HIGH_DENSITY = 9000L

        fun create(): DanmakuFactory = DanmakuFactory()
    }

    /** 当前显示区域宽度 */
    var CURRENT_DISP_WIDTH = 0

    /** 当前显示区域高度 */
    var CURRENT_DISP_HEIGHT = 0

    private var CURRENT_DISP_SIZE_FACTOR = 1.0f

    /** 实际弹幕存活时间 */
    var REAL_DANMAKU_DURATION = COMMON_DANMAKU_DURATION

    /** 最大弹幕存活时间 */
    var MAX_DANMAKU_DURATION = MIN_DANMAKU_DURATION

    /** 滚动弹幕最大存活时长 */
    var MAX_Duration_Scroll_Danmaku: Duration? = null

    /** 固定弹幕最大存活时长 */
    var MAX_Duration_Fix_Danmaku: Duration? = null

    /** 特殊弹幕最大存活时长 */
    var MAX_Duration_Special_Danmaku: Duration? = null

    var sLastDisp: IDisplayer? = null
    private var sLastConfig: DanmakuContext? = null

    /**
     * 重置时长数据
     */
    fun resetDurationsData() {
        sLastDisp = null
        CURRENT_DISP_WIDTH = 0
        CURRENT_DISP_HEIGHT = 0
        MAX_Duration_Scroll_Danmaku = null
        MAX_Duration_Fix_Danmaku = null
        MAX_Duration_Special_Danmaku = null
        MAX_DANMAKU_DURATION = MIN_DANMAKU_DURATION
    }

    /**
     * 通知显示尺寸变化
     */
    fun notifyDispSizeChanged(context: DanmakuContext) {
        sLastConfig = context
        sLastDisp = context.mDisplayer
        createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, context)
    }

    /**
     * 创建弹幕数据（使用上次保存的配置）
     *
     * @param type 弹幕类型
     * @return 弹幕实例
     */
    fun createDanmaku(type: Int): BaseDanmaku? {
        return createDanmaku(type, sLastConfig)
    }

    /**
     * 创建弹幕数据
     *
     * @param type 弹幕类型
     * @param context 弹幕上下文
     * @return 弹幕实例
     */
    fun createDanmaku(type: Int, context: DanmakuContext?): BaseDanmaku? {
        if (context == null) return null
        sLastConfig = context
        sLastDisp = context.mDisplayer
        return createDanmaku(
            type,
            sLastDisp!!.width.toFloat(),
            sLastDisp!!.height.toFloat(),
            CURRENT_DISP_SIZE_FACTOR,
            context.scrollSpeedFactor
        )
    }

    /**
     * 创建弹幕数据
     *
     * @param type 弹幕类型
     * @param disp 显示器
     * @param viewportScale 缩放比例
     * @param scrollSpeedFactor 滚动速度系数
     * @return 弹幕实例
     */
    fun createDanmaku(type: Int, disp: IDisplayer, viewportScale: Float, scrollSpeedFactor: Float): BaseDanmaku? {
        sLastDisp = disp
        return createDanmaku(type, disp.width.toFloat(), disp.height.toFloat(), viewportScale, scrollSpeedFactor)
    }

    /**
     * 创建弹幕数据请尽量使用此方法,参考BiliDanmakuParser或AcfunDanmakuParser
     *
     * @param type 弹幕类型
     * @param viewportWidth danmakuview宽度,会影响滚动弹幕的存活时间(duration)
     * @param viewportHeight danmakuview高度
     * @param viewportScale 缩放比例,会影响滚动弹幕的存活时间(duration)
     * @param scrollSpeedFactor 滚动速度系数
     * @return 弹幕实例
     */
    fun createDanmaku(type: Int, viewportWidth: Int, viewportHeight: Int,
                      viewportScale: Float, scrollSpeedFactor: Float): BaseDanmaku? {
        return createDanmaku(type, viewportWidth.toFloat(), viewportHeight.toFloat(), viewportScale, scrollSpeedFactor)
    }

    /**
     * 创建弹幕数据请尽量使用此方法,参考BiliDanmakuParser或AcfunDanmakuParser
     *
     * @param type 弹幕类型
     * @param viewportWidth danmakuview宽度,会影响滚动弹幕的存活时间(duration)
     * @param viewportHeight danmakuview高度
     * @param viewportSizeFactor 会影响滚动弹幕的速度/存活时间(duration)
     * @param scrollSpeedFactor 滚动速度系数
     * @return 弹幕实例
     */
    fun createDanmaku(type: Int, viewportWidth: Float, viewportHeight: Float,
                      viewportSizeFactor: Float, scrollSpeedFactor: Float): BaseDanmaku? {
        val sizeChanged = updateViewportState(viewportWidth, viewportHeight, viewportSizeFactor)

        if (MAX_Duration_Scroll_Danmaku == null) {
            MAX_Duration_Scroll_Danmaku = Duration(REAL_DANMAKU_DURATION).also {
                it.setFactor(scrollSpeedFactor)
            }
        } else if (sizeChanged) {
            MAX_Duration_Scroll_Danmaku!!.setValue(REAL_DANMAKU_DURATION)
        }

        if (MAX_Duration_Fix_Danmaku == null) {
            MAX_Duration_Fix_Danmaku = Duration(COMMON_DANMAKU_DURATION)
        }

        if (sizeChanged && viewportWidth > 0) {
            updateMaxDanmakuDuration()
        }

        return when (type) {
            BaseDanmaku.TYPE_SCROLL_RL -> R2LDanmaku(MAX_Duration_Scroll_Danmaku!!)       // 从右往左滚动
            BaseDanmaku.TYPE_FIX_BOTTOM -> FBDanmaku(MAX_Duration_Fix_Danmaku!!)           // 底端固定
            BaseDanmaku.TYPE_FIX_TOP -> FTDanmaku(MAX_Duration_Fix_Danmaku!!)              // 顶端固定
            BaseDanmaku.TYPE_SCROLL_LR -> L2RDanmaku(MAX_Duration_Scroll_Danmaku!!)        // 从左往右滚动
            BaseDanmaku.TYPE_SPECIAL -> SpecialDanmaku()                                   // 特殊弹幕
            else -> null
        }
    }

    /**
     * 更新视口状态
     *
     * @param viewportWidth 视口宽度
     * @param viewportHeight 视口高度
     * @param viewportSizeFactor 视口缩放因子
     * @return 是否发生了尺寸变化
     */
    fun updateViewportState(viewportWidth: Float, viewportHeight: Float,
                            viewportSizeFactor: Float): Boolean {
        var sizeChanged = false
        if (CURRENT_DISP_WIDTH != viewportWidth.toInt()
            || CURRENT_DISP_HEIGHT != viewportHeight.toInt()
            || CURRENT_DISP_SIZE_FACTOR != viewportSizeFactor
        ) {
            sizeChanged = true
            REAL_DANMAKU_DURATION = (COMMON_DANMAKU_DURATION * (viewportSizeFactor
                    * viewportWidth / BILI_PLAYER_WIDTH)).toLong()
            REAL_DANMAKU_DURATION = minOf(MAX_DANMAKU_DURATION_HIGH_DENSITY, REAL_DANMAKU_DURATION)
            REAL_DANMAKU_DURATION = maxOf(MIN_DANMAKU_DURATION, REAL_DANMAKU_DURATION)

            CURRENT_DISP_WIDTH = viewportWidth.toInt()
            CURRENT_DISP_HEIGHT = viewportHeight.toInt()
            CURRENT_DISP_SIZE_FACTOR = viewportSizeFactor
        }
        return sizeChanged
    }

    /**
     * 更新最大弹幕存活时间
     */
    fun updateMaxDanmakuDuration() {
        val maxScrollDuration = MAX_Duration_Scroll_Danmaku?.value ?: 0L
        val maxFixDuration = MAX_Duration_Fix_Danmaku?.value ?: 0L
        val maxSpecialDuration = MAX_Duration_Special_Danmaku?.value ?: 0L

        MAX_DANMAKU_DURATION = maxOf(maxScrollDuration, maxFixDuration)
        MAX_DANMAKU_DURATION = maxOf(MAX_DANMAKU_DURATION, maxSpecialDuration)
        MAX_DANMAKU_DURATION = maxOf(COMMON_DANMAKU_DURATION, MAX_DANMAKU_DURATION)
        MAX_DANMAKU_DURATION = maxOf(REAL_DANMAKU_DURATION, MAX_DANMAKU_DURATION)
    }

    /**
     * 更新滚动弹幕时长因子
     *
     * @param f 速度因子
     */
    fun updateDurationFactor(f: Float) {
        if (MAX_Duration_Scroll_Danmaku == null || MAX_Duration_Fix_Danmaku == null) return
        MAX_Duration_Scroll_Danmaku!!.setFactor(f)
        updateMaxDanmakuDuration()
    }

    /**
     * 初始化特殊弹幕的位移数据
     *
     * @param item 弹幕
     * @param beginX 起始X坐标
     * @param beginY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param translationDuration 位移动画时长
     * @param translationStartDelay 位移动画开始延迟
     */
    fun fillTranslationData(item: BaseDanmaku, beginX: Float, beginY: Float,
                            endX: Float, endY: Float, translationDuration: Long,
                            translationStartDelay: Long) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL) return
        (item as SpecialDanmaku).setTranslationData(
            beginX, beginY, endX, endY, translationDuration, translationStartDelay
        )
        updateSpecialDanmakuDuration(item)
    }

    /**
     * 初始化特殊弹幕的路径数据
     *
     * @param item 弹幕
     * @param points 路径点数组
     */
    fun fillLinePathData(item: BaseDanmaku, points: Array<FloatArray>) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL || points.isEmpty()
            || points[0].size != 2
        ) return
        (item as SpecialDanmaku).setLinePathData(points)
    }

    /**
     * 初始化特殊弹幕的透明度数据
     *
     * @param item 弹幕
     * @param beginAlpha 起始透明度
     * @param endAlpha 结束透明度
     * @param alphaDuration 透明度动画时长
     */
    fun fillAlphaData(item: BaseDanmaku, beginAlpha: Int, endAlpha: Int,
                      alphaDuration: Long) {
        if (item.getType() != BaseDanmaku.TYPE_SPECIAL) return
        (item as SpecialDanmaku).setAlphaData(beginAlpha, endAlpha, alphaDuration)
        updateSpecialDanmakuDuration(item)
    }

    private fun updateSpecialDanmakuDuration(item: BaseDanmaku) {
        if (MAX_Duration_Special_Danmaku == null
            || (item.duration != null && item.duration!!.value > MAX_Duration_Special_Danmaku!!.value)
        ) {
            MAX_Duration_Special_Danmaku = item.duration
            updateMaxDanmakuDuration()
        }
    }
}
