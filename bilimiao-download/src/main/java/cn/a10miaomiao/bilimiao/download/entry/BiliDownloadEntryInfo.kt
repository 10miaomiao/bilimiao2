package cn.a10miaomiao.bilimiao.download.entry

data class BiliDownloadEntryInfo(
    val media_type: Int,
    val has_dash_audio: Boolean,
    var is_completed: Boolean,
    var total_bytes: Long,
    var downloaded_bytes: Long,
    val title: String,
    val type_tag: String,
    val cover: String,
    val prefered_video_quality: Int,
    val quality_pithy_description: String,
    val guessed_total_bytes: Int,
    var total_time_milli: Long,
    val danmaku_count: Int,
    val time_update_stamp: Long,
    val time_create_stamp: Long,
    val can_play_in_advance: Boolean,
    var interrupt_transform_temp_file: Boolean,
    val avid: Long?,
    val spid: Long,
    val bvid: String?,
    val owner_id: Long,
    val page_data: PageInfo?,
    val season_id: String?,
    val source: SourceInfo?,
    val ep: EpInfo?,
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

    // 视频分P信息
    data class PageInfo(
        val cid: Long,
        val page: Int,
        val from: String,
        val part: String,
        val vid: String,
        val has_alias: Boolean,
        val tid: Int,
        val width: Int,
        val height: Int,
        val rotate: Int,
        val download_title: String,
        val download_subtitle: String
    )
    // 番剧源信息
    data class SourceInfo(
        val av_id: Long,
        val cid: Long,
        val website: String,
    )
    // 番剧剧集信息
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
        val link: String,
        val bvid: String,
        val sort_index: Int,
    )
}