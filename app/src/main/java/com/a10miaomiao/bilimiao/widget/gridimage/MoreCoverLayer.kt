/*
    MIT License
    Copyright (c) 2021 Plain
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
/*
    来源：https://github.com/plain-dev/NineGridImageView
 */
package com.a10miaomiao.bilimiao.widget.gridimage


import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

class MoreCoverLayer(
    private val desc: String?
) : BitmapTransformation() {

    companion object {
        private const val ID = "com.a10miaomiao.bilimiao.widget.gridimage.MoreCoverLayer"
        private val ID_BYTE: ByteArray = ID.toByteArray(Charset.defaultCharset())
    }

    private var paint = Paint(ANTI_ALIAS_FLAG)

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val poolBitmap = pool[outWidth, outHeight, Bitmap.Config.ARGB_8888]
        paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        // 灰色蒙层
        paint.colorFilter = PorterDuffColorFilter(
            Color.parseColor("#80333333"),
            PorterDuff.Mode.SRC_ATOP
        )

        val newBitmapCanvas = Canvas(poolBitmap)
        newBitmapCanvas.drawBitmap(toTransform, 0f, 0f, paint)

        // 查看更多图标
        /*if (moreIcon != null) {
            paint.colorFilter = null
            val drawableWidth = outWidth / 3
            val drawableHeight = outHeight / 3
            val markBitmap = drawable2Bitmap(moreIcon, drawableWidth, drawableHeight)
            newBitmapCanvas.drawBitmap(
                markBitmap,
                outWidth / 2f - drawableWidth / 2f,
                outHeight / 2f - drawableHeight / 2f,
                paint
            )
        }*/

        if (!desc.isNullOrBlank()) {
            paint.colorFilter = PorterDuffColorFilter(
                Color.WHITE,
                PorterDuff.Mode.SRC_ATOP
            )
            paint.textSize = 120f

            val boundsText = Rect()
            paint.getTextBounds(desc, 0, desc.length, boundsText)
            var x = (outWidth - boundsText.width()) / 2f

            /* 保证文字在区域内 */
            while (x < 10) {
                paint.textSize = paint.textSize - 10f
                paint.getTextBounds(desc, 0, desc.length, boundsText)
                x = (outWidth - boundsText.width()) / 2f
            }

            val y = (outHeight + boundsText.height()) / 2f

            newBitmapCanvas.drawText(desc, x, y, paint)
        }

        return poolBitmap
    }

    private fun drawable2Bitmap(
        drawable: Drawable,
        drawableWidth: Int,
        drawableHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawableWidth, drawableHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawableWidth, drawableHeight)
        drawable.draw(canvas)
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTE)
    }

    override fun equals(other: Any?): Boolean {
        return other is MoreCoverLayer
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

}
