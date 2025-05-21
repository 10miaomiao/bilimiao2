package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaListV2Info(
    val attr: Int,
    val bv_id: String,
    val cnt_info: CntInfo,
    val coin: Coin,
    val copy_right: Int,
    val cover: String,
    val duration: Long,
//    val elec_info: Any,
    val fav_state: Int,
    val forbid_fav: Boolean,
    val id: String,
    val index: Int,
    val intro: String,
    val like_state: Int,
    val link: String,
    val offset: Int,
    val page: Int,
    val pages: List<Page>,
    val pubtime: Long,
    val rights: Rights,
    val short_link: String,
    val tid: Int,
    val title: String,
    val type: Int,
    val upper: Upper
){
    @Serializable
    data class CntInfo(
        val coin: Int,
        val collect: Int,
        val danmaku: Int,
        val play: Int,
        val play_switch: Int,
        val reply: Int,
        val share: Int,
        val thumb_down: Int,
        val thumb_up: Int,
        val view_text_1: String,
        val vt: Int
    )
    @Serializable
    data class Coin(
        val coin_number: Int,
        val max_num: Int
    )
    @Serializable
    data class Page(
        val dimension: Dimension,
        val duration: Int,
        val from: String,
        val id: String,
        val intro: String,
        val link: String,
        val metas: List<Meta>,
        val page: Int,
        val title: String
    )
    @Serializable
    data class Rights(
        val autoplay: Int,
        val bp: Int,
        val download: Int,
        val elec: Int,
        val hd5: Int,
        val movie: Int,
        val no_background: Int,
        val no_reprint: Int,
        val pay: Int,
        val ugc_pay: Int
    )
    @Serializable
    data class Upper(
        val display_name: String,
        val face: String,
        val fans: Int,
        val followed: Int,
        val mid: String,
        val name: String,
        val official_desc: String,
        val official_role: Int,
        val official_title: String,
        val vip_due_date: Long,
        val vip_pay_type: Int,
        val vip_statue: Int,
        val vip_type: Int
    )
    @Serializable
    data class Dimension(
        val height: Int,
        val rotate: Int,
        val width: Int
    )
    @Serializable
    data class Meta(
        val quality: Int,
        val size: Int
    )
}
