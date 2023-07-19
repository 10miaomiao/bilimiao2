package cn.a10miaomiao.bilimiao.download.entry

sealed class BiliDownloadMediaFileInfo {

    data class Type1(
        val available_period_milli: Int,
        val description: String,
        val format: String,
        val from: String,
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
        val type_tag: String,
        val user_agent: String,
        val video_codec_id: Int,
        val video_project: Boolean
    ): BiliDownloadMediaFileInfo()

    data class Type1PlayerCodecConfig(
        val player: String,
        val use_ijk_media_codec: Boolean
    )

    data class Type1Segment(
        val backup_urls: List<String>,
        val bytes: Long,
        val duration: Long,
        val md5: String,
        val meta_url: String,
        val order: Int,
        val url: String
    )


    data class Type2(
        val duration: Long,
        val video: List<Type2File>,
        val audio: List<Type2File>?
    ): BiliDownloadMediaFileInfo()

    data class Type2File(
        val id: Int,
        val base_url: String,
        val backup_url: List<String>,
        val bandwidth: Int,
        val codecid: Int,
        var size: Long,
        val md5: String,
        val no_rexcode: Boolean,
        val frame_rate: String,
        val width: Int,
        val height: Int,
        val dash_drm_type: Int,
    )

    data class None(
        val message: String,
    ): BiliDownloadMediaFileInfo()

}