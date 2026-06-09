package cn.a10miaomiao.bilimiao.danmaku.platform

import android.graphics.Bitmap

class AndroidBitmap(val bitmap: Bitmap) : DanmakuBitmap {
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height

    override fun eraseColor(color: Int) = bitmap.eraseColor(color)
    override fun recycle() = bitmap.recycle()
    override fun isRecycled(): Boolean = bitmap.isRecycled
}

class AndroidBitmapFactory : DanmakuBitmapFactory {
    override fun create(width: Int, height: Int, config: BitmapConfig): DanmakuBitmap {
        val androidConfig = when (config) {
            BitmapConfig.ARGB_8888 -> Bitmap.Config.ARGB_8888
            BitmapConfig.ARGB_4444 -> Bitmap.Config.ARGB_4444
            BitmapConfig.RGB_565 -> Bitmap.Config.RGB_565
        }
        return AndroidBitmap(Bitmap.createBitmap(width, height, androidConfig))
    }
}
