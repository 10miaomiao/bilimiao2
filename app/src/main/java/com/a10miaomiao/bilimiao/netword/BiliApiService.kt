package com.a10miaomiao.bilimiao.netword

import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoInfo
import io.reactivex.Observable

/**
 * Created by 10喵喵 on 2017/11/14.
 */
object BiliApiService {

    fun createUrl(url: String, vararg pairs: Pair<String, String>): String {
        val params = ApiHelper.createParams(*pairs)
        return url + "?" + ApiHelper.urlencode(params)
    }

    fun biliApi(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://api.bilibili.com/$path", *pairs)
    }

    fun biliApp(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://app.bilibili.com/$path", *pairs)
    }

    fun biliBangumi(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://bangumi.bilibili.com/$path", *pairs)
    }


    //-------------分区-----------------------
    /**
     * 获取分区视频列表
     */
    fun getRegionTypeVideoList(rid: Int, rankOrder: String, pageNum: Int, pageSize: Int, timeFrom: String, timeTo: String) =
            "https://s.search.bilibili.com/cate/search?main_ver=v3&search_type=video&view_type=hot_rank&order=$rankOrder&copy_right=-1&cate_id=$rid&page=$pageNum&pagesize=$pageSize&time_from=$timeFrom&time_to=$timeTo"

    //-------------视频信息部分-----------
    /**
     * 获取视频信息
     */
    fun getVideoInfo(aid: String) = biliApp("x/v2/view",
            "aid" to aid,
            "autoplay" to "0",
            "qn" to "32")

    /**
     * 获取番剧信息
     */
    fun getBangumiInfo(seasonId: String) = biliBangumi("view/api/season",
            "season_id" to seasonId)

    /**
     * 单集番剧
     */
    fun getSeasonEpisodeInfo(id: String) = biliApi("pgc/view/app/season",
            "ep_id" to id)

    /**
     * 获取直播信息
     */
    fun getLiveInfo(aid: String): String {
        var url = "https://live.bilibili.com/AppRoom/index?_device=android&access_key=&appkey=${ApiHelper.appKey_Android}&build=434000&buld=434000&jumpFrom=24000&mobi_app=android&platform=android&room_id=$aid&scale=xxhdpi"
        url += "&sign=" + ApiHelper.getAndroidSign(url)
        return url
    }
    fun getRoomInfo(id: String) = createUrl("https://api.live.bilibili.com/room/v1/Room/get_info",
            "id" to id)

    /**
     * 获取音频信息
     */
    fun getAudioInfo(aid: String) = "https://www.bilibili.com/audio/music-service-c/web/song/info?sid=$aid"

    /**
     * 获取专栏信息
     */
    fun getCvInfo(aid: String): String {
        var url = "https://api.bilibili.com/x/article/viewinfo?appkey=1d8b6e7d45233436&build=5340000&id=$aid&mobi_app=android&platform=android&ts=${ApiHelper.getTimeSpen()}"
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    /**
     * 获取弹幕列表
     */
    fun getDanmakuList(cid: String) = "https://comment.bilibili.com/$cid.xml"


    /**
     * 视频评论
     */
    fun getCommentList(aid: String, minid: Int, pageSize: Int): String {
        // https://api.bilibili.com/x/v2/reply/cursor?appkey=1d8b6e7d45233436&build=5340000&max_id=218&mobi_app=android&oid=36286263&plat=2&platform=android&size=20&sort=0&ts=1543072948&type=1&sign=53df67ea7816a2bf644b49075bb1406e
        // https://api.bilibili.com/x/v2/reply/cursor?appkey=1d8b6e7d45233436&build=5340000&mobi_app=android&oid=36417189&plat=2&platform=android&size=20&sort=1543073266199&ts=1543072187&type=1&sign=49da459239fa0c249b95f322525b8636
        var url = "https://api.bilibili.com/x/v2/reply/cursor?appkey=1d8b6e7d45233436&build=5340000&mobi_app=android&oid=$aid&plat=2&platform=android&size=$pageSize&sort=0&ts=${ApiHelper.getTimeSpen()}&type=1"
        if (minid > 1)
            url += "&max_id=" + (minid - 1)
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    /**
     * 评论回复列表
     */
    fun getCommentReplyList(oid: String, rpid: String, min_id: Int, pageSize: Int): String {
        // GET /x/v2/reply/reply/cursor?appkey=1d8b6e7d45233436&build=5390000&channel=bilibili197&mobi_app=android&oid=49004387&plat=2&platform=android&root=1520702961&size=20&sort=0&ts=1555223623&type=1&sign=44e7d9738c1c171703f2c972c99d9679 HTTP/1.1
        // GET /x/v2/reply/reply/cursor?appkey=1d8b6e7d45233436&build=5390000&channel=bilibili197&min_id=22&mobi_app=android&oid=49004387&plat=2&platform=android&root=1520702961&size=20&sort=0&ts=1555223644&type=1&sign=f9cc0e37cae8498c02245cd6f76b949f HTTP/1.1
        // GET /x/v2/reply/reply/cursor?appkey=1d8b6e7d45233436&build=5390000&channel=bilibili197&min_id=42&mobi_app=android&oid=49004387&plat=2&platform=android&root=1520702961&size=20&sort=0&ts=1555223644&type=1&sign=1e1d083c4e9da2aac49cf173d8060a6c HTTP/1.1
        var url = "https://api.bilibili.com/x/v2/reply/reply/cursor?appkey=1d8b6e7d45233436&build=5390000&channel=bilibili197&mobi_app=android&oid=$oid&plat=2&platform=android&root=$rpid&size=$pageSize&sort=0&ts=${ApiHelper.getTimeSpen()}&type=1"
        if (min_id > 1)
            url += "&min_id=" + (min_id + 1)
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    //-------------up主部分---------------
    /**
     * 获取up主的视频列表
     */
    fun getUpperVideo(mid: Long, pageNum: Int, pageSize: Int) =
            "https://space.bilibili.com/ajax/member/getSubmitVideos?mid=$mid&page=$pageNum&pagesize=$pageSize"

    /**
     * 获取up主的频道列表
     */
    fun getUpperChanne(mid: Long) =
            "https://api.bilibili.com/x/space/channel/index?mid=$mid&guest=false&jsonp=jsonp"

    /**
     * 获取up主频道的视频列表
     */
    fun getUpperChanneVideo(mid: Int, cid: Int, pageNum: Int, pageSize: Int) =
            "https://api.bilibili.com/x/space/channel/video?mid=$mid&cid=$cid&pn=$pageNum&ps=$pageSize&order=0&jsonp=jsonp"

    //------------搜索部分----------------
    /**
     * 综合
     */
    fun getSearchArchive(keyword: String, pageNum: Int, pageSize: Int, order: String, duration: Int, rid: Int) =
            "https://app.bilibili.com/x/v2/search?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&duration=$duration&mobi_app=iphone&order=$order&platform=ios&rid=$rid&keyword=$keyword&pn=$pageNum&ps=$pageSize"

    /**
     * 番剧
     */
    fun getSearchBangumi(keyword: String, pageNum: Int, pageSize: Int) =
            "https://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=1&keyword=$keyword&pn=$pageNum&ps=$pageSize"

    /**
     * 直播
     */
    fun getSearchUpper(keyword: String, pageNum: Int, pageSize: Int) =
            "https://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=2&keyword=$keyword&pn=$pageNum&ps=$pageSize"

    /**
     * dili
     */
    fun getSearchDili(keyword: String, pageNum: Int, pageSize: Int) =
            "https://10miaomiao.cn/miao/bilimiao/search/bangumi?keyword=$keyword&pn=$pageNum&ps=$pageSize&v=0"


    /**
     * 影视搜索
     */
    fun getSearchMovie(keyword: String, pageNum: Int, pageSize: Int) =
            "https://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=3&keyword=$keyword&pn=$pageNum&ps=$pageSize"

    /**
     * 关键字列表
     */
    fun getKeyWord(keyword: String) =
            "https://s.search.bilibili.com/main/suggest?suggest_type=accurate&sub_type=tag&main_ver=v1&term=$keyword"

    fun getSpace(id: String) = biliApp("x/v2/space",
            "vmid" to id)

    /**
     * 收藏夹列表
     */
    fun gatMedialist() = biliApi("medialist/gateway/base/space")

    fun gatMedialist(up_mid: Long) = biliApi("medialist/gateway/base/space",
        "up_mid" to up_mid.toString())

    /**
     * 收藏夹列表详情
     */
    fun gatMedialistDetail(
            media_id: Long,
            pn: Int,
            ps: Int
    ) = biliApi(
            "medialist/gateway/base/detail",
            "media_id" to media_id.toString(),
            "pn" to pn.toString(),
            "ps" to ps.toString()
    )

    /**
     * 收藏番剧
     */
    fun getFollowBangumi(
            pn: Int,
            ps: Int
    )  = biliApi("pgc/app/follow/bangumi",
            "pn" to pn.toString(),
            "ps" to ps.toString()
    )

    fun getFollowBangumi(
            vmid: Long,
            pn: Int,
            ps: Int
    ) = biliApp("x/v2/space/bangumi",
            "vmid" to vmid.toString(),
            "pn" to pn.toString(),
            "ps" to ps.toString())


    fun oss() = createUrl("https://passport.bilibili.com/api/login/sso",
            "gourl" to "https://account.bilibili.com/account/home")
}