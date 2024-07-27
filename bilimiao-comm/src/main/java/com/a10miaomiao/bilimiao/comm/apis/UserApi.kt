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

    // https://app.bilibili.com/x/v2/space/series?appkey=1d8b6e7d45233436&build=6740400&c_locale=zh-Hans_CN&channel=bili&disable_rcmd=0&fnval=16&fnver=0&force_host=0&fourk=0&mobi_app=android&next=0&platform=android&player_net=1&ps=10&qn=32&s_locale=zh-Hans_CN&series_id=931536&sort=desc&statistics=%7B%22appId%22%3A1%2C%22platform%22%3A3%2C%22version%22%3A%226.74.0%22%2C%22abtest%22%3A%22%22%7D&ts=1683944381&vmid=546195&sign=64a1160021d6563a84e96bd97ac655f0

    /**
     * 获取up主的合集列表
     */
    fun upperSeriesList(
        mid: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/polymer/space/seasons_series_list_mobile",
            "mid" to mid,
            "page_num" to pageNum.toString(),
            "page_size" to pageSize.toString(),
        )
    }

    fun medialistResourceList(
        bizId: String,
        type: String, // 5为系列系列(series)，8为合集(seasons)，其它暂时未知
        oid: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v2/medialist/resource/list",
            "biz_id" to bizId,
            "type" to type,
            "oid" to oid,
        )
    }

    /**
     * 用户空间的收藏夹列表
     */
    fun favSpaceFolder(
        up_mid: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v3/fav/folder/space",
            "up_mid" to up_mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 创建的收藏夹列表
     */
    fun favCreatedList(
        upMid: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v3/fav/folder/created/list",
            "up_mid" to upMid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 订阅的收藏夹列表
     */
    fun favCollectedList(
        upMid: String,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/v3/fav/folder/collected-with-resources",
            "up_mid" to upMid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "disable_rcmd" to "0",
        )
    }

    fun favAddFolder(
        title: String,
        cover: String,
        intro: String,
        privacy: Int, // 0:公开,1:不公开
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/folder/add")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "title" to title,
            "cover" to cover,
            "intro" to intro,
            "privacy" to privacy.toString(),
        )
    }

    fun favEditFolder(
        mediaId: String,
        title: String,
        cover: String,
        intro: String,
        privacy: Int, // 0:公开,1:不公开
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/folder/edit")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "media_id" to mediaId,
            "title" to title,
            "cover" to cover,
            "intro" to intro,
            "privacy" to privacy.toString(),
        )
    }

    fun favDeleteFolder(
        mediaIds: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/folder/del")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "media_ids" to mediaIds,
        )
    }

    fun favFavFolder(
        mediaId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/folder/fav")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "media_id" to mediaId,
        )
    }

    fun favUnfavFolder(
        mediaId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/folder/unfav")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "media_id" to mediaId,
        )
    }

    fun favFavSeason(
        seasonId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/season/fav")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
        )
    }

    fun favUnfavSeason(
        seasonId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/v3/fav/season/unfav")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
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

}