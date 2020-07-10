package com.a10miaomiao.bilimiao.ui.login

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.entity.LoginInfo
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.toast

class LoginViewModel(
        val context: Context
) : ViewModel() {

    val userStore = Store.from(context).userStore
    val username = MiaoLiveData("")
    val password = MiaoLiveData("")
    val captcha = MiaoLiveData("")
    val passwordHasFocus = MiaoLiveData(false)
    val isCaptcha = MiaoLiveData(false)
    val captchaUrl = MiaoLiveData(getCaptchaUrl())

    fun login() {
        if ((-username).isEmpty()) {
            alert("请输入用户名/邮箱/手机号")
            return
        }
        if ((-password).isEmpty()) {
            alert("请输入密码")
            return
        }
        if (-isCaptcha && (-captcha).isEmpty()) {
            alert("请输入验证码")
            return
        }
        val progressDialog = context.indeterminateProgressDialog("登陆中")
        LoginHelper.login(-username, -password, -captcha)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    val (code, data, message) = it
                    if (code == 0) {
                        authInfo(data)
                    } else if (code == -105) {
                        alert(if (-isCaptcha) "验证码不正确" else "请输入验证码")
                        captcha set ""
                        captchaUrl set getCaptchaUrl()
                        isCaptcha set true
                    } else {
                        alert(message)
                    }
                }, {
                    alert("网络错误")
                }, {
                    progressDialog.hide()
                })
    }

    fun authInfo(loginInfo: LoginInfo) {
        val progressDialog = context.indeterminateProgressDialog("获取信息中")
        val accessToken = loginInfo.token_info.access_token
        LoginHelper.authInfo(accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    val (code, data, message) = it
                    if (code == 0) {
                        Bilimiao.app.saveAuthInfo(loginInfo)
                        userStore.setUserInfo(data)
                        MainActivity.of(context)
                                .pop()
                    } else {
                        context.alert {
                            title = message
                            negativeButton("确定") { }
                        }.show()
                    }
                }, {
                    alert("网络错误")
                }, {
                    progressDialog.hide()
                })
    }

    private fun alert(_title: String) {
        context.alert {

            title = _title
            negativeButton("确定") { }
        }.show()
    }

    fun getCaptchaUrl() = "https://passport.bilibili.com/captcha?ts=${ApiHelper.getTimeSpen()}"

}