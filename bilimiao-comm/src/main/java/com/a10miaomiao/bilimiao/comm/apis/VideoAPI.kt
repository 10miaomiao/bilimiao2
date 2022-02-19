package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
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

    /**
     * 点👍
     */
    fun like(
        aid: String,
        dislike: Int,
        like: Int
    ) = MiaoHttp.request {
        method = "POST"
        url = BiliApiService.biliApp("x/v2/view/like")
        formBody = ApiHelper.createParams(
            "aid" to aid,
            "dislike" to dislike.toString(),
            "like" to like.toString()
        )
    }

    /**
     * 点👎
     */
    fun disLike(
        aid: String,
        dislike: Int,
        like: Int
    ) = MiaoHttp.request {
        method = "POST"
        url = BiliApiService.biliApp("x/v2/view/like")
        formBody = ApiHelper.createParams(
            "aid" to aid,
            "dislike" to dislike.toString(),
            "like" to like.toString()
        )
    }

    /**
     * 投币
     */
    fun coin(
        aid: String,
        num: Int,
        select_like: Int = 0
    ) = MiaoHttp.request {
        method = "POST"
        url = BiliApiService.biliApp("x/v2/view/coin/add")
        formBody = ApiHelper.createParams(
            "aid" to aid,
            "multiply" to num.toString(),
            "select_like" to select_like.toString()
        )
    }

    /**
     * 一键三连
     */
    fun triple(
        aid: String
    ) = MiaoHttp.request {
        method = "POST"
        url = BiliApiService.biliApp("x/v2/view/like/triple")
        formBody = ApiHelper.createParams(
            "aid" to aid
        )
    }

    fun favoriteCreated (
        aid: String
    ) = MiaoHttp.request {
        val mid = BilimiaoCommApp.commApp.loginInfo?.token_info?.let {
            it.mid.toString()
        } ?: ""
        url = BiliApiService.biliApi(
            "medialist/gateway/base/created",
            "up_mid" to mid,
            "rid" to aid,
            "type" to "2",
            "pn" to "1",
            "ps" to "100"
        )
    }

    fun favoriteDeal(
        aid: String,
        addIds: List<String>,
        delIds: List<String>
    ) = MiaoHttp.request {
        method = "POST"
        url = BiliApiService.biliApi("medialist/gateway/coll/resource/deal")
        formBody = ApiHelper.createParams(
            "add_media_ids" to StringBuilder("").apply {
                addIds.forEachIndexed { index, s ->
                    append(if (index == 0) s else ",$s")
                }
            }.toString(),
            "del_media_ids" to StringBuilder("").apply {
                delIds.forEachIndexed { index, s ->
                    append(if (index == 0) s else ",$s")
                }
            }.toString(),
            "rid" to aid,
            "type" to "2"
        )
    }


}