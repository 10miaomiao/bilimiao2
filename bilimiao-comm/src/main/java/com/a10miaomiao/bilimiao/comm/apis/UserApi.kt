package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class UserApi {

    /**
     * 个人空间
     */
    fun space(id: String) = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/space",
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
        url =
            "https://api.bilibili.com/x/space/channel/video?mid=$mid&cid=$cid&pn=$pageNum&ps=$pageSize&order=0&jsonp=jsonp"
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
        url = BiliApiService.biliApi(
            "x/space/wbi/arc/search",
            "mid" to mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "tid" to tid.toString(),
            "keyword" to keyword,
            "order" to order,
            "notoken" to "1", // 不带token,匿名访问
        )
    }

    // https://app.bilibili.com/x/v2/space/series?appkey=1d8b6e7d45233436&build=6740400&c_locale=zh-Hans_CN&channel=bili&disable_rcmd=0&fnval=16&fnver=0&force_host=0&fourk=0&mobi_app=android&next=0&platform=android&player_net=1&ps=10&qn=32&s_locale=zh-Hans_CN&series_id=931536&sort=desc&statistics=%7B%22appId%22%3A1%2C%22platform%22%3A3%2C%22version%22%3A%226.74.0%22%2C%22abtest%22%3A%22%22%7D&ts=1683944381&vmid=546195&sign=64a1160021d6563a84e96bd97ac655f0

    /**
     * 获取up主的视频投稿
     */
    fun upperVideoList2(
        vmid: String,
        aid: String,
        pageSize: Int,
        keyword: String = "",
        order: String = "pubdate",
    ) = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/space/archive/cursor",
            "vmid" to vmid,
            "aid" to aid,
            "ps" to pageSize.toString(),
            "keyword" to keyword,
            "order" to order,
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
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        // 用户空间 x/v3/fav/folder/space/v2
        url = BiliApiService.biliApi(
            "x/v3/fav/folder/created/list",
            "up_mid" to up_mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }


    /**
     * 收藏夹列表详情
     */
    fun mediaDetail(
        media_id: String,
        pageNum: Int,
        pageSize: Int,
        keyword: String = "",
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v3/fav/resource/list",
            "media_id" to media_id,
            "keyword" to keyword,
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
        url = BiliApiService.biliApi(
            "pgc/app/follow/bangumi",
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString()
        )
    }

    fun followBangumi(
        vmid: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/space/bangumi",
            "vmid" to vmid.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 历史记录
     */
    fun videoHistory(
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
     * 稍后再看
     */
    fun videoHistoryToview(
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v2/history/toview",
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 视频添加稍后再看
     */
    fun videoHistoryToviewAdd(
        aid: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v2/history/toview/add")
        formBody = ApiHelper.createParams(
            "aid" to aid,
        )
        method = MiaoHttp.POST
    }

    /**
     * 删除稍后再看视频
     */
    fun videoHistoryToviewDel(
        aid: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v2/history/toview/del")
        formBody = ApiHelper.createParams(
            "aid" to aid,
        )
        method = MiaoHttp.POST
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
        order: String = "attention" // 最常访问排列：attention，关注顺序排列：留空
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/relation/followings",
            "vmid" to mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }

    /**
     * 关注分组
     */
    fun relationTags() = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags")
    }

    fun relationTagDetail(
        tagid: String, // 特别关注恒为-10,默认分组恒为0
        order: String = "attention", // 最常访问排列：attention，关注顺序排列：留空
        pageNum: Int = 1,
        pageSize: Int = 30,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag",
            "tagid" to tagid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }
}