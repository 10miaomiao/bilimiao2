package cn.a10miaomiao.bilimiao.download.entry

import kotlinx.serialization.Serializable

@Serializable
sealed class BiliDownloadMediaFileInfo {

    open fun httpHeader(): Map<String, String> = emptyMap()

    @Serializable
    data class Type1(
        val available_period_milli: Int,
        val description: String,
        val format: String,
        val from: String? = null,
        val intact: Boolean,
        val is_downloaded: Boolean,
        val is_resolved: Boolean,
        val marlin_token: String,
        val need_login: Boolean,
        val need_vip: Boolean,
        val parse_timestamp_milli: Long,
        val player_codec_config_list: List<Type1PlayerCodecConfig>,
        val player_error: Int,
        val quality: Int,
        val segment_list: List<Type1Segment>,
        val time_length: Int,
        val type_tag: String? = null,
        val user_agent: String? = null,
        val referer: String? = null,
        val video_codec_id: Int,
        val video_project: Boolean
    ): BiliDownloadMediaFileInfo() {
        override fun httpHeader(): Map<String, String> {
            return mapOf(
                "Referer" to referer.orEmpty(),
                "User-Agent" to user_agent.orEmpty(),
            ).filterValues { it.isNotEmpty() }
        }
    }

    @Serializable
    data class Type1PlayerCodecConfig(
        val player: String,
        val use_ijk_media_codec: Boolean
    )

    @Serializable
    data class Type1Segment(
        val backup_urls: List<String>,
        val bytes: Long,
        val duration: Long = 0L,
        val md5: String,
        val meta_url: String,
        val order: Int,
        val url: String
    )

    @Serializable
    data class Type2(
        val duration: Long = 0L,
        val video: List<Type2File>,
        val audio: List<Type2File>?,
        val user_agent: String? = null,
        val referer: String? = null,
    ): BiliDownloadMediaFileInfo() {
        override fun httpHeader(): Map<String, String> {
            return mapOf(
                "Referer" to referer.orEmpty(),
                "User-Agent" to user_agent.orEmpty(),
            ).filterValues { it.isNotEmpty() }
        }
    }

    @Serializable
    data class Type2File(
        val id: Int,
        val base_url: String,
        val backup_url: List<String>? = null,
        val bandwidth: Int,
        val codecid: Int,
        var size: Long,
        val md5: String,
        val no_rexcode: Boolean,
        val frame_rate: String = "",
        val width: Int = 1,
        val height: Int = 1,
        val dash_drm_type: Int = 0,
    )

    @Serializable
    data class None(
        val message: String,
    ): BiliDownloadMediaFileInfo()

}