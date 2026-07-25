package cn.a10miaomiao.bilimiao.download.entry

import kotlinx.serialization.Serializable

@Serializable
data class BiliDownloadEntryInfo(
    val media_type: Int = 1,
    val has_dash_audio: Boolean = false,
    var is_completed: Boolean,
    var total_bytes: Long,
    var downloaded_bytes: Long,
    val title: String,
    val type_tag: String? = null,
    val cover: String,
    val video_quality: Int? = null,
    val prefered_video_quality: Int,
    val quality_pithy_description: String = "",
    val guessed_total_bytes: Int,
    var total_time_milli: Long,
    val danmaku_count: Int,
    val time_update_stamp: Long = 0L,
    val time_create_stamp: Long = 0L,
    val can_play_in_advance: Boolean = false,
    var interrupt_transform_temp_file: Boolean = false,
    val avid: Long? = null,
    val spid: Long? = null,
    val bvid: String? = null,
    val owner_id: Long? = null,
    var page_data: PageInfo? = null,
    val season_id: String? = null,
    val source: SourceInfo? = null,
    var ep: EpInfo? = null,
) {

    val key: Long
        get() {
            return source?.cid ?: page_data?.cid ?: avid!!
        }

    val name: String
        get() {
            val e = ep
            if (e != null) {
                return title + e.index_title
            }
            val p = page_data
            if (p != null) {
                return title + p.part
            }
            return title
        }

    val videoDirName: String
        get() = type_tag ?: video_quality.toString()

    // 视频分P信息
    @Serializable
    data class PageInfo(
        val cid: Long,
        val page: Int,
        val from: String? = null,
        val part: String? = null,
        val vid: String? = null,
        val has_alias: Boolean,
        val tid: Int,
        val width: Int = 0,
        val height: Int = 0,
        val rotate: Int = 0,
        val download_title: String? = null,
        val download_subtitle: String? = null
    )

    // 番剧源信息
    @Serializable
    data class SourceInfo(
        val av_id: Long,
        val cid: Long,
//        val website: String,
    )

    // 番剧剧集信息
    @Serializable
    data class EpInfo(
        val av_id: Long,
        val page: Int,
        val danmaku: Long,
        val cover: String,
        val episode_id: Long,
        val index: String,
        val index_title: String,
        val from: String,
        val season_type: Int,
        val width: Int,
        val height: Int,
        val rotate: Int,
        val link: String = "",
        val bvid: String = "",
        val sort_index: Int = 0,
    )

}