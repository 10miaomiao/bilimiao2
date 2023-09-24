package com.a10miaomiao.bilimiao.comm.entity.bangumi

data class SeasonV2UserStatus(
    val follow: Int,
    val follow_bubble: Int,
    val follow_status: Int,
    val pay: Int,
    val pay_for: Int,
//    "progress": {
//        "last_ep_id": 304193,
//        "last_ep_index": "OVA",
//        "last_time": 0
//    },
//    "review": {
//        "article_url": "https://member.bilibili.com/article-text/mobile?media_id=28225115",
//        "is_open": 1,
//        "score": 0
//    },
    val sponsor: Int,
    val vip: Int,
    val vip_frozen: Int,
)
