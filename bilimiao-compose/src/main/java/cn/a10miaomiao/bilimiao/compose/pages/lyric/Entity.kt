package cn.a10miaomiao.bilimiao.compose.pages.lyric


//网易云搜索结果
internal data class NeteaseSearchResultInfo(
    val result:NeteaseSearchResult,
    val code:Int,
)
internal data class NeteaseSearchResult(
    val songs:List<NeteaseSong>,
    val songCount:Int,
)
internal data class NeteaseSong(
    val id:String,
    val name:String,
    val duration: Int,
)


//网易云歌词
internal data class NeteaseLyricItem (
    val lyricVersion: Int,
    val lyric: String,
    val code: Int,
)


//酷狗搜索结果
internal data class KugouSearchResultInfo(
    val data:KugouResultData,
    val errcode:Int,
    val status:Int,
    val error:String,
)
internal data class KugouResultData(
    val timestamp:Int,
    val info:List<KugouResultItemInfo>,
)
internal data class KugouResultItemInfo(
    val hash: String,
    val filename: String,
    val duration: Int,
)

//酷狗accesskey
internal data class KugouAccessKeyItem(
    val errcode:Int,
    val errmsg:String,
    val candidates:List<KugouCandidates>
)
internal data class KugouCandidates(
    val id:Int,
    val accesskey:String,
)

//酷狗歌词
internal data class KugouLyricItem(
    val error_code:Int,
    val fmt:String,
    val content:String,
    val info:String
)