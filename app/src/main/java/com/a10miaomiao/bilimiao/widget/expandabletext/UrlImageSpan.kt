package com.a10miaomiao.bilimiao.widget.expandabletext


import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import splitties.dimensions.dip


/**
 * 获取网络图片的ImageSpan
 * Created by Yomii on 2016/10/13.
 */
class UrlImageSpan(
    private val context: Context,
    private val url: String,
    private val tv: TextView
) : ImageSpan(context, R.drawable.bili_default_image_tv) {

    private var picShowed = false

    override fun getDrawable(): Drawable {
        if (!picShowed) {
            Glide.with(context)
                .asBitmap()
                .load(url.replace("http://","https://"))
                .listener(object : RequestListener<Bitmap> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val resources: Resources = tv.context.resources
                        val targetWidth = (resources.displayMetrics.widthPixels * 0.8).toInt()
                        val targetHeight = context.dip(24)
                        val zoom: Bitmap = zoom(resource!!, targetHeight)
                        val b = BitmapDrawable(resources, zoom)
                        b.setBounds(0, 0, b.intrinsicWidth, b.intrinsicHeight)
                        try {
                            val mDrawable = ImageSpan::class.java.getDeclaredField("mDrawable")
                            mDrawable.isAccessible = true
                            mDrawable.set(this@UrlImageSpan, b)
                            val mDrawableRef = DynamicDrawableSpan::class.java.getDeclaredField("mDrawableRef")
                            mDrawableRef.isAccessible = true
                            mDrawableRef.set(this@UrlImageSpan, null)
                            picShowed = true
                            tv.text = tv.text
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        } catch (e: NoSuchFieldException) {
                            e.printStackTrace()
                        }
                        return false
                    }
                })
                .submit();
        }
        val drawable = super.getDrawable()
        drawable.setBounds(0, 0, context.dip(24), context.dip(24))
        return drawable
    }

    companion object {
        /**
         * 按宽度缩放图片
         *
         * @param bmp  需要缩放的图片源
         * @param newW 需要缩放成的图片宽度
         *
         * @return 缩放后的图片
         */
        fun zoom(bmp: Bitmap, newW: Int): Bitmap {

            // 获得图片的宽高
            val width: Int = bmp.width
            val height: Int = bmp.height

            // 计算缩放比例
            val scale = newW.toFloat() / height

            // 取得想要缩放的matrix参数
            val matrix = Matrix()
            matrix.postScale(scale, scale)

            // 得到新的图片
            return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true)
        }
    }
}