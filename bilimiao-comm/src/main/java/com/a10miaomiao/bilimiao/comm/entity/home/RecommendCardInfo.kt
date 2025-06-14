package com.a10miaomiao.bilimiao.comm.entity.home

import kotlinx.serialization.Serializable

@Serializable
data class RecommendCardInfo (
    // small_cover_v2
    val card_type: String,
    // av/bv?/picture/bangumi/live
    val card_goto: String,
    val goto: String? = null,
    val param: String = "",
    val cover: String? = null,
    val title: String = "",
    val uri: String = "",
    val idx: Long,
    val track_id: String? = null,
    val talk_back: String? = null,
    val cover_left_text_1: String? = null,
    val cover_left_icon_1: Int = 0,
    val cover_left_1_content_description: String? = null,
    val cover_left_text_2: String? = null,
    val cover_left_icon_2: Int = 0,
    val cover_left_2_content_description: String? = null,
    val cover_right_text: String? = null,
    val cover_right_content_description: String? = null,
    val can_play: Int = 0,
    val three_point_v2: List<ThreePointV2Info>,
    val desc_button: DescButtonInfo? = null,
    val goto_icon: GotoIconInfo? = null,
    val args: RecommendCardArgsInfo? = null,
    val player_args: RecommendCardPlayerArgsInfo? = null,
) {
    @Serializable
    data class ThreePointV2Info(
        val title: String,
        /**
         * watch_later:稍后再看. feedback:反馈. dislike:不喜欢.
         */
        val type: String,
        val icon: String? = null,
        val reasons: List<DislikeReasonInfo>? = null,
    )
    @Serializable
    data class DislikeReasonInfo(
        val id: Int,
        val name: String,
        val toast: String,
    )
    @Serializable
    data class DescButtonInfo(
        val type: Int,
        val text: String,
        val uri: String,
        val event: String,
        val event_v2: String,
    )
    @Serializable
    data class GotoIconInfo(
        val icon_url: String,
        val icon_night_url: String,
        val icon_width: Int,
        val icon_height: Int,
    )
}