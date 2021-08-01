package com.a10miaomiao.bilimiao.netword.api

import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp

class FavoriteApi {

    fun getCreated (
        aid: String
    ) = MiaoHttp.request {
        val mid = Bilimiao.app.loginInfo?.token_info?.let {
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

    fun deal(
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