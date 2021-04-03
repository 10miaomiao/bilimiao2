package cn.a10miaomiao.download

/**
 * {
"from": "vupload",
"quality": 112,
"type_tag": "lua.hdflv2.bili2api.112",
"description": "高清 1080P+",
"segment_list": [{
"url": "https:\/\/upos-sz-mirrorks3.bilivideo.com\/upgcxcode\/26\/81\/35958126\/35958126-1-112.flv?e=ig8euxZM2rNcNbUVhwdVhwdl7WdVhwdVhoNvNC8BqJIzNbfqXBvEuENvNC8aNEVEtEvE9IMvXBvE2ENvNCImNEVEIj0Y2J_aug859r1qXg8xNEVE5XREto8GuFGv2U7SuxI72X6fTr859IB_&uipk=5&nbs=1&deadline=1589838461&gen=playurl&os=ks3bv&oi=2005332791&trid=886d4b218eff4f9d9f2740ddc5262fbeu&platform=android&upsig=36d741bdd8021a061af5cc21338ba326&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,platform&mid=6789810&logo=90000000",
"duration": 188304,
"bytes": 93959900,
"meta_url": "",
"md5": "bdd57d6ce7d87db9af037efae53351d6",
"order": 0,
"backup_urls": ["https:\/\/upos-sz-mirrorks3c.bilivideo.com\/upgcxcode\/26\/81\/35958126\/35958126-1-112.flv?e=ig8euxZM2rNcNbUVhwdVhwdl7WdVhwdVhoNvNC8BqJIzNbfqXBvEuENvNC8aNEVEtEvE9IMvXBvE2ENvNCImNEVEIj0Y2J_aug859r1qXg8xNEVE5XREto8GuFGv2U7SuxI72X6fTr859IB_&uipk=5&nbs=1&deadline=1589838461&gen=playurl&os=ks3cbv&oi=2005332791&trid=886d4b218eff4f9d9f2740ddc5262fbeu&platform=android&upsig=55503c02f4b7a85bb53edb4ca519734d&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,platform&mid=6789810&logo=50000000"]
}],
"parse_timestamp_milli": 1589831261829,
"available_period_milli": 3600000,
"user_agent": "Bilibili Freedoooooom\/MarkII",
"is_downloaded": false,
"is_resolved": true,
"player_codec_config_list": [{
"use_ijk_media_codec": false,
"player": "IJK_PLAYER"
}, {
"use_ijk_media_codec": false,
"player": "ANDROID_PLAYER"
}],
"time_length": 188304,
"marlin_token": "",
"video_codec_id": 7,
"video_project": true,
"format": "",
"player_error": 0,
"need_vip": false,
"need_login": false,
"intact": false
}*/

data class BiliVideoPlayUrlEntry(
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
        val player_codec_config_list: List<PlayerCodecConfig>,
        val player_error: Int,
        val quality: Int,
        val segment_list: List<Segment>,
        val time_length: Int,
        val type_tag: String,
        val user_agent: String,
        val video_codec_id: Int,
        val video_project: Boolean
)

data class PlayerCodecConfig(
        val player: String,
        val use_ijk_media_codec: Boolean
)

data class Segment(
        val backup_urls: List<String>,
        val bytes: Long,
        val duration: Long,
        val md5: String,
        val meta_url: String,
        val order: Int,
        val url: String
)