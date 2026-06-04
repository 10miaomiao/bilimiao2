package com.a10miaomiao.bilimiao.comm.entity.bangumi

data class BangumiUserStatusInfo(
    var follow: Int,
    val is_vip: Int,
    val pay: Int,
    val pay_pack_paid: Int,
    val sponsor: Int,
    val watch_progress: WatchProgressInfo,
) {
    data class WatchProgressInfo(
        val last_ep_id: Long,
        val last_ep_index: String,
        val last_time: Long,
    )
}
