package com.a10miaomiao.bilimiao.comm.delegate.player.entity

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil

class DashSource(
    val uposHost: String = "",
) {

    private fun codecidToCodecString(codecid: Int): String = when (codecid) {
        0 -> "mp4a.40.2"
        7 -> "avc1.640028"      // H.264
        12 -> "hev1.1.6.L120.90"// H.265
        13 -> "av01.0.01M.08.0.110.01.01.01.0"
        30216 -> "mp4a.40.2"    // AAC
        30232 -> "ec-3"         // EAC3
        else -> "avc1.640028"
    }

    private fun getSegmentBaseXml(
        segmentBase: SegmentBase?
    ): String {
        if (segmentBase == null) return ""
        return """
            <SegmentBase indexRange="${segmentBase.indexRange}">
                <Initialization range="${segmentBase.initialization}" />
            </SegmentBase>
        """.trimIndent()
    }

    fun replaceHostToUposHost(url: String): String {
        if (uposHost.isEmpty()) return url
        return UrlUtil.replaceHost(url, uposHost)
    }


    private fun getMDPUrl(
        video: DashItem,
        audio: DashItem?,
        duration: Long,
    ): String {
//        val videoContentProtection = if (!video.widevinePssh.isNullOrBlank()) {
//            """
//        <ContentProtection schemeIdUri="urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed">
//            <cenc:pssh xmlns:cenc="urn:mpeg:cenc:2013">${video.widevinePssh}</cenc:pssh>
//        </ContentProtection>
//        """.trimIndent()
//        } else ""
        val mpdStr = """
<MPD xmlns="urn:mpeg:DASH:schema:MPD:2011" profiles="urn:mpeg:dash:profile:isoff-on-demand:2011" type="static" mediaPresentationDuration="PT${duration}S" minBufferTime="PT1.5S">
    <Period start="PT0S">
        <AdaptationSet>
            <ContentComponent contentType="video" id="1" />
            <Representation bandwidth="${video.bandwidth}" codecs="${video.codecs}" height="${video.height}" id="${video.id}" mimeType="${video.mimeType}" width="${video.width}">
                <BaseURL></BaseURL>
            </Representation>
            ${getSegmentBaseXml(video.segmentBase)}
        </AdaptationSet>
        ${
            if (audio != null) {
                var audioUrl = audio.baseUrl
                """
                 <AdaptationSet>
                    <ContentComponent contentType="audio" id="2" />
                    <Representation bandwidth="${audio.bandwidth}" codecs="${audio.codecs}" id="${audio.id}" mimeType="${audio.mimeType}" >
                        <BaseURL>${replaceHostToUposHost(audioUrl).replace("&", "&amp;")}</BaseURL>
                    </Representation>
                    ${getSegmentBaseXml(audio.segmentBase)}
                </AdaptationSet>
                """.trimIndent()
            } else {
                ""
            }
        }
    </Period>
</MPD>
        """.trimIndent()
        var url = replaceHostToUposHost(video.baseUrl)
        return "[dash-mpd]\n" + url + "\n" + mpdStr.replace("\n", "")
    }

    fun getMDPUrl(
        videoId: Int,
        videoFormat: String,
        video: bilibili.pgc.gateway.player.v2.DashVideo,
        audio: bilibili.pgc.gateway.player.v2.DashItem?,
        durationMs: Long,
    ): String {
        return getMDPUrl(
            video = DashItem(
                id = videoId,
                baseUrl = video.baseUrl,
                backupUrl = video.backupUrl,
                bandwidth = video.bandwidth,
                codecsId = video.codecid,
                codecs = codecidToCodecString(video.codecid),
                width = video.width,
                height = video.height,
                mimeType = "video/${videoFormat}",
                frameRate = video.frameRate,
                minBufferTime = null,
                segmentBase = null,
            ),
            audio = audio?.let {
                DashItem(
                    id = it.id,
                    baseUrl = it.baseUrl,
                    backupUrl = it.backupUrl,
                    bandwidth = it.bandwidth,
                    codecsId = it.codecid,
                    codecs = codecidToCodecString(it.codecid),
                    width = 0,
                    height = 0,
                    mimeType = "audio/${videoFormat}",
                    frameRate = it.frameRate,
                    minBufferTime = null,
                    segmentBase = null,
                )
            },
            duration = durationMs / 1000,
        )
    }

    fun getMDPUrl(
        videoId: Int,
        videoFormat: String,
        video: bilibili.app.playurl.v1.DashVideo,
        audio: bilibili.app.playurl.v1.DashItem?,
        durationMs: Long,
    ): String {
        return getMDPUrl(
            video = DashItem(
                id = videoId,
                baseUrl = video.baseUrl,
                backupUrl = video.backupUrl,
                bandwidth = video.bandwidth,
                codecsId = video.codecid,
                codecs = codecidToCodecString(video.codecid),
                width = video.width,
                height = video.height,
                mimeType = "video/${videoFormat}",
                frameRate = video.frameRate,
                minBufferTime = null,
                segmentBase = null,
            ),
            audio = audio?.let {
                DashItem(
                    id = it.id,
                    baseUrl = it.baseUrl,
                    backupUrl = it.backupUrl,
                    bandwidth = it.bandwidth,
                    codecsId = it.codecid,
                    codecs = codecidToCodecString(it.codecid),
                    width = 0,
                    height = 0,
                    mimeType = "audio/${videoFormat}",
                    frameRate = it.frameRate,
                    minBufferTime = null,
                    segmentBase = null,
                )
            },
            duration = durationMs / 1000,
        )
    }

    fun getMDPUrl(
        dashData: PlayerAPI.Dash,
        quality: Int,
    ): String {
        val video = dashData.video.firstOrNull {
            it.id == quality
        } ?: dashData.video.lastOrNull() ?: return ""
        val audio = dashData.audio?.firstOrNull()
        return getMDPUrl(
            video = DashItem(
                id = video.id,
                baseUrl = video.base_url,
                backupUrl = video.backup_url ?: listOf(),
                bandwidth = video.bandwidth,
                codecsId = video.codecid,
                codecs = video.codecs,
                frameRate = video.frame_rate,
                width = video.width,
                height = video.height,
                mimeType = video.mime_type,
                minBufferTime = dashData.min_buffer_time,
                segmentBase = SegmentBase(
                    initialization = video.segment_base.initialization,
                    indexRange = video.segment_base.index_range,
                ),
            ),
            audio = audio?.let {
                DashItem(
                    id = it.id,
                    baseUrl = it.base_url,
                    backupUrl = it.backup_url ?: listOf(),
                    bandwidth = it.bandwidth,
                    codecsId = it.codecid,
                    codecs = it.codecs,
                    frameRate = it.frame_rate,
                    width = 0,
                    height = 0,
                    mimeType = it.mime_type,
                    minBufferTime = null,
                    segmentBase = SegmentBase(
                        initialization = it.segment_base.initialization,
                        indexRange = it.segment_base.index_range,
                    ),
                )
            },
            duration = dashData.duration,
        )
    }

    private data class DashItem(
        val id: Int,
        val baseUrl: String,
        val backupUrl: List<String>,
        val bandwidth: Int,
        val codecsId: Int,
        val codecs: String,
        val width: Int,
        val height: Int,
        val mimeType: String,
        val frameRate: String,
        val minBufferTime: Double?,
//        val widevinePssh: String?,
        val segmentBase: SegmentBase?,
    )

    private data class SegmentBase(
        val initialization: String,
        val indexRange: String,
    )


}