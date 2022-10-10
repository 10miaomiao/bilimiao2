package com.a10miaomiao.bilimiao.comm.entity.video

/**
 * Created by 10喵喵 on 2017/10/30.
 */
data class SubmitVideosInfo (
    val list: ListBean,
    val page: PageBean,
) {
    data class ListBean (
        val vlist: List<DataBean>,
        val tlist: Map<String, RegionBean>?,
    )

    data class DataBean (
        val title : String,
        val aid : String,
        val pic : String,
        val play : String,
        val created : Long,
        val video_review : String,
        val length : String,
    )

    data class RegionBean(
        val tid : Int,
        val count : Int,
        val name : String,
    )

    data class PageBean(
        val pn : Int,
        val ps : Int,
        val count : Int,
    )
}