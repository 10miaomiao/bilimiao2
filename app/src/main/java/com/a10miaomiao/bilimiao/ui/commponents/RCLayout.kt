package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import org.jetbrains.anko._FrameLayout

/**
 * 圆角相对布局
 * 参考 https://github.com/GcsSloop/rclayout
 */
class RCLayout constructor(context: Context) : _FrameLayout(context) {

    private var radii = FloatArray(8)          // top-left, top-right, bottom-right, bottom-left
    var mClipPath = Path()                  // 剪裁区域路径
    var mPaint = Paint()                    // 画笔
    var mRoundAsCircle = false              // 圆形
    var mStrokeColor = Color.WHITE          // 描边颜色
    var mStrokeWidth = 0                    // 描边半径
    var mAreaRegion = Region()              // 内容区域

    var roundCornerTopLeft = 0
        set(value) {
            radii[0] = value.toFloat()
            radii[1] = value.toFloat()
        }

    var roundCornerTopRight = 0
        set(value) {
            radii[2] = value.toFloat()
            radii[3] = value.toFloat()
        }

    var roundCornerBottomRight = 0
        set(value) {
            radii[4] = value.toFloat()
            radii[5] = value.toFloat()
        }

    var roundCornerBottomLeft = 0
        set(value) {
            radii[6] = value.toFloat()
            radii[7] = value.toFloat()
        }

    var roundCorner = 0
        set(value) {
            roundCornerTopLeft = value
            roundCornerTopRight = value
            roundCornerBottomRight = value
            roundCornerBottomLeft = value
        }

    init {
        radii[0] = roundCornerTopLeft.toFloat()
        radii[1] = roundCornerTopLeft.toFloat()

        radii[2] = roundCornerTopRight.toFloat()
        radii[3] = roundCornerTopRight.toFloat()

        radii[4] = roundCornerBottomRight.toFloat()
        radii[5] = roundCornerBottomRight.toFloat()

        radii[6] = roundCornerBottomLeft.toFloat()
        radii[7] = roundCornerBottomLeft.toFloat()
        mPaint.color = Color.WHITE
        mPaint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val areas = RectF()
        areas.left = paddingLeft.toFloat()
        areas.top = paddingTop.toFloat()
        areas.right = (w - paddingRight).toFloat()
        areas.bottom = (h - paddingBottom).toFloat()
        mClipPath.reset()
        if (mRoundAsCircle) {
            val d = if (areas.width() >= areas.height()) areas.height() else areas.width()
            val r = d / 2
            val center = PointF((w / 2).toFloat(), (h / 2).toFloat())
            mClipPath.addCircle(center.x, center.y, r, Path.Direction.CW)
            mClipPath.moveTo(0f, 0f)  // 通过空操作让Path区域占满画布
            mClipPath.moveTo(w.toFloat(), h.toFloat())
        } else {
            mClipPath.addRoundRect(areas, radii, Path.Direction.CW)
        }
        val clip = Region(areas.left.toInt(), areas.top.toInt(),
                areas.right.toInt(), areas.bottom.toInt())
        mAreaRegion.setPath(mClipPath, clip)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.saveLayer(RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()), null, Canvas
                .ALL_SAVE_FLAG)
        super.dispatchDraw(canvas)
        if (mStrokeWidth > 0) {
            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            mPaint.strokeWidth = (mStrokeWidth * 2).toFloat()
            mPaint.color = mStrokeColor
            mPaint.style = Paint.Style.STROKE
            canvas.drawPath(mClipPath, mPaint)
        }
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(mClipPath, mPaint)
        canvas.restore()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (!mAreaRegion.contains(ev.x.toInt(), ev.y.toInt())) {
            false
        } else super.dispatchTouchEvent(ev)
    }

}