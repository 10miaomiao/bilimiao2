package cn.a10miaomiao.download

/**
 * {
"cid": 35958126,
"page": 1,
"from": "vupload",
"part": "红军最强大上传版本",
"vid": "",
"has_alias": false,
"tid": 24,
"width": 0,
"height": 0,
"rotate": 0,
"download_title": "视频已缓存完成",
"download_subtitle": "【少女与战车】红军最强大！！（Красная Армия всех	сильней！！） 红军最强大上传版本"
}
 */
data class BiliVideoPageData(
        var cid: Long,
        var page: Int,
        var from: String,
        var part: String,
        var vid: String,
        var has_alias: Boolean,
        var tid: Int,
        var width: Int,
        var height: Int,
        var rotate: Int,
        var download_title: String,
        var download_subtitle: String
)