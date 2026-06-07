package com.a10miaomiao.bilimiao.comm.delegate.player

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.BiliDanmukuParser
import java.io.ByteArrayInputStream
import java.io.InputStream

actual fun createBangumiPlayerSource(
    sid: String,
    epid: String,
    aid: String,
    id: String,
    title: String,
    coverUrl: String,
    ownerId: String,
    ownerName: String,
    episodes: List<BangumiEpisodeInfo>,
): BasePlayerSource {
    val source = object : BangumiPlayerSource(sid, epid, aid, id, title, coverUrl, ownerId, ownerName),
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
            if (sid == "26257") {
                // 答辩就不要看了
                throw com.a10miaomiao.bilimiao.comm.exception.DabianException()
            }
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
    source.episodes = episodes.map {
        BangumiPlayerSource.EpisodeInfo(
            epid = it.epid,
            aid = it.aid,
            cid = it.cid,
            cover = it.cover,
            index = it.index,
            index_title = it.index_title,
            badge = it.badge,
            badge_info = BangumiPlayerSource.EpisodeBadgeInfo(
                text = it.badge_info.text,
                bg_color = it.badge_info.bg_color,
                bg_color_night = it.badge_info.bg_color_night,
            ),
        )
    }
    return source
}
