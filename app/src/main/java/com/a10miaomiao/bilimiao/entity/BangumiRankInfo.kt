package com.a10miaomiao.bilimiao.entity

/**
 * Created by 10喵喵 on 2017/12/2.
 */
data class BangumiRankInfo(
        var code: Int,
        var message: String,
        var result: Result
) {
    data class Result(
            var note: String,
            var list: List<BangumiInfo>
    )

    data class Ep(
            var cover: String,
            var index_show: String
    )
    data class BangumiInfo(
            var badge: String,
            var badge_type: Int,
            var copyright: String,
            var cover: String,
            var new_ep: Ep,
            var pts: Long, //分数
            var rank: Int,
            var season_id: String,
            var title: String,
            var url: String
    )
}