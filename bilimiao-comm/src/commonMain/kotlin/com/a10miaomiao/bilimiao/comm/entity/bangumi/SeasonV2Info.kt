package com.a10miaomiao.bilimiao.comm.entity.bangumi

import com.a10miaomiao.bilimiao.comm.entity.comm.bili.BadgeInfo
import kotlinx.serialization.Serializable

@Serializable
data class SeasonV2Info(
    val actor: SeasonV2ActorInfo,
    val alias: String,
    val badge: String,
    val badge_info: BadgeInfo,
    val cover: String,
    val detail: String,
    val dynamic_subtitle: String,
    val enable_vt: Boolean,
    val evaluate: String,
    val link: String,
    val media_badge_info: BadgeInfo,
    val media_id: String,
    val mode: Int,
    val modules: List<SeasonV2ModuleInfo>,
    val new_ep: SeasonV2NewEpInfo,
    val new_keep_activity_material: SeasonV2NewKeepActivityMaterialInfo,
    val origin_name: String = "",

    val season_id: String,
    val season_title: String,
    val share_url: String,
    val short_link: String,
    val show_season_type: Int,
    val square_cover: String,

    val stat: SeasonV2StatInfo,
    val user_status: SeasonV2UserStatus,
)

