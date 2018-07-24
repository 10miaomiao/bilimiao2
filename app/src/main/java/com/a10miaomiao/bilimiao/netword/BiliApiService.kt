package com.a10miaomiao.bilimiao.netword

/**
 * Created by 10喵喵 on 2017/11/14.
 */
object BiliApiService {
    //-------------分区-----------------------
    /**
     * 获取分区视频列表
     */
    fun getRegionTypeVideoList(rid: Int,rankOrder: String,pageNum: Int,pageSize: Int,timeFrom: String,timeTo: String) =
        "https://s.search.bilibili.com/cate/search?main_ver=v3&search_type=video&view_type=hot_rank&order=$rankOrder&copy_right=-1&cate_id=$rid&page=$pageNum&pagesize=$pageSize&time_from=$timeFrom&time_to=$timeTo"

    //-------------视频信息部分-----------
    /**
     * 获取视频信息
     */
    fun getVideoInfo(aid: String): String{
        return "http://app.bilibili.com/x/view?_device=wp&_ulv=10000&access_key=&aid=$aid&appkey=${ApiHelper.appKey_Android}&build=411005&plat=4&platform=android&ts=${ApiHelper.getTimeSpen()}"
    }
    /**
     * 获取番剧信息
     */
    fun getBangumiInfo(aid: String): String{
        var url = "http://bangumi.bilibili.com/api/season_v3?_device=android&_ulv=10000&access_key=&appkey=${ApiHelper.appKey_Android}&build=411005&platform=android&season_id=$aid&ts=${ApiHelper.getTimeSpen()}&type=bangumi"
        //var url = "http://bangumi.bilibili.com/api/season_v3?_device=wp&access_key=${ApiHelper.appKey_Android}&_ulv=10000&build=411005&platform=android&appkey=422fd9d7289a1dd9&ts=${ApiHelper.GetTimeSpen()}000&type=bangumi&season_id=$aid"
        //val url = "http://bangumi.bilibili.com/api/season_v4?access_key=19946e1ef3b5cad1a756c475a67185bb&actionKey=appkey&appkey=27eb53fc9058f8c3&build=3940&device=phone&mobi_app=iphone&platform=ios&season_id=$aid&sign=3e5d4d7460961d9bab5da2341fd98dc1&ts=1477898526&type=bangumi"
        url += "&sign=" + ApiHelper.getSign_Android(url)
        return url;
    }
    /**
     * 获取直播信息
     */
    fun getLiveInfo(aid: String): String{
        var url = "http://live.bilibili.com/AppRoom/index?_device=android&access_key=&appkey=${ApiHelper.appKey_Android}&build=434000&buld=434000&jumpFrom=24000&mobi_app=android&platform=android&room_id=$aid&scale=xxhdpi"
        //var url = "http://bangumi.bilibili.com/api/season_v3?_device=android&_ulv=10000&access_key=&appkey=${ApiHelper.appKey_Android}&build=411005&platform=android&season_id=$aid&ts=${ApiHelper.GetTimeSpen()}&type=bangumi"
        url += "&sign=" + ApiHelper.getSign_Android(url)
        return url
    }

    /**
     * 获取音频信息
     */
    fun getAudioInfo(aid: String) = "https://m.bilibili.com/audio/au$aid"

    /**
     * 获取专栏信息
     */
    fun getCvInfo(aid: String) = "http://www.bilibili.com/read/mobile/$aid"

    /**
     * 获取弹幕列表
     */
    fun getDanmakuList(cid: String) = "http://comment.bilibili.com/$cid.xml"

    //-------------up主部分---------------
    /**
     * 获取up主的视频列表
     */
    fun getUpperVideo(mid: Int,pageNum: Int,pageSize: Int) =
            "https://space.bilibili.com/ajax/member/getSubmitVideos?mid=$mid&page=$pageNum&pagesize=$pageSize"
    /**
     * 获取up主的频道列表
     */
    fun getUpperChanne(mid: Int) =
            "https://api.bilibili.com/x/space/channel/index?mid=$mid&guest=false&jsonp=jsonp"
    /**
     * 获取up主频道的视频列表
     */
    fun getUpperChanneVideo(mid: Int,cid: Int,pageNum: Int,pageSize: Int) =
            "https://api.bilibili.com/x/space/channel/video?mid=$mid&cid=$cid&pn=$pageNum&ps=$pageSize&order=0&jsonp=jsonp"

    //------------搜索部分----------------
    /**
     * 综合
     */
    fun getSearchArchive(keyword: String,pageNum: Int,pageSize: Int,order: String,duration: Int,rid: Int) =
            "http://app.bilibili.com/x/v2/search?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&duration=$duration&mobi_app=iphone&order=$order&platform=ios&rid=$rid&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    /**
     * 番剧
     */
    fun getSearchBangumi(keyword: String,pageNum: Int,pageSize: Int) =
            "http://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=1&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    /**
     * 直播
     */
    fun getSearchUpper(keyword: String,pageNum: Int,pageSize: Int) =
            "http://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=2&keyword=$keyword&pn=$pageNum&ps=$pageSize"

    /**
     * 影视搜索
     */
    fun getSearchMovie(keyword: String,pageNum: Int,pageSize: Int) =
            "http://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=3&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    /**
     * 关键字列表
     */
    fun getKeyWord(keyword: String) =
            "http://s.search.bilibili.com/main/suggest?suggest_type=accurate&sub_type=tag&main_ver=v1&term=$keyword"
    //------------排行榜部分----------------
    /**
     * 全站
     * https://www.bilibili.com/index/rank/all-3-1.json  全部投稿
     * https://www.bilibili.com/index/rank/all-03-1.json   近期投稿
     */
    fun getRankAll(isall: Boolean,dayNum: Int,rid: Int) =
        if (isall)
            "https://www.bilibili.com/index/rank/all-$dayNum-$rid.json"
        else
            "https://www.bilibili.com/index/rank/all-0$dayNum-$rid.json"
    /**
     * 原创
     */
    fun getRankOrigin(isall: Boolean,dayNum: Int,rid: Int) =
            if (isall)
                "https://www.bilibili.com/index/rank/origin-$dayNum-$rid.json"
            else
                "https://www.bilibili.com/index/rank/origin-0$dayNum-$rid.json"
    /**
     * 新人
     */
    fun getRankRookie(isall: Boolean,dayNum: Int,rid: Int) =
            if (isall)
                "https://www.bilibili.com/index/rank/rookie-$dayNum-$rid.json"
            else
                "https://www.bilibili.com/index/rank/rookie-0$dayNum-$rid.json"

    /**
     * 番剧
     */
    fun getRankBangumi(region: String,dayNum: Int) =
            "https://bangumi.bilibili.com/jsonp/season_rank_list/$region/$dayNum.ver?callback=bangumiRankCallback&_=${ApiHelper.getTimeSpen()}"
}