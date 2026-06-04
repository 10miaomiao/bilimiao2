package com.a10miaomiao.bilimiao.comm.entity.article

import kotlinx.serialization.Serializable

@Serializable
data class ArticleInfo(
    val attention: Boolean,
    val author_name: String,
    val banner_url: String,
    val coin: Int,
    val favorite: Boolean,
    val image_urls: List<String>,
    val in_list: Boolean,
    val is_author: Boolean,
    val like: Int,
    val mid: Int,
    val next: Int,
    val origin_image_urls: List<String>,
    val pre: Int,
    val shareable: Boolean,
    val show_later_watch: Boolean,
    val show_small_window: Boolean,
    val title: String
)