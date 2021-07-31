package com.a10miaomiao.bilimiao.netword.api

import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp

class VideoApi {

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

    

}