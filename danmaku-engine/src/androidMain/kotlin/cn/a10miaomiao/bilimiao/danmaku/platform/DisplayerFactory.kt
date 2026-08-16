package cn.a10miaomiao.bilimiao.danmaku.platform

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 安卓端 actual：创建 [AndroidDisplayer]
 */
actual fun createPlatformDisplayer(context: DanmakuContext): IDisplayer {
    return AndroidDisplayer(context)
}
