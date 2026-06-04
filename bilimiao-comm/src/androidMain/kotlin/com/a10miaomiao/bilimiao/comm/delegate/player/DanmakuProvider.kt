package com.a10miaomiao.bilimiao.comm.delegate.player

import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

interface DanmakuProvider {
    suspend fun getDanmakuParser(): BaseDanmakuParser?
}
