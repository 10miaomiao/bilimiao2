package com.a10miaomiao.bilimiao.comm.delegate.player.model

import android.net.Uri
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import java.io.File

class LocalDashSource(
    val videoDir: String,
    val dashData: PlayerAPI.Dash,
) {

    fun getMDPUrl(): String {
        val videoFile = File(videoDir, "video.m4s")
        val audioFile = File(videoDir, "audio.m4s")
        val url = Uri.fromFile(videoFile).toString()
        val audioUrl = Uri.fromFile(audioFile).toString()
        val video = dashData.video[0]
        val audio = dashData.audio[0]
        var mpdStr = """
<MPD xmlns="urn:mpeg:DASH:schema:MPD:2011" profiles="urn:mpeg:dash:profile:isoff-on-demand:2011" type="static" mediaPresentationDuration="PT${dashData.duration}S" minBufferTime="PT${dashData.min_buffer_time}S">
    <Period start="PT0S">
        <AdaptationSet>
            <ContentComponent contentType="video" id="1" />
            <Representation bandwidth="${video.bandwidth}" codecs="${video.codecs}" height="${video.height}" id="${video.id}" mimeType="${video.mime_type}" width="${video.width}">
                <BaseURL></BaseURL>
            </Representation>
        </AdaptationSet>
        ${
            if (audio != null) {
                """
                 <AdaptationSet>
                    <ContentComponent contentType="audio" id="2" />
                    <Representation bandwidth="${audio.bandwidth}" codecs="${audio.codecs}" id="${audio.id}" mimeType="${audio.mime_type}" >
                        <BaseURL>${audioUrl.replace("&", "&amp;")}</BaseURL>
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
        DebugMiao.log(mpdStr)
        return url + "\n" + mpdStr.replace("\n", "")
    }
}