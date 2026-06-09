package cn.a10miaomiao.bilimiao.danmaku.platform

import java.awt.image.BufferedImage

class DesktopBitmap(val image: BufferedImage) : DanmakuBitmap {
    override val width: Int get() = image.width
    override val height: Int get() = image.height

    override fun eraseColor(color: Int) {
        val g = image.createGraphics()
        g.color = java.awt.Color(color, true)
        g.composite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC)
        g.fillRect(0, 0, width, height)
        g.dispose()
    }

    override fun recycle() {
        // BufferedImage 没有显式 recycle，由 GC 管理
    }

    override fun isRecycled(): Boolean = false
}

class DesktopBitmapFactory : DanmakuBitmapFactory {
    override fun create(width: Int, height: Int, config: BitmapConfig): DanmakuBitmap {
        val imageType = when (config) {
            BitmapConfig.ARGB_8888 -> BufferedImage.TYPE_INT_ARGB
            BitmapConfig.ARGB_4444 -> BufferedImage.TYPE_INT_ARGB
            BitmapConfig.RGB_565 -> BufferedImage.TYPE_USHORT_565_RGB
        }
        return DesktopBitmap(BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), imageType))
    }
}
