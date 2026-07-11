package cn.a10miaomiao.bilimiao.danmaku.platform

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

/**
 * Desktop 端基于 Skia Bitmap 的位图实现
 *
 * 同时持有 Bitmap（用于像素操作）和延迟创建的 Image（用于 GPU 绘制）。
 */
class SkiaBitmap(val bitmap: Bitmap) : DanmakuBitmap {
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height

    /** 延迟创建的 Skia Image，用于 GPU 绘制 */
    val image: Image by lazy { Image.makeFromBitmap(bitmap) }

    override fun eraseColor(color: Int) {
        bitmap.erase(color)
    }

    override fun recycle() {
        // Skia Bitmap/Image 由 GC 管理
    }

    override fun isRecycled(): Boolean = false
}

class SkiaBitmapFactory : DanmakuBitmapFactory {
    override fun create(width: Int, height: Int, config: BitmapConfig): DanmakuBitmap {
        val colorType = when (config) {
            BitmapConfig.ARGB_8888 -> ColorType.BGRA_8888
            BitmapConfig.ARGB_4444 -> ColorType.BGRA_8888
            BitmapConfig.RGB_565 -> ColorType.RGB_565
        }
        val info = ImageInfo(width.coerceAtLeast(1), height.coerceAtLeast(1), colorType, ColorAlphaType.PREMUL)
        val bitmap = Bitmap()
        bitmap.allocPixels(info)
        return SkiaBitmap(bitmap)
    }
}
