package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.BiliDanmukuParser
import java.io.ByteArrayInputStream
import java.io.InputStream

actual fun createVideoPlayerSource(
    mainTitle: String,
    title: String,
    coverUrl: String,
    aid: String,
    id: String,
    ownerId: String,
    ownerName: String,
): VideoPlayerSource {
    return object : VideoPlayerSource(mainTitle, title, coverUrl, aid, id, ownerId, ownerName),
        DanmakuProvider {
        override suspend fun getDanmakuParser(): BaseDanmakuParser? {
            val inputStream = getBiliDanmukuStream()
            return if (inputStream == null) {
                null
            } else {
                val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
                loader.load(inputStream)
                val parser = BiliDanmukuParser()
                val dataSource = loader.dataSource
                parser.load(dataSource)
                parser
            }
        }

        private suspend fun getBiliDanmukuStream(): InputStream? {
            val res = BiliApiService.playerAPI.getDanmakuList(id)
                .awaitCall()
            val body = res.body
            return if (body == null) {
                null
            } else {
                ByteArrayInputStream(CompressionTools.decompressXML(body.bytes()))
            }
        }
    }
}
