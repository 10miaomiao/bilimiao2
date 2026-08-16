package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser

/**
 * 弹幕提供者接口
 *
 * 由 [BasePlayerSource] 的子类实现，提供弹幕解析器。
 * 统一使用 KMP 弹幕引擎 (danmaku-engine) 的 [BaseDanmakuParser]，
 * 安卓端和桌面端共用同一套解析逻辑。
 */
interface DanmakuProvider {
    /**
     * 获取弹幕解析器，解析失败或无弹幕时返回 null
     */
    suspend fun getDanmakuParser(): BaseDanmakuParser?
}
