package com.a10miaomiao.bilimiao.comm.entity.home

data class WebRecommendInfo (
    val item: List<ItemInfo>,
    val mid: String,
) {
    /**
     *  "id": 220523564,
    "bvid": "BV1s8411L7kB",
    "cid": 903702775,
    "goto": "av",
    "uri": "https://www.bilibili.com/video/BV1s8411L7kB",
    "pic": "http://i1.hdslb.com/bfs/archive/ab3ef96ff169d5aea4181205ddc24e6e7111e61b.jpg",
    "title": "【4k】胡桃 但是很还原！",
    "duration": 90,
    "pubdate": 1669459010,
    "owner": {
    "mid": 142699547,
    "name": "摄影师618",
    "face": "https://i2.hdslb.com/bfs/face/ba7c11316df6cd57bc781ab60a1652356bcd1b4d.jpg"
    },
    "stat": {
    "view": 253960,
    "like": 18556,
    "danmaku": 212
    },
    "av_feature": null,
    "is_followed": 0,
    "rcmd_reason": {
    "content": "1万点赞",
    "reason_type": 3
    },
    "show_info": 1,
    "track_id": "web_pegasus_1.shylf-ai-recsys-647.1671022246864.462",
    "pos": 0,
    "room_info": null,
    "ogv_info": null,
    "business_info": null,
    "is_stock": 0
     */
    data class ItemInfo(
        val id: String,
        val bvid: String,
        val cid: String,
        val goto: String,
        val uri: String,
        val pic: String,
        val title: String,
        val duration: Long,
        val pubdate: String,
        val owner: OwnerInfo,
        val stat: StatInfo,
        val is_followed: Int,
    )

    data class OwnerInfo(
        val mid: String,
        val name: String,
        val face: String,
    )

    data class StatInfo(
        val view: String,
        val like: String,
        val danmaku: String,
    )
}