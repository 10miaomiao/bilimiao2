package com.a10miaomiao.bilimiao.utils

import android.R.attr.resource
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
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import org.jetbrains.anko.dip
import java.lang.reflect.Field


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
            Glide.with(context).load(url.replace("http://","https://")).asBitmap().into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap?>?) {
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
                }
            })
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