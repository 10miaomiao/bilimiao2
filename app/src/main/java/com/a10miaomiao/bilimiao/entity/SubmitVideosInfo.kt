package com.a10miaomiao.bilimiao.entity

/**
 * Created by 10喵喵 on 2017/10/30.
 */
data class SubmitVideosInfo (
        var data : SubmitVideosDataInfo,
        var status : Boolean
){
    data class SubmitVideosDataInfo(
            var count : Int,
            var vlist : List<VideoInfo>
    )
    data class VideoInfo(
            var title : String,
            var aid : Int,
            var pic : String,
            var play : Int,
            var created : Long,
            var video_review : Int,
            var length : String
    )
}