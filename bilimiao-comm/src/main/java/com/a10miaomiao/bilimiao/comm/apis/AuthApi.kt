package com.a10miaomiao.bilimiao.comm.apis

import android.webkit.CookieManager
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.ApiHelper.BILI_APP_VERSION
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.RSAUtil
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

    fun sso() = MiaoHttp.request {
        url = BiliApiService.createUrl("https://passport.bilibili.com/api/login/sso")
    }


    fun cookieInfo(
        biliJct: String,
        cookie: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/web/cookie/info?csrf=$biliJct"
        headers["cookie"] = cookie
    }

    /**
     * 验证码
     */
    fun captchaPre() = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/safecenter/captcha/pre"
        formBody = ApiHelper.createParams(
            "disable_rcmd" to "0",
        )
        method = MiaoHttp.POST
    }

    /**
     * 密码加密密钥
     */
    fun webKey() = MiaoHttp.request {
        url = BiliApiService.createUrl("https://passport.bilibili.com/x/passport-login/web/key",
            "disable_rcmd" to "0",
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),)
    }

    /**
     * 密码登录带验证码
     */
    fun oauth2Login(
        username: String,
        passport: String,
        key: String,
        rhash: String,
        geeChallenge: String,
        geeSeccode: String,
        geeValidate: String,
        recaptchaToken: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/oauth2/login"
        formBody = ApiHelper.createParams(
            "disable_rcmd" to "0",
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "password" to RSAUtil.rsaPassword(passport, key, rhash),
            "username" to username,
            "gee_type" to "10",
            "gee_challenge" to geeChallenge,
            "gee_seccode" to geeSeccode,
            "gee_validate" to geeValidate,
            "recaptcha_token" to recaptchaToken,
        )
        method = MiaoHttp.POST
    }

    /**
     * 密码登录
     */
    fun oauth2Login(
        username: String,
        passport: String,
        key: String,
        rhash: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/oauth2/login"
        formBody = ApiHelper.createParams(
            "disable_rcmd" to "0",
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "password" to RSAUtil.rsaPassword(passport, key, rhash),
            "username" to username,
        )
        method = MiaoHttp.POST
    }

    /**
     * 短信验证码发送
     */
    fun smsSend(
        tmpCode: String,
        geeChallenge: String,
        geeSeccode: String,
        geeValidate: String,
        recaptchaToken: String,
    ) = MiaoHttp.request{
        url = "https://passport.bilibili.com/x/safecenter/common/sms/send"
        formBody = ApiHelper.createParams(
            // type ：11
            "disable_rcmd" to "0",
            "sms_type" to "loginTelCheck",
            "tmp_code" to tmpCode,
            "gee_challenge" to geeChallenge,
            "gee_seccode" to geeSeccode,
            "gee_validate" to geeValidate,
            "recaptcha_token" to recaptchaToken,
        )
        method = MiaoHttp.POST
    }


    /**
     * 手机号验证登录
     */
    fun telVerify(
        code: String,
        tmpCode: String,
        requestId: String,
        source: String,
        captcha_key: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/safecenter/login/tel/verify"
        formBody = ApiHelper.createParams(
            "disable_rcmd" to "0",
            "type" to "loginTelCheck",
            "code" to code,
            "tmp_code" to tmpCode,
            "request_id" to requestId,
            "source" to source,
            "captcha_key" to captcha_key,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
        )
        method = MiaoHttp.POST
    }

    /**
     * 邮箱验证码发送
     */
    fun emailSend(
        tmpCode: String,
        geeChallenge: String,
        geeSeccode: String,
        geeValidate: String,
        recaptchaToken: String,
    ) = MiaoHttp.request{
        url = "https://passport.bilibili.com/x/safecenter/common/email/send"
        formBody = ApiHelper.createParams(
            "type" to "14",
            "tmp_code" to tmpCode,
            "gee_challenge" to geeChallenge,
            "gee_seccode" to geeSeccode,
            "gee_validate" to geeValidate,
            "recaptcha_token" to recaptchaToken,
        )
        method = MiaoHttp.POST
    }

    /**
     * 邮箱验证登录
     */
    fun emailVerify(
        code: String,
        tmpCode: String,
        requestId: String,
        source: String,
        captcha_key: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/safecenter/sec/verify"
        // verify_type=email
        formBody = ApiHelper.createParams(
            "verify_type" to "email",
            "code" to code,
            "tmp_code" to tmpCode,
            "request_id" to requestId,
            "source" to source,
            "captcha_key" to captcha_key,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
        )
        method = MiaoHttp.POST
    }

    fun oauth2AccessToken(
        code: String,
    )  = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/oauth2/access_token"
        formBody = ApiHelper.createParams(
            "disable_rcmd" to "0",
            "code" to code,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "grant_type" to "authorization_code",
        )
        method = MiaoHttp.POST
    }

    fun tmpUserInfo(
        tmpCode: String,
    ) = MiaoHttp.request {
        url = BiliApiService.createUrl("https://passport.bilibili.com/x/safecenter/user/info",
            "tmp_code" to tmpCode,
        )
    }

    /**
     * 短信验证码登录
     */
    fun smsLogin(
        cid: String, // 国际区号
        tel: String ,
        code: String ,
        captchaKey: String ,
        key: String,
    ) = MiaoHttp.request {
        val rsaKey = key.replace("-----BEGIN PUBLIC KEY-----\n", "")
            .replace("-----END PUBLIC KEY-----\n", "")
        url = "https://passport.bilibili.com/x/passport-login/login/sms"
        formBody = ApiHelper.createParams(
            "cid" to cid,
            "tel" to tel,
            "code" to code,
            "captcha_key" to captchaKey,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "password" to RSAUtil.decryptByPublicKey(ApiHelper.getUUID().substring(0..15), rsaKey),
        )
        method = MiaoHttp.POST
    }

    fun smsLoginSend(
        cid: String, // 国际区号
        tel: String,
        geeChallenge: String,
        geeSeccode: String,
        geeValidate: String,
        recaptchaToken: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/sms/send"
        formBody = ApiHelper.createParams(
            "cid" to cid,
            "tel" to tel,
            "gee_challenge" to geeChallenge,
            "gee_seccode" to geeSeccode,
            "gee_validate" to geeValidate,
            "recaptcha_token" to recaptchaToken,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
        )
        method = MiaoHttp.POST
    }

    fun smsLoginSend(
        cid: String, // 国际区号
        tel: String,
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-login/sms/send"
        formBody = ApiHelper.createParams(
            "cid" to cid,
            "tel" to tel,
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
        )
        method = MiaoHttp.POST
    }


    /**
     * 获取登录二维码
     */
    fun qrCode(
        loginSessionId: String
    ) = MiaoHttp.request {
        url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code"
        formBody = ApiHelper.createParams(
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "login_session_id" to loginSessionId,
            "spm_id" to "from_spmid"
        )
        headers["APP-KEY"] = "android_hd"
        method = MiaoHttp.POST
        // Response: QRLoginInfo
    }

    /**
     *
     */
    fun checkQrCode(
        authCode: String
    ) = MiaoHttp.request {
        url = BiliApiService.createUrl("https://passport.bilibili.com/x/passport-tv-login/qrcode/poll")
        formBody = ApiHelper.createParams(
            "local_id" to BilimiaoCommApp.commApp.getBilibiliBuvid(),
            "auth_code" to authCode
        )
        method = MiaoHttp.POST
        // Response: TokenInfo
    }

    fun confirmQRCode(
        authCode: String,
    ) = MiaoHttp.request {
        val cookieManager = CookieManager.getInstance()
        val cookie = cookieManager.getCookie("https://passport.bilibili.com/")
        val keyValueArr = cookie.split(";").map {
            it.trimIndent()
        }
        val biliJct = keyValueArr.find { it.startsWith("bili_jct=") }?.let {
            it.substring(9, it.length)
        } ?: ""
        url = "https://passport.bilibili.com/x/passport-tv-login/h5/qrcode/confirm"
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "auth_code" to authCode,
            "csrf" to biliJct,
            "build" to "7082000",
            "statistics" to """{"appId":6,"platform":3,"version":"7.82.0","abtest":""}""",
            appKey = "27eb53fc9058f8c3",
            appSecrer = "c2ed53a74eeefe3cf99fbd01d8c9c375"
        )

        headers["cookie"] = cookie
    }
}