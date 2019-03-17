package com.a10miaomiao.bilimiao.entity


data class VideoComment(
        val assist: Int,
        val blacklist: Int,
        val config: Config,
        val cursor: Cursor,
        val hots: List<ReplyBean>,
        val notice: Any,
        val replies: List<ReplyBean>,
        val top: Top,
        val upper: Upper
){
    data class Member(
            val DisplayRank: String,
            val avatar: String,
            val fans_detail: Any,
            val following: Int,
            val level_info: LevelInfo,
            val mid: String,
            val nameplate: Nameplate,
            val official_verify: OfficialVerify,
            val pendant: Pendant,
            val rank: String,
            val sex: String,
            val sign: String,
            val uname: String,
            val vip: Vip
    )

    data class LevelInfo(
            val current_exp: Int,
            val current_level: Int,
            val current_min: Int,
            val next_exp: Int
    )

    data class Nameplate(
            val condition: String,
            val image: String,
            val image_small: String,
            val level: String,
            val name: String,
            val nid: Int
    )

    data class OfficialVerify(
            val desc: String,
            val type: Int
    )

    data class Pendant(
            val expire: Int,
            val image: String,
            val name: String,
            val pid: Int
    )

    data class Vip(
            val accessStatus: Int,
            val dueRemark: String,
            val vipDueDate: Long,
            val vipStatus: Int,
            val vipStatusWarn: String,
            val vipType: Int
    )

    data class ReplyBean(
            val action: Int,
            val assist: Int,
            val attr: Int,
            val content: Content,
            val count: Int,
            val ctime: Long,
            val dialog: Int,
            val dialog_str: String,
            val fansgrade: Int,
            val floor: Int,
            val like: Int,
            val member: Member,
            val mid: Int,
            val oid: Int,
            val parent: Int,
            val parent_str: String,
            val rcount: Int,
            val replies: List<Any>,
            val root: Int,
            val root_str: String,
            val rpid: Int,
            val rpid_str: String,
            val state: Int,
            val type: Int
    )

    data class Content(
            val device: String,
            val members: List<Any>,
            val message: String,
            val plat: Int
    )

    data class Top(
            val admin: Any,
            val upper: Any
    )

    data class Upper(
            val mid: Int
    )

    data class Cursor(
            val all_count: Int,
            val max_id: Int,
            val min_id: Int,
            val size: Int
    )

    data class Config(
            val showadmin: Int,
            val showentry: Int
    )
}