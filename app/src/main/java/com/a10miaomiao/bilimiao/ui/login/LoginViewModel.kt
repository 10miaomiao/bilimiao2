package com.a10miaomiao.bilimiao.ui.login

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class LoginViewModel(
        val context: Context
) : ViewModel() {

    val userStore = MainActivity.of(context).userStore
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
        LoginHelper.login(-username, -password, -captcha)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val (code, data, message) = it
                    if (code == 0) {
                        val (access_token, refresh_token) = data
                        LoginHelper.saveToken(context, access_token, refresh_token)
                        authInfo(access_token)
                    } else if (code == -105) {
                        alert(if (-isCaptcha) "验证码不正确" else "请输入验证码")
                        captcha set ""
                        captchaUrl set getCaptchaUrl()
                        isCaptcha set true
                    } else {
                        alert(message)
                    }
                    DebugMiao.log(it)
                }
    }

    fun authInfo(access_token: String) {
        LoginHelper.authInfo(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val (code, data, message) = it
                    if (code == 0) {
                        LoginHelper.saveUserInfo(context, data)
                        userStore.setUserInfo(data)
                        MainActivity.of(context)
                                .pop()
                    } else {
                        context.alert {
                            title = message
                            negativeButton("确定") { }
                        }.show()
                    }
                }
    }

    private fun alert(_title: String) {
        context.alert {
            title = _title
            negativeButton("确定") { }
        }.show()
    }

    fun getCaptchaUrl() = "https://passport.bilibili.com/captcha?ts=${ApiHelper.getTimeSpen()}"

}