package com.a10miaomiao.bilimiao.comm.glide

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element.U8_4
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import java.security.MessageDigest


class GlideBlurTransformation(
    val context: Context,
    val blurRadius: Float = 25f
) : CenterCrop() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val bitmap: Bitmap = super.transform(pool!!, toTransform, outWidth, outHeight)
        return blurBitmap(
            bitmap, blurRadius, (outWidth * 0.5).toInt(),
            (outHeight * 0.5).toInt()
        )
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

    /**
     * @param image         需要模糊的图片
     * @param blurRadius    模糊的半径（1-25之间）
     * @return 模糊处理后的Bitmap
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun blurBitmap(
        image: Bitmap,
        blurRadius: Float,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // 将缩小后的图片做为预渲染的图片
        val inputBitmap = Bitmap.createScaledBitmap(image!!, outWidth, outHeight, false)
        // 创建一张渲染后的输出图片
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        // 创建RenderScript内核对象
        val rs: RenderScript = RenderScript.create(context)
        // 创建一个模糊效果的RenderScript的工具对象
        val blurScript: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, U8_4(rs))
        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        val tmpIn: Allocation = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut: Allocation = Allocation.createFromBitmap(rs, outputBitmap)
        // 设置渲染的模糊程度, 25f是最大模糊度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            blurScript.setRadius(blurRadius)
        }
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn)
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut)
        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }
}