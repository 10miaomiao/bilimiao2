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

//        mpdStr = """
//<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//<MPD id="f08e80da-bf1d-4e3d-8899-f0f6155f6efa" profiles="urn:mpeg:dash:profile:isoff-main:2011" type="static" availabilityStartTime="2015-08-04T09:33:14.000Z" publishTime="2015-08-04T10:47:32.000Z" mediaPresentationDuration="P0Y0M0DT0H3M30.000S" minBufferTime="P0Y0M0DT0H0M1.000S" bitmovin:version="1.6.0" xmlns:ns2="http://www.w3.org/1999/xlink" xmlns="urn:mpeg:dash:schema:mpd:2011" xmlns:bitmovin="http://www.bitmovin.net/mpd/2015">
//    <Period>
//        <AdaptationSet mimeType="video/mp4" codecs="avc1.42c00d">
//            <SegmentTemplate media="../video/${'$'}RepresentationID${'$'}/dash/segment_${'$'}Number${'$'}.m4s" initialization="../video/${'$'}RepresentationID${'$'}/dash/init.mp4" duration="100000" startNumber="0" timescale="25000"/>
//            <Representation id="180_250000" bandwidth="250000" width="320" height="180" frameRate="25"/>
//            <Representation id="270_400000" bandwidth="400000" width="480" height="270" frameRate="25"/>
//            <Representation id="360_800000" bandwidth="800000" width="640" height="360" frameRate="25"/>
//            <Representation id="540_1200000" bandwidth="1200000" width="960" height="540" frameRate="25"/>
//            <Representation id="720_2400000" bandwidth="2400000" width="1280" height="720" frameRate="25"/>
//            <Representation id="1080_4800000" bandwidth="4800000" width="1920" height="1080" frameRate="25"/>
//        </AdaptationSet>
//        <AdaptationSet lang="en" mimeType="audio/mp4" codecs="mp4a.40.2" bitmovin:label="English stereo">
//            <AudioChannelConfiguration schemeIdUri="urn:mpeg:dash:23003:3:audio_channel_configuration:2011" value="2"/>
//            <SegmentTemplate media="../audio/${'$'}RepresentationID${'$'}/dash/segment_${'$'}Number${'$'}.m4s" initialization="../audio/${'$'}RepresentationID${'$'}/dash/init.mp4" duration="191472" startNumber="0" timescale="48000"/>
//            <Representation id="1_stereo_128000" bandwidth="128000" audioSamplingRate="48000"/>
//        </AdaptationSet>
//    </Period>
//</MPD>
//        """.trimIndent()
//        val url = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd"
        val url = video.base_url
        DebugMiao.log(mpdStr)
        return url + "\n" + mpdStr.replace("\n", "")
    }

}