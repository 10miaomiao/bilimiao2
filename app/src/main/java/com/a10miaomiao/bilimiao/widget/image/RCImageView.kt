package com.a10miaomiao.bilimiao.widget.image

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.graphics.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.BitmapShader
import androidx.appcompat.widget.AppCompatImageView

class RCImageView @JvmOverloads constructor(context: Context
                                            , attrs: AttributeSet? = null
                                            , defStyleAttr: Int = 0)
    : AppCompatImageView(context, attrs, defStyleAttr) {


    var isCircle = false
    var radius = 0

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 当模式为圆形模式的时候，我们强制让宽高一致
        if (isCircle) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val result = Math.min(measuredHeight, measuredWidth)
            setMeasuredDimension(result, result)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val mDrawable = drawable
        val mDrawMatrix = imageMatrix
        if (mDrawable == null) {
            return  // couldn't resolve the URI
        }

        if (mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0) {
            return      // nothing to draw (empty bounds)
        }

        if (mDrawMatrix == null && paddingTop == 0 && paddingLeft == 0) {
            mDrawable.draw(canvas)
        } else {
            val saveCount = canvas.saveCount
            canvas.save()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (cropToPadding) {
                    val scrollX = scrollX
                    val scrollY = scrollY
                    canvas.clipRect(scrollX + paddingLeft, scrollY + paddingTop,
                        scrollX + right - left - paddingRight,
                        scrollY + bottom - top - paddingBottom)
                }
            }
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            if (isCircle) { // 当为圆形模式的时候
                val bitmap = drawable2Bitmap(mDrawable)
                mPaint.shader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                canvas.drawCircle(width / 2f, height / 2f, width / 2f, mPaint)
            } else if (radius > 0) { // 当为圆角模式的时候
                val bitmap = drawable2Bitmap(mDrawable)
                mPaint.shader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(RectF(paddingLeft.toFloat(), paddingTop.toFloat(), (width - paddingRight).toFloat(), (height - paddingBottom).toFloat()),
                    radius.toFloat(), radius.toFloat(), mPaint)
            } else {
                if (mDrawMatrix != null) {
                    canvas.concat(mDrawMatrix)
                }
                mDrawable.draw(canvas)
            }
            canvas.restoreToCount(saveCount)
        }
    }

    /**
     * drawable转换成bitmap
     */
    private fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        //根据传递的scaletype获取matrix对象，设置给bitmap
        val matrix = imageMatrix
        if (matrix != null) {
            canvas.concat(matrix)
        }
        drawable.draw(canvas)
        return bitmap
    }
}