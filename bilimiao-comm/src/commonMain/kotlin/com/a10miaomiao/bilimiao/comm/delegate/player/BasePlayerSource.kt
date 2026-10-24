package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.bilimiao.danmaku.parser.BiliDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.parser.IDataSource
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceIds
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.source.ResolvedSourceUrls
import com.a10miaomiao.bilimiao.comm.delegate.player.source.SmartSourceSelector
import com.a10miaomiao.bilimiao.comm.delegate.player.source.SourceUrlSet
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools

abstract class BasePlayerSource() : DanmakuProvider {
    abstract val id: String // cid
    abstract val title: String
    abstract val coverUrl: String
    abstract val ownerId: String
    abstract val ownerName: String
    open val mainTitle: String = "" // 视频主标题(分P所属的视频标题)
    abstract suspend fun getPlayerUrl(quality: Int, fnval: Int): PlayerSourceInfo
    abstract fun getSourceIds(): PlayerSourceIds

    open suspend fun getSubtitles(): List<SubtitleSourceInfo> = emptyList()
    open suspend fun historyReport(progress: Long) {}

    open fun next(): BasePlayerSource? = null

    var defaultPlayerSource = PlayerSourceInfo()
    var proxyServer: ProxyServerInfo? = null
    var uposHost: String = ""
    var isLoop: Boolean = false // 循环播放

    /**
     * 决定本次播放实际使用的视频 / 音频地址
     *
     * 优先级：手动指定的 upos 服务器（「区域限制」设置）> 智能换源选中的线路 > B 站下发的原地址。
     * 具体的测速与挑选见 [SmartSourceSelector]。
     *
     * @param video 视频流在 playurl 里下发的地址
     * @param audio 音频流在 playurl 里下发的地址；音视频合一（或已合进 MPD）时传 null
     * @param headers 播放用的请求头，测速时 CDN 靠它校验 Referer / User-Agent
     */
    protected suspend fun resolveSourceUrls(
        video: SourceUrlSet,
        audio: SourceUrlSet?,
        headers: Map<String, String>,
    ): ResolvedSourceUrls = SmartSourceSelector.resolve(video, audio, headers, uposHost)

    /**
     * 默认弹幕获取实现：下载 B站弹幕 XML（gzip 压缩），用 KMP [BiliDanmakuParser] 解析。
     *
     * 安卓端和桌面端共用此实现，替代原先安卓端基于 DanmakuFlameMaster 的
     * `DanmakuLoaderFactory` + `BiliDanmukuParser` 路径。
     */
    override suspend fun getDanmakuParser(): cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser? {
        return try {
            val res = BiliApiService.playerAPI.getDanmakuList(id).awaitCall()
            val body = res.body ?: return null
            val xmlBytes = CompressionTools.decompressXML(body.bytes())
            val dataSource = object : IDataSource<ByteArray> {
                override fun data() = xmlBytes
                override fun release() {}
            }
            val parser = BiliDanmakuParser()
            parser.load(dataSource)
            parser
        } catch (e: Exception) {
            null
        }
    }
}