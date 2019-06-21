package com.a10miaomiao.bilimiao.entity

/**
 * Created by 10喵喵 on 2017/12/2.
 */
data class VideoRankInfo (
        var rank: RankInfo
){
    data class RankInfo(
            var code: Int,
            var note: String,
            var num: Int,
            var pages: Int,
            var list: List<VideoInfo>
    )
    data class VideoInfo(
            var aid: String,
            var title: String,
            var author: String,
            var mid: Long,
            var coins: Int,//硬币数
            var duration: String,
            var pic: String,
            var play: String,
            var pts: Int,//分数
            var video_review: Int,//弹幕数
            var sort_num: Int = 0
    )
}