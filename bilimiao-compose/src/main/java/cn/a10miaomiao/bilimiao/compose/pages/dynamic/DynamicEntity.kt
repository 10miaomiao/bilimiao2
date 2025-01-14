package cn.a10miaomiao.bilimiao.compose.pages.dynamic

data class DynamicVideoInfo(
    val mid: String,
    val name: String,
    val face: String,
    val labelText: String,
    val locationText: String,
    val isLike: Boolean,
    val like: Long,
    val reply: Long,
    val share: Long,
    val dynamicType: Int,
    val dynamicContent: DynamicVideoContentInfo,
)

data class DynamicVideoContentInfo(
    val id: String,
    val title: String = "",
    val pic: String = "",
    val remark: String? = null,
    val duration: String? = null,
)