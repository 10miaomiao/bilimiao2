package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class UserApi {

    /**
     * 个人空间
     */
    fun space(id: String) = MiaoHttp.request {
        url = BiliApiService.biliApp("x/v2/space",
            "vmid" to id,
        )
    }

    /**
     * 获取up主的频道列表
     */
    fun upperChanne(mid: String) = MiaoHttp.request {
        url = "https://api.bilibili.com/x/space/channel/index?mid=$mid&guest=false&jsonp=jsonp"
    }

    /**
     * 获取up主的视频投稿
     */
    fun upperVideoList(
        mid: String,
        pageNum: Int,
        pageSize: Int,
        keyword: String = "",
        order: String = "pubdate",
    ) = MiaoHttp.request {
        url = "https://api.bilibili.com/x/space/arc/search?mid=$mid&pn=$pageNum&ps=$pageSize&keyword=${keyword}&order=${order}"
//        url = BiliApiService.biliApi("x/space/arc/search",
//            "mid" to mid,
//            "pn" to pageNum.toString(),
//            "ps" to pageSize.toString(),
//            "keyword" to keyword,
//            "order" to order
//        )
    }

    /**
     * 收藏夹列表
     */
    fun medialist() = MiaoHttp.request {
        url = BiliApiService.biliApi("medialist/gateway/base/space")
    }
    fun medialist(up_mid: String) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "medialist/gateway/base/space",
            "up_mid" to up_mid
        )
    }

    /**
     * 收藏夹列表详情
     */
    fun mediaDetail(
        media_id: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "medialist/gateway/base/detail",
            "media_id" to media_id,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }



}