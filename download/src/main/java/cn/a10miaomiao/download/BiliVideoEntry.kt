package cn.a10miaomiao.download

/**
{
"media_type": 1, // 普通视频1
"has_dash_audio": false, // 未知false
"is_completed": true, // 是否完成，默认false
"total_bytes": 93959900, // 总大小
"downloaded_bytes": 93959900, // 已下载的大小
"title": "【少女与战车】红军最强大！！（Красная Армия всех сильней！！）", // 标题
"type_tag": "lua.hdflv2.bili2api.112", // 未知
"cover": "http:\/\/i1.hdslb.com\/bfs\/archive\/6b31b99a518c0f70ba7f554e014713aaa9592705.png", // 封面
"prefered_video_quality": 112,
"guessed_total_bytes": 0,
"total_time_milli": 188304,
"danmaku_count": 1000,
"time_update_stamp": 1589831292571,
"time_create_stamp": 1589831261539,
"can_play_in_advance": true,
"interrupt_transform_temp_file": false,
"avid": 21794985, // av号
"spid": 0, // 应该跟番剧有关
"seasion_id": 0,  // 应该跟番剧有关
"bvid": "BV1KW411K7DY",  // BV号
"owner_id": 13628080, // 普通视频是up主的uid
"page_data": {
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
}
 */
data class BiliVideoEntry (
        var media_type: Int,
        var has_dash_audio: Boolean,
        var is_completed: Boolean,
        var total_bytes: Long,
        var downloaded_bytes: Long,
        var title: String,
        var type_tag: String,
        var cover: String,
        var prefered_video_quality: Int,
        var guessed_total_bytes: Int,
        var total_time_milli: Long,
        var danmaku_count: Int,
        var time_update_stamp: Long,
        var time_create_stamp: Long,
        var can_play_in_advance: Boolean,
        var interrupt_transform_temp_file: Boolean,
        var avid: Long,
        var spid: Long,
        var seasion_id: Long,
        var bvid: String,
        var owner_id: Long,
        var page_data: BiliVideoPageData
)