package com.a10miaomiao.bilimiao.entity.comment

data class ReplyRoot(
        val action: Int,
        val assist: Int,
        val attr: Int,
        val content: Content,
        val count: Int,
        val ctime: Int,
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
        val replies: List<ReplyBean>,
        val root: Int,
        val root_str: String,
        val rpid: Int,
        val rpid_str: String,
        val state: Int,
        val type: Int
//        val up_action: UpAction
)
