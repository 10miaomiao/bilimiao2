package com.a10miaomiao.bilimiao.entity

/**
 * 视频推荐（广告）
 */
data class MiaoAdInfo (
     var code: Int,
     var msg: String,
     var data: DataBean
){
    data class DataBean(
            var isShow: Boolean,
            var title: String,
            var link: LinkBean
    )
    data class LinkBean(
        var text: String,
        var url: String
    )
}