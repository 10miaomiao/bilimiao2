package com.a10miaomiao.bilimiao.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation

class GlideRoundTransform(context: Context, val dp: Int = 5) : BitmapTransformation(context) {

    private val radius = Resources.getSystem().displayMetrics.density * dp

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return roundCrop(pool, toTransform)
    }

    private fun roundCrop(pool: BitmapPool, source: Bitmap): Bitmap {
        val result = pool.get(source.width, source.height, Bitmap.Config.ARGB_8888)
            ?: Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        paint.shader = BitmapShader (source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true;
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return result
    }

    override fun getId() = javaClass.name + Math.round(radius)


}