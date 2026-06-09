package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 底部固定弹幕
 */
class FBDanmaku(duration: Duration) : FTDanmaku(duration) {
    override fun getType(): Int = TYPE_FIX_BOTTOM
}
