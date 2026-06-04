package com.a10miaomiao.bilimiao.comm.entity.player

data class PlayerV2Info(
    val aid: Int,
    val allow_bp: Boolean,
    val answer_status: Int,
    val bgm_info: Any,
    val block_time: Int,
    val bvid: String,
    val cid: Int,
    val fawkes: Fawkes,
    val guide_attention: List<Any>,
    val has_next: Boolean,
    val ip_info: IpInfo,
    val is_owner: Boolean,
    val is_ugc_pay_preview: Boolean,
    val jump_card: List<Any>,
    val last_play_cid: Int,
    val last_play_time: Int,
    val level_info: LevelInfo,
    val login_mid: Int,
    val login_mid_hash: String,
    val max_limit: Int,
    val name: String,
    val no_share: Boolean,
    val now_time: Int,
    val online_count: Int,
    val online_switch: OnlineSwitch,
    val operation_card: List<Any>,
    val options: Options,
    val page_no: Int,
    val pcdn_loader: PcdnLoader,
    val permission: String,
    val preview_toast: String,
    val role: String,
    val show_switch: ShowSwitch,
    val subtitle: Subtitle,
    val toast_block: Boolean,
//    val view_points: List<Any>,
    val vip: Vip
) {

    data class Fawkes(
        val config_version: Int,
        val ff_version: Int
    )

    data class IpInfo(
        val city: String,
        val country: String,
        val ip: String,
        val province: String,
        val zone_id: Int,
        val zone_ip: String
    )

    data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val level_up: Long,
        val next_exp: Long
    )

    data class OnlineSwitch(
        val enable_gray_dash_playback: String,
        val new_broadcast: String,
        val realtime_dm: String,
        val subtitle_submit_switch: String
    )

    data class Options(
        val is_360: Boolean,
        val without_vip: Boolean
    )

    data class PcdnLoader(
        val dash: Dash,
        val flv: Flv
    )

    data class ShowSwitch(
        val long_progress: Boolean
    )

    data class Subtitle(
        val allow_submit: Boolean,
        val lan: String,
        val lan_doc: String,
        val subtitles: List<SubtitleX>
    )

    data class Vip(
        val avatar_subscript: Int,
        val avatar_subscript_url: String,
        val due_date: Long,
        val label: Label,
        val nickname_color: String,
        val role: Int,
        val status: Int,
        val theme_type: Int,
        val tv_vip_pay_type: Int,
        val tv_vip_status: Int,
        val type: Int,
        val vip_pay_type: Int
    )

    data class Dash(
        val labels: Labels
    )

    data class Flv(
        val labels: Labels
    )

    data class Labels(
        val pcdn_group: String,
        val pcdn_stage: String,
        val pcdn_vendor: String,
        val pcdn_version: String,
        val pcdn_video_type: String
    )

    data class SubtitleX(
        val ai_status: Int,
        val ai_type: Int,
        val id: String,
        val id_str: String,
        val is_lock: Boolean,
        val lan: String,
        val lan_doc: String,
        val subtitle_url: String,
        val type: Int
    )

    data class Label(
        val bg_color: String,
        val bg_style: Int,
        val border_color: String,
        val img_label_uri_hans: String,
        val img_label_uri_hans_static: String,
        val img_label_uri_hant: String,
        val img_label_uri_hant_static: String,
        val label_theme: String,
        val path: String,
        val text: String,
        val text_color: String,
        val use_img_label: Boolean
    )

}