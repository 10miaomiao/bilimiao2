package cn.a10miaomiao.bilimiao.danmaku.platform

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 桌面端 actual：创建 [SkiaDisplayer]
 */
actual fun createPlatformDisplayer(context: DanmakuContext): IDisplayer {
    return SkiaDisplayer(context)
}
