package com.a10miaomiao.bilimiao.entity

data class VideoInfo(
        val aid: Long,
        val attribute: Int,
        val bvid: String,
        val cid: Long,
        val cm_config: CmConfig,
        val cms: List<Cm>,
        val copyright: Int,
        val ctime: Double,
        var desc: String,
        val dimension: Dimension,
        val dislike_reasons: List<DislikeReason>,
        val dm_seg: Int,
        val duration: Int,
        val `dynamic`: String,
        val elec: Elec,
        val owner: Owner,
        val owner_ext: OwnerExt,
        val pages: List<Page>,
        val pic: String,
        val pubdate: Long,
        val relates: List<Relate>,
        val req_user: ReqUser,
        val rights: Rights,
        val staff: List<Staff>,
        val stat: Stat,
        val state: Int,
        val tag: List<Tag>,
        val tid: Int,
        val title: String,
        val tname: String,
        val videos: Int,
        val view_at: Long? // 历史记录的观看时间
)