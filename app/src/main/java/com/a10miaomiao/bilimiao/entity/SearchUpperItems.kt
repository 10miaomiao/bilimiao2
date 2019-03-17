package com.a10miaomiao.bilimiao.entity

import com.google.gson.annotations.SerializedName


data class SearchUpperItems(
        var title: String,
        var cover: String,
        var uri: String,
        var param: String,
        var goto: String,
        var total_count: Int,
        var sign: String,
        var fans: Int,
        var archives: Int,
        var status: Int
)
