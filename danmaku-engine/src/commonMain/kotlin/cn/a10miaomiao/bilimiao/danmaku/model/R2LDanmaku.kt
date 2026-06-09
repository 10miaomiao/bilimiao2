package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 右到左滚动弹幕
 */
open class R2LDanmaku(duration: Duration) : BaseDanmaku() {

    companion object {
        const val MAX_RENDERING_TIME = 100L
        const val CORDON_RENDERING_TIME = 40L
    }

    protected var x: Float = 0f
    protected var y: Float = -1f
    protected var mDistance: Int = 0
    protected var RECT: FloatArray? = null
    protected var mStepX: Float = 0f
    protected var mLastTime: Long = 0L

    init {
        this.duration = duration
    }

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

    protected open fun getAccurateLeft(displayer: IDisplayer, currTime: Long): Float {
        val elapsedTime = currTime - getActualTime()
        if (elapsedTime >= (duration?.value ?: 0L)) {
            return -paintWidth
        }
        return displayer.width - elapsedTime * mStepX
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

    override fun getLeft(): Float = x
    override fun getTop(): Float = y
    override fun getRight(): Float = x + paintWidth
    override fun getBottom(): Float = y + paintHeight
    override fun getType(): Int = TYPE_SCROLL_RL

    override fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        super.measure(displayer, fromWorkerThread)
        mDistance = (displayer.width + paintWidth).toInt()
        mStepX = mDistance / (duration?.value ?: 1L).toFloat()
    }
}
