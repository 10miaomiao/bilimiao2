package com.a10miaomiao.bilimiao.comm.entity.user

import kotlinx.serialization.Serializable

/**
// "mid": 6789810,
// "name": "10喵喵",
// "face": "http://i2.hdslb.com/bfs/face/a9c907d558e46fc3addf15a72cfb66d2d2a955bf.jpg",
// "coin": 446,
// "bcoin": 5,
// "sex": 0,
// "rank": 10000,
// "silence": 0,
// "show_videoup": 1,
// "show_creative": 1,
// "level": 5,
// "vip_type": 2,
// "audio_type": 0,
// "dynamic": 11,
// "following": 95,
// "follower": 10,
// "official_verify": {
//   "type": -1,
//   "desc": ""
// }
 */
@Serializable
data class UserInfo (
    var mid: Long,
    var name: String,
    var face: String,
    var coin: Double,
    var bcoin: Double,
    var sex: Int,
    var rank: Int,
    var silence: Int,
    var show_videoup: Int,
    var show_creative: Int,
    var level: Int,
    var vip_type: Int,
    var audio_type: Int,
    var dynamic: Int,
    var following: Int,
    var follower: Int
)