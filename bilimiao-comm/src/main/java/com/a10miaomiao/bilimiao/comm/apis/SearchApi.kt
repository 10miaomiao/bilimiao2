package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class SearchApi {


    /**
     * 关键字列表
     */
    fun suggestList(keyword: String) = MiaoHttp.request {
        url = "https://s.search.bilibili.com/main/suggest?suggest_type=accurate&sub_type=tag&main_ver=v1&term=$keyword"
    }

    /**
     * 综合
     */
    fun searchArchive(
        keyword: String,
        pageNum: Int,
        pageSize: Int,
        order: String,
        duration: Int,
        rid: Int,
    ) = MiaoHttp.request {
        url = "https://app.bilibili.com/x/v2/search?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&duration=$duration&mobi_app=iphone&order=$order&platform=ios&rid=$rid&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    }

    /**
     * 番剧
     */
    fun searchBangumi(
        keyword: String,
        pageNum: Int,
        pageSize: Int
    ) = MiaoHttp.request{
        url = "https://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=1&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    }

    /**
     * UP主
     */
    fun searchUpper(
        keyword: String,
        pageNum: Int,
        pageSize: Int
    ) = MiaoHttp.request{
        url = "https://app.bilibili.com/x/v2/search/type?actionKey=appkey&appkey=27eb53fc9058f8c3&build=3710&device=phone&mobi_app=iphone&platform=ios&type=2&keyword=$keyword&pn=$pageNum&ps=$pageSize"
    }

}