package cn.a10miaomiao.bilimiao.compose.pages.community

data class ReplyEditParams(
    val type: Int,
    val oid: String,
    val root: String? = null,
    val parent: String? = null,
    val name: String = "",
)