package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.apis.*

object BiliApiService {
    fun createUrl(url: String, vararg pairs: Pair<String, String?>): String {
        val params = ApiHelper.createParams(*pairs)
        return url + "?" + ApiHelper.urlencode(params)
    }

    fun biliApi(path: String, vararg pairs: Pair<String, String?>): String {
        return createUrl("https://api.bilibili.com/$path", *pairs)
    }

    fun biliApp(path: String, vararg pairs: Pair<String, String?>): String {
        return createUrl("https://app.bilibili.com/$path", *pairs)
    }

    fun biliBangumi(path: String, vararg pairs: Pair<String, String?>): String {
        return createUrl("https://bangumi.bilibili.com/$path", *pairs)
    }

    val regionAPI = RegionAPI()
    val videoAPI = VideoAPI()
    val bangumiAPI = BangumiAPI()
    val commentApi = CommentApi()
    val searchApi = SearchApi()
    val playerAPI = PlayerAPI()
    val userApi = UserApi()
    val userRelationApi = UserRelationApi()
    val messageApi = MessageAPI()
    val authApi = AuthApi()
    val homeApi = HomeApi()
    val archiveApi = ArchiveApi()
}