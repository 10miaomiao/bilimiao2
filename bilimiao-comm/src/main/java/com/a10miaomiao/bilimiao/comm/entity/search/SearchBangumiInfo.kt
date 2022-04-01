package com.a10miaomiao.bilimiao.comm.entity.search

data class SearchBangumiInfo(
    var title: String,
    var cover: String,
    var uri: String,
    var param: String,
    var goto: String,
    var finish: Int,
    var index: String,
    var newest_cat: String,
    var newest_season: String,
    var cat_desc: String,
    var total_count: Int,
)