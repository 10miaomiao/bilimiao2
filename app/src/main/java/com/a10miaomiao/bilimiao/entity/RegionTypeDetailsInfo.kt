package com.a10miaomiao.bilimiao.entity

/**
 * Created by 10喵喵 on 2017/9/19.
 */
data class RegionTypeDetailsInfo(
        var code: Int,
        var result: List<Result>
){
    /**
     *arcurl:"http://www.bilibili.com/video/av11337775"
     *author:小小☆精灵"
     *id:11337775
     *pic:"//i0.hdslb.com/bfs/archive/166fb5c456c71b8a8ab6a203d26d3e9ae4a2cadf.jpg_160x100.jpg"
     *play:"221368"
     *video_review:2940
     *title:"【进击的巨人/史诗/AMV】 献出心脏——为自由而战！！！"
     */
    data class Result(
            var arcurl: String,
            var author: String,
            var id: String,
            var mid: String,
            var pic: String,
            var play: String,
            var title: String,
            var video_review: Int,
            var duration: Int
    )
}