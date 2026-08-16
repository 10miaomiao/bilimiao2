package cn.a10miaomiao.bilimiao.danmaku.platform

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 平台 Displayer 工厂（expect/actual）
 *
 * 安卓端返回 [AndroidDisplayer]（android.graphics.Canvas），
 * 桌面端返回 [SkiaDisplayer]（Skia Canvas）。
 */
expect fun createPlatformDisplayer(context: DanmakuContext): IDisplayer
