package cn.a10miaomiao.bilimiao.danmaku.model

import cn.a10miaomiao.bilimiao.danmaku.util.DanmakuUtils

/**
 * 占位/空弹幕，用于比较器和占位
 */
class Danmaku(text: CharSequence) : BaseDanmaku() {
    init {
        DanmakuUtils.fillText(this, text)
    }

    override fun isShown(): Boolean = false
    override fun layout(displayer: IDisplayer, x: Float, y: Float) {}
    override fun getRectAtTime(displayer: IDisplayer, time: Long): FloatArray? = null
    override fun getLeft(): Float = 0f
    override fun getTop(): Float = 0f
    override fun getRight(): Float = 0f
    override fun getBottom(): Float = 0f
    override fun getType(): Int = 0
}
