package com.a10miaomiao.bilimiao.entity

import com.google.gson.annotations.SerializedName

data class SearchBangumiItems(
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
        var total_count: Int
)