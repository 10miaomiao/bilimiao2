package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
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
     * 获取up主频道的视频列表
     */
    fun upperChanneVideo(mid: String, cid: String, pageNum: Int, pageSize: Int) = MiaoHttp.request {
        url = "https://api.bilibili.com/x/space/channel/video?mid=$mid&cid=$cid&pn=$pageNum&ps=$pageSize&order=0&jsonp=jsonp"
    }

    /**
     * 获取up主的视频投稿
     */
    fun upperVideoList(
        mid: String,
        pageNum: Int,
        pageSize: Int,
        tid: Int = 0,
        keyword: String = "",
        order: String = "pubdate",
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/space/arc/search",
            "mid" to mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "tid" to tid.toString(),
            "keyword" to keyword,
            "order" to order,
            "notoken" to "1", // 不带token,匿名访问
        )
    }


    /**
     * 收藏夹列表
     */
    fun medialist() = MiaoHttp.request {
        url = BiliApiService.biliApi("medialist/gateway/base/space")
    }
    fun medialist(up_mid: String) = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/favorite",
            "vmid" to up_mid
        )
    }

    fun favFolderList(
        up_mid: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v3/fav/folder/space/v2",
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
            "x/v3/fav/resource/list",
            "media_id" to media_id,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 追番列表
     */
    fun followBangumi(
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/bangumi",
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString()
        )
    }
    fun followBangumi(
        vmid: String,
        pageNum: Int,
        pageSize: Int,
    )  = MiaoHttp.request {
        url = BiliApiService.biliApp("x/v2/space/bangumi",
            "vmid" to vmid.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 历史记录
     */
    fun videoHistory (
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v2/history",
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * web端历史记录
     * 不支持token，需要cookie
     */
    fun webVideoHistory (
        max: Long,
        viewAt: Long,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/web-interface/history/cursor",
            "business" to "archive",
            "max" to max.toString(),
            "view_at" to viewAt.toString(),
        )
    }

    /**
     * 关注Up主
     */
    fun attention(
        mid: String,
        mode: Int, // 1为关注，2为取消关注
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/modify")
        formBody = ApiHelper.createParams(
            "fid" to mid,
            "act" to mode.toString(),
//            "re_src" to "32",
        )
        method = MiaoHttp.POST
    }

    /**
     * 关注的up主
     */
    fun followings(
        mid: String,
        pageNum: Int = 1,
        pageSize: Int = 30,
        keyword: String = "",
        order: String = "attention"
    ) = MiaoHttp.request {
            url = BiliApiService.biliApi("x/relation/followings",
            "vmid" to mid,
                "pn" to pageNum.toString(),
                "ps" to pageSize.toString(),
                "order_type" to order,
                "order" to "desc",
            )
    }

}