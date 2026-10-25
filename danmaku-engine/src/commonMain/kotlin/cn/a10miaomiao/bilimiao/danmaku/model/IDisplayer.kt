package cn.a10miaomiao.bilimiao.danmaku.model

import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuTypeface

/**
 * 弹幕显示器接口
 */
interface IDisplayer {

    val width: Int
    val height: Int
    val density: Float
    val densityDpi: Int
    val scaledDensity: Float
    val slopPixel: Int
    val margin: Int
    val allMarginTop: Int

    fun draw(danmaku: BaseDanmaku)
    fun recycle(danmaku: BaseDanmaku)
    fun measure(danmaku: BaseDanmaku, fromWorkerThread: Boolean)

    fun resetSlopPixel(factor: Float)
    fun setDensities(density: Float, densityDpi: Int, scaledDensity: Float)
    fun setSize(width: Int, height: Int)
    fun setMargin(m: Int)
    fun setAllMarginTop(m: Int)

    /**
     * 清除文本高度缓存
     */
    fun clearTextHeightCache()

    /**
     * 设置字体
     */
    fun setTypeFace(typeface: DanmakuTypeface?)

    /**
     * 设置透明度 (0-255)
     */
    fun setTransparency(alpha: Int)

    /**
     * 设置文本缩放因子
     */
    fun setScaleTextSizeFactor(factor: Float)

    /**
     * 设置粗体
     */
    fun setFakeBoldText(fakeBold: Boolean)
}
