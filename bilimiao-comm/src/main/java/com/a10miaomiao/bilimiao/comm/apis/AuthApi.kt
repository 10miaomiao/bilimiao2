package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import java.util.*

class AuthApi {

    fun account() = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/account/mine",
        )
    }

//    fun authInfo(access_token: String): Observable<ResultInfo<UserInfo>> {
//        var params = mapOf(
//            "appkey" to ApiHelper.APP_KEY_NEW,
//            "access_key" to access_token,
//            "build" to "5310300",
//            "mobi_app" to "android",
//            "platform" to "android",
//            "ts" to ApiHelper.getTimeSpen().toString()
//        )
//        var url = "https://app.bilibili.com/x/v2/account/mine?" + ApiHelper.urlencode(params)
//        url += "&sign=" + ApiHelper.getNewSign(url)
//        return MiaoHttp.getJson(url)
//    }

    fun oauth2() = MiaoHttp.request {
        url = BiliApiService.createUrl(
            "https://passport.bilibili.com/api/oauth2/info"
        )
    }

    fun refreshToken(refreshToken: String) = MiaoHttp.request {
        url = "https://passport.bilibili.com/api/oauth2/refreshToken"
        formBody = ApiHelper.createParams(
            "refresh_token" to refreshToken
        )
        method = MiaoHttp.POST
    }

}