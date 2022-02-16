package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class VideoAPI {

    /**
     * 视频信息
     */
    fun info(id: String, type: String = "AV",) = MiaoHttp.request {
        url = BiliApiService.biliApp("x/v2/view",
            (if (type == "AV") "aid" else "bvid") to id,
            "autoplay" to "0",
            "qn" to "32"
        )
    }

    /**
     * 视频评论
     */
    fun commentList(
        aid: String,
        sort: Int,
        pageNum: Int,
        pageSize: Int
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v2/reply",
            "oid" to aid,
            "plat" to "2",
            "sort" to sort.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "type" to "1"
        )
    }

    /**
     * 评论回复列表
     */
    fun commentReplyList(
        oid: String,
        rpid: String,
        minId: String?,
        pageSize: Int
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v2/reply/reply/cursor",
            "oid" to oid,
            "plat" to "2",
            "root" to rpid,
            "sort" to "0",
            "type" to "1",
            "minId" to (minId ?: ""),
//            "pn" to pageNum.toString(),
            "size" to pageSize.toString(),
        )
    }

}