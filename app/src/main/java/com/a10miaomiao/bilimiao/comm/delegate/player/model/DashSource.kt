package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class DashSource(
    val quality: Int,
    val dashData: PlayerAPI.Dash
) {

    private fun getDashVideo(): PlayerAPI.DashItem? {
        val videoList = dashData.video
        var conditionStreams = videoList.find { it.id == quality }
        if (conditionStreams != null) {
            return conditionStreams
        } else if (videoList.isNotEmpty()) {
            return videoList[videoList.size - 1]
        }
        return null
    }

    private fun getDashAudio(): PlayerAPI.DashItem? {
        val audioList = dashData.audio
        if (audioList.isNotEmpty()) {
            return audioList[0]
        }
        return null
    }


    fun getMDPUrl(): String {
        val video = getDashVideo()!!
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
                """
                 <AdaptationSet>
                    <ContentComponent contentType="audio" id="2" />
                    <Representation bandwidth="${audio.bandwidth}" codecs="${audio.codecs}" id="${audio.id}" mimeType="${audio.mime_type}" >
                        <BaseURL>${audio.base_url.replace("&", "&amp;")}</BaseURL>
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
        val url = video.base_url
        return "[dash-mpd]\n" + url + "\n" + mpdStr.replace("\n", "")
    }

}