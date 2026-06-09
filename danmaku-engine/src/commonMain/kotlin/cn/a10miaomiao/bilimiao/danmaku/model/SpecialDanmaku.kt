package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 特殊弹幕（支持位移动画、透明度动画、路径动画）
 */
class SpecialDanmaku : BaseDanmaku() {

    class Point(val x: Float, val y: Float) {
        fun getDistance(p: Point): Float {
            val dx = Math.abs(this.x - p.x)
            val dy = Math.abs(this.y - p.y)
            return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }

    class LinePath {
        var pBegin: Point? = null
        var pEnd: Point? = null
        var duration: Long = 0L
        var beginTime: Long = 0L
        var endTime: Long = 0L
        var deltaX: Float = 0f
        var deltaY: Float = 0f

        fun setPoints(begin: Point, end: Point) {
            this.pBegin = begin
            this.pEnd = end
            this.deltaX = end.x - begin.x
            this.deltaY = end.y - begin.y
        }

        fun getDistance(): Float = pEnd!!.getDistance(pBegin!!)
        fun getBeginPoint(): FloatArray = floatArrayOf(pBegin!!.x, pBegin!!.y)
        fun getEndPoint(): FloatArray = floatArrayOf(pEnd!!.x, pEnd!!.y)
    }

    @JvmField var beginX: Float = 0f
    @JvmField var beginY: Float = 0f
    @JvmField var endX: Float = 0f
    @JvmField var endY: Float = 0f
    @JvmField var deltaX: Float = 0f
    @JvmField var deltaY: Float = 0f
    @JvmField var translationDuration: Long = 0L
    @JvmField var translationStartDelay: Long = 0L

    private var mCurrentWidth: Int = 0
    private var mCurrentHeight: Int = 0

    /** Linear.easeIn or Quadratic.easeOut */
    @JvmField var isQuadraticEaseOut: Boolean = false

    @JvmField var beginAlpha: Int = 0
    @JvmField var endAlpha: Int = 0
    @JvmField var deltaAlpha: Int = 0
    @JvmField var alphaDuration: Long = 0L

    @JvmField var rotateX: Float = 0f
    @JvmField var rotateZ: Float = 0f
    @JvmField var pivotX: Float = 0f
    @JvmField var pivotY: Float = 0f

    private val currStateValues = FloatArray(4)
    @JvmField var linePaths: Array<LinePath>? = null

    override fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        super.measure(displayer, fromWorkerThread)
        if (mCurrentWidth == 0 || mCurrentHeight == 0) {
            mCurrentWidth = displayer.width
            mCurrentHeight = displayer.height
        }
    }

    override fun layout(displayer: IDisplayer, x: Float, y: Float) {
        if (mTimer != null) {
            getRectAtTime(displayer, mTimer!!.currMillisecond)
        }
    }

    override fun getRectAtTime(displayer: IDisplayer, currTime: Long): FloatArray? {
        if (!isMeasured()) return null

        setTranslationData(beginX, beginY, endX, endY, translationDuration, translationStartDelay)

        val paths = linePaths
        if (paths != null && paths.isNotEmpty()) {
            val points = Array(paths.size + 1) { FloatArray(2) }
            for (j in paths.indices) {
                points[j] = paths[j].getBeginPoint()
                points[j + 1] = paths[j].getEndPoint()
            }
            setLinePathData(points)
        }

        val deltaTime = currTime - getActualTime()

        // 计算透明度
        if (alphaDuration > 0 && deltaAlpha != 0) {
            if (deltaTime >= alphaDuration) {
                alpha = endAlpha
            } else {
                val alphaProgress = deltaTime.toFloat() / alphaDuration
                val vectorAlpha = (deltaAlpha * alphaProgress).toInt()
                alpha = beginAlpha + vectorAlpha
            }
        }

        // 计算 x y
        var currX = beginX
        var currY = beginY
        val dtime = deltaTime - translationStartDelay
        if (translationDuration > 0 && dtime in 0..translationDuration) {
            if (paths != null) {
                var currentLinePath: LinePath? = null
                for (line in paths) {
                    if (dtime >= line.beginTime && dtime < line.endTime) {
                        currentLinePath = line
                        break
                    } else {
                        currX = line.pEnd!!.x
                        currY = line.pEnd!!.y
                    }
                }
                if (currentLinePath != null) {
                    val ldx = currentLinePath.deltaX
                    val ldy = currentLinePath.deltaY
                    val progress = (deltaTime - currentLinePath.beginTime).toFloat() / currentLinePath.duration
                    val bX = currentLinePath.pBegin!!.x
                    val bY = currentLinePath.pBegin!!.y
                    if (ldx != 0f) currX = bX + ldx * progress
                    if (ldy != 0f) currY = bY + ldy * progress
                }
            } else {
                val progress = if (isQuadraticEaseOut) {
                    getQuadEaseOutProgress(dtime, translationDuration)
                } else {
                    dtime.toFloat() / translationDuration
                }
                if (deltaX != 0f) currX = beginX + deltaX * progress
                if (deltaY != 0f) currY = beginY + deltaY * progress
            }
        } else if (dtime > translationDuration) {
            currX = endX
            currY = endY
        }

        currStateValues[0] = currX
        currStateValues[1] = currY
        currStateValues[2] = currX + paintWidth
        currStateValues[3] = currY + paintHeight

        setVisibility(!isOutside())

        return currStateValues
    }

    override fun getLeft(): Float = currStateValues[0]
    override fun getTop(): Float = currStateValues[1]
    override fun getRight(): Float = currStateValues[2]
    override fun getBottom(): Float = currStateValues[3]
    override fun getType(): Int = TYPE_SPECIAL

    fun setTranslationData(
        beginX: Float, beginY: Float, endX: Float, endY: Float,
        translationDuration: Long, translationStartDelay: Long
    ) {
        this.beginX = beginX
        this.beginY = beginY
        this.endX = endX
        this.endY = endY
        this.deltaX = endX - beginX
        this.deltaY = endY - beginY
        this.translationDuration = translationDuration
        this.translationStartDelay = translationStartDelay
    }

    fun setAlphaData(beginAlpha: Int, endAlpha: Int, alphaDuration: Long) {
        this.beginAlpha = beginAlpha
        this.endAlpha = endAlpha
        this.deltaAlpha = endAlpha - beginAlpha
        this.alphaDuration = alphaDuration
        if (beginAlpha != AlphaValue.MAX) {
            alpha = beginAlpha
        }
    }

    fun setLinePathData(points: Array<FloatArray>?) {
        if (points != null) {
            val length = points.size
            this.beginX = points[0][0]
            this.beginY = points[0][1]
            this.endX = points[length - 1][0]
            this.endY = points[length - 1][1]
            if (points.size > 1) {
                linePaths = Array(points.size - 1) { i ->
                    LinePath().apply {
                        setPoints(
                            Point(points[i][0], points[i][1]),
                            Point(points[i + 1][0], points[i + 1][1])
                        )
                    }
                }
                var totalDistance = 0f
                for (line in linePaths!!) {
                    totalDistance += line.getDistance()
                }
                var lastLine: LinePath? = null
                for (line in linePaths!!) {
                    line.duration = ((line.getDistance() / totalDistance) * translationDuration).toLong()
                    line.beginTime = lastLine?.endTime ?: 0L
                    line.endTime = line.beginTime + line.duration
                    lastLine = line
                }
            }
        }
    }

    companion object {
        private fun getQuadEaseOutProgress(ctime: Long, duration: Long): Float {
            val t = ctime.toFloat() / duration
            return -1f * t * (t - 2f)
        }
    }
}
