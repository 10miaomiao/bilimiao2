package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil

class DashSource(
    val quality: Int,
    val dashData: PlayerAPI.Dash,
    val uposHost: String = ""
) {

    fun getDashVideo(): PlayerAPI.DashItem? {
        val videoList = dashData.video
        var conditionStreams = videoList.find { it.id == quality }
        if (conditionStreams != null) {
            return conditionStreams
        } else if (videoList.isNotEmpty()) {
            return videoList[videoList.size - 1]
        }
        return null
    }

    fun getDashAudio(): PlayerAPI.DashItem? {
        val audioList = dashData.audio
        if (audioList?.isNotEmpty() == true) {
            return audioList[0]
        }
        return null
    }


    fun getMDPUrl(
        video: PlayerAPI.DashItem = getDashVideo()!!
    ): String {
        val audio = getDashAudio()
        var mpdStr = """
<MPD xmlns="urn:mpeg:DASH:schema:MPD:2011" profiles="urn:mpeg:dash:profile:isoff-on-demand:2011" type="static" mediaPresentationDuration="PT${dashData.duration}S" minBufferTime="PT${dashData.min_buffer_time}S">
    <Period start="PT0S">
        <AdaptationSet>
            <ContentComponent contentType="video" id="1" />
            <Representation bandwidth="${video.bandwidth}" codecs="${video.codecs}" height="${video.height}" id="${video.id}" mimeType="${video.mime_type}" width="${video.width}">
                <BaseURL></BaseURL>
                <SegmentBase indexRange="${video.segment_base.index_range}">
                    <Initialization range="${video.segment_base.initialization}" />
                </SegmentBase>
            </Representation>
        </AdaptationSet>
        ${
            if (audio != null) {
                var audioUrl = audio.base_url
                if (uposHost.isNotBlank()) {
                    audioUrl = UrlUtil.replaceHost(audioUrl, uposHost)
                }
                """
                 <AdaptationSet>
                    <ContentComponent contentType="audio" id="2" />
                    <Representation bandwidth="${audio.bandwidth}" codecs="${audio.codecs}" id="${audio.id}" mimeType="${audio.mime_type}" >
                        <BaseURL>${audioUrl.replace("&", "&amp;")}</BaseURL>
                        <SegmentBase indexRange="${audio.segment_base.index_range}">
                            <Initialization range="${audio.segment_base.initialization}" />
                        </SegmentBase>
                    </Representation>
                </AdaptationSet>
                """.trimIndent()
            } else {
                ""
            }
        }
    </Period>
</MPD>
        """.trimIndent()
        var url = video.base_url
        if (uposHost.isNotBlank()) {
            url = UrlUtil.replaceHost(url, uposHost)
        }
        return "[dash-mpd]\n" + url + "\n" + mpdStr.replace("\n", "")
    }

}