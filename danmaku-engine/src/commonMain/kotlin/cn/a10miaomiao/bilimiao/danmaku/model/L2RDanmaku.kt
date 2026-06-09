package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 左到右滚动弹幕
 */
class L2RDanmaku(duration: Duration) : R2LDanmaku(duration) {

    override fun layout(displayer: IDisplayer, x: Float, y: Float) {
        if (mTimer != null) {
            val currMS = mTimer!!.currMillisecond
            val deltaDuration = currMS - getActualTime()
            if (deltaDuration > 0 && deltaDuration < (duration?.value ?: 0L)) {
                this.x = getAccurateLeft(displayer, currMS)
                if (!isShown()) {
                    this.y = y
                    setVisibility(true)
                }
                mLastTime = currMS
                return
            }
            mLastTime = currMS
        }
        setVisibility(false)
    }

    override fun getRectAtTime(displayer: IDisplayer, time: Long): FloatArray? {
        if (!isMeasured()) return null
        val left = getAccurateLeft(displayer, time)
        if (RECT == null) {
            RECT = FloatArray(4)
        }
        RECT!![0] = left
        RECT!![1] = y
        RECT!![2] = left + paintWidth
        RECT!![3] = y + paintHeight
        return RECT
    }

    override fun getAccurateLeft(displayer: IDisplayer, currTime: Long): Float {
        val elapsedTime = currTime - getActualTime()
        if (elapsedTime >= (duration?.value ?: 0L)) {
            return displayer.width.toFloat()
        }
        return mStepX * elapsedTime - paintWidth
    }

    override fun getLeft(): Float = x
    override fun getTop(): Float = y
    override fun getRight(): Float = x + paintWidth
    override fun getBottom(): Float = y + paintHeight
    override fun getType(): Int = TYPE_SCROLL_LR
}
