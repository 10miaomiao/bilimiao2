package com.a10miaomiao.bilimiao.comm.entity.video

/**
 * Created by 10喵喵 on 2017/10/30.
 */
data class SubmitVideosInfo (
    var list : ListBean,
) {
    data class ListBean (
        var vlist: List<DataBean>,
    )

    data class DataBean (
        var title : String,
        var aid : String,
        var pic : String,
        var play : String,
        var created : Long,
        var video_review : String,
        var length : String,
    )
}