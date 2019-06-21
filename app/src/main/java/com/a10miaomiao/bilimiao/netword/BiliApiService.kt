package com.a10miaomiao.bilimiao.netword

import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoInfo
import io.reactivex.Observable

/**
 * Created by 10喵喵 on 2017/11/14.
 */
object BiliApiService {
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
    fun getVideoInfo(aid: String): String {
//        "http://app.bilibili.com/x/view?access_key={0}&aid={1}&appkey=422fd9d7289a1dd9&build=510000&platform=android&ts={2}"
//        val url = "https://app.bilibili.com/x/view?_device=wp&_ulv=10000&access_key=&aid=$aid&appkey=${ApiHelper.appKey_Android}&build=411005&plat=4&platform=android&ts=${ApiHelper.getTimeSpen()}"
        var url = "https://app.bilibili.com//x/v2/view?aid=$aid&appkey=1d8b6e7d45233436&autoplay=0&build=5340000&fnval=16&fnver=0&from=52&mobi_app=android&plat=0&platform=android&qn=32&ts=${ApiHelper.getTimeSpen()}"
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    /**
     * 获取番剧信息
     */
    fun getBangumiInfo(aid: String): String {
        // var url = "https://bangumi.bilibili.com/api/season_v3?_device=android&_ulv=10000&access_key=&appkey=${ApiHelper.appKey_Android}&build=411005&platform=android&season_id=$aid&ts=${ApiHelper.getTimeSpen()}&type=bangumi"
        //var url = "http://bangumi.bilibili.com/api/season_v3?_device=wp&access_key=${ApiHelper.appKey_Android}&_ulv=10000&build=411005&platform=android&appkey=422fd9d7289a1dd9&ts=${ApiHelper.GetTimeSpen()}000&type=bangumi&season_id=$aid"
        //val url = "http://bangumi.bilibili.com/api/season_v4?access_key=19946e1ef3b5cad1a756c475a67185bb&actionKey=appkey&appkey=27eb53fc9058f8c3&build=3940&device=phone&mobi_app=iphone&platform=ios&season_id=$aid&sign=3e5d4d7460961d9bab5da2341fd98dc1&ts=1477898526&type=bangumi"
        var url = "https://bangumi.bilibili.com/view/api/season?access_key=&appkey=1d8b6e7d45233436&build=5310300&mobi_app=android&platform=android&season_id=$aid&ts=${ApiHelper.getTimeSpen()}"
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    /**
     * 单集番剧
     */
    fun getSeasonEpisodeInfo(id: String): String {
        var url = "https://api.bilibili.com/pgc/view/app/season?access_key=&appkey=1d8b6e7d45233436&build=5310300&ep_id=$id&mobi_app=android&platform=android&track_path=22&ts=${ApiHelper.getTimeSpen()}"
        url += "&sign=" + ApiHelper.getNewSign(url)
        return url
    }

    /**
     * 获取直播信息
     */
    fun getLiveInfo(aid: String): String {
        var url = "https://live.bilibili.com/AppRoom/index?_device=android&access_key=&appkey=${ApiHelper.appKey_Android}&build=434000&buld=434000&jumpFrom=24000&mobi_app=android&platform=android&room_id=$aid&scale=xxhdpi"
        //var url = "http://bangumi.bilibili.com/api/season_v3?_device=android&_ulv=10000&access_key=&appkey=${ApiHelper.appKey_Android}&build=411005&platform=android&season_id=$aid&ts=${ApiHelper.GetTimeSpen()}&type=bangumi"
        url += "&sign=" + ApiHelper.getAndroidSign(url)
        return url
    }

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
     * 直播
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
    //------------排行榜部分----------------
    /**
     * 全站
     * https://www.bilibili.com/index/rank/all-3-1.json  全部投稿
     * https://www.bilibili.com/index/rank/all-03-1.json   近期投稿
     */
    fun getRankAll(isall: Boolean, dayNum: Int, rid: Int) =
            if (isall)
                "https://www.bilibili.com/index/rank/all-$dayNum-$rid.json"
            else
                "https://www.bilibili.com/index/rank/all-0$dayNum-$rid.json"

    /**
     * 原创
     */
    fun getRankOrigin(isall: Boolean, dayNum: Int, rid: Int) =
            if (isall)
                "https://www.bilibili.com/index/rank/origin-$dayNum-$rid.json"
            else
                "https://www.bilibili.com/index/rank/origin-0$dayNum-$rid.json"

    /**
     * 新人
     */
    fun getRankRookie(isall: Boolean, dayNum: Int, rid: Int) =
            if (isall)
                "https://www.bilibili.com/index/rank/rookie-$dayNum-$rid.json"
            else
                "https://www.bilibili.com/index/rank/rookie-0$dayNum-$rid.json"

    /**
     * 番剧
     */
    fun getRankBangumi(region: String, dayNum: Int) =
            "https://bangumi.bilibili.com/jsonp/season_rank_list/$region/$dayNum.ver?callback=bangumiRankCallback&_=${ApiHelper.getTimeSpen()}"

    //播放地址
    fun getPlayUrl() {
//        val url =  "https://app.bilibili.com/x/playurl?device=android&expire=0&mobi_app=android&mid=0&appkey=iVGUTjsxvpLeuDCf&fnval=16&qn=32&npcybs=0&cid=70240545&otype=json&platform=android&ts=1549444302234&build=5340000&fnver=0&buvid=KREhESMULBkrGiNGOkY6QDgJblpoC3sRdQinfoc&aid=40000061&sign=befde9bca9ea3f55c33eaba77777359c"
    }


}