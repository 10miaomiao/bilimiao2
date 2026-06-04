package cn.a10miaomiao.bilimiao.compose.pages.download

enum class DownloadType {
    VIDEO,
    BANGUMI
}
data class DownloadInfo(
    val dir_path: String,
    val media_type: Int,
    val has_dash_audio: Boolean,
    var is_completed: Boolean,
    val total_bytes: Long,
    val downloaded_bytes: Long,
    val title: String,
    val cover: String,
    val id: Long,
    val cid: Long,
    val type: DownloadType,
    val items: MutableList<DownloadItemInfo>,
//    val owner_id: Long,
)

data class DownloadItemInfo(
    val dir_path: String,
    val media_type: Int,
    val has_dash_audio: Boolean,
    val is_completed: Boolean,
    val total_bytes: Long,
    val downloaded_bytes: Long,
    val title: String,
    val cover: String,
    val id: Long,
    val type: DownloadType,
    val index_title: String,
    val cid: Long,
    val epid: Long,
)