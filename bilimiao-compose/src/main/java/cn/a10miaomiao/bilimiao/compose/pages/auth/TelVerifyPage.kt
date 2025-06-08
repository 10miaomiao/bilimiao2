package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.auth.*
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
data class TelVerifyPage(
    private val code: String = "",
    private val requestId: String = "",
    private val source: String = "",
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: TelVerifyPageViewModel = diViewModel()
        LaunchedEffect(code, requestId, source) {
            viewModel.code = code
            viewModel.requestId = requestId
            viewModel.source = source
            viewModel.getTmpUserInfo()
        }
        TelVerifyPageCompose(viewModel)
    }

}

private class TelVerifyPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware, BiliGeetestUtil.GTCallBack {

    var code = ""
    var requestId = ""
    var source = ""
    var recaptchaToken = ""

    private var captchaKey = ""

    private val pageNavigation by instance<PageNavigation>()
    private val userStore by instance<UserStore>()
    private val messageDialog by instance<MessageDialogState>()

    private val biliGeetestUtil by instance<BiliGeetestUtil>()

    val verifyType = MutableStateFlow(VerifyType.TEL)
    val tmpAccountInfo = MutableStateFlow<TmpUserInfo.AccountInfo?>(null)
    val countdown = MutableStateFlow(0)
    val verifyCode = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    fun setVerifyCode(value: String) {
        verifyCode.value = value
    }

    fun startCountdown(second: Int) {
        countdown.value = second
        flow<Int> {
            for (i in second downTo 0) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Default)
            .onCompletion { countdown.value = 0 }
            .onEach { countdown.value = it }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    fun getTmpUserInfo() = viewModelScope.launch(Dispatchers.Main) {
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi.tmpUserInfo(tmpCode = code)
                    .awaitCall()
                    .json<ResponseData<TmpUserInfo>>()
            }
            if (res.isSuccess) {
                val info = res.requireData().account_info
                tmpAccountInfo.value = info
                if (info.bind_tel && info.tel_verify) {
                    verifyType.value = VerifyType.TEL
                } else if (info.bind_mail && info.mail_verify) {
                    verifyType.value = VerifyType.EMAIL
                } else {
                    messageDialog.alert(
                        text = "此帐号不支持手机号及邮箱验证，请去B站官方客户端或PC网页版完善帐号信息后再重新登录",
                        title = "不支持验证"
                    )
                    pageNavigation.popBackStack()
                }
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show("网络错误：$e")
            e.printStackTrace()
        }
    }

    fun verifyTel() = viewModelScope.launch(Dispatchers.Main) {
        try {
            if (verifyCode.value.isBlank()) {
                messageDialog.alert("请输入验证码")
                return@launch
            }
            loading.value = true
            val res = withContext(Dispatchers.IO) {
                if (verifyType.value == VerifyType.TEL) {
                    BiliApiService.authApi.telVerify(
                        code = verifyCode.value,
                        tmpCode = code,
                        requestId = requestId,
                        source = source,
                        captcha_key = captchaKey,
                    ).awaitCall().json<ResponseData<VerifyTelInfo>>(isLog = true)
                } else {
                    BiliApiService.authApi.emailVerify(
                        code = verifyCode.value,
                        tmpCode = code,
                        requestId = requestId,
                        source = source,
                        captcha_key = captchaKey,
                    ).awaitCall().json<ResponseData<VerifyTelInfo>>(isLog = true)
                }
            }
            if (res.isSuccess) {
                getOauth2AccessToken(res.requireData().code)
            } else {
                PopTip.show(res.message)
                loading.value = false
            }
        } catch (e: Exception) {
            PopTip.show("网络错误：$e")
            e.printStackTrace()
            loading.value = false
        }
    }

    fun getOauth2AccessToken(
        code: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.authApi
                .oauth2AccessToken(
                    code = code
                ).awaitCall().json<ResponseData<LoginInfo>>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val loginInfo = res.data!!
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                    authInfo()
                } else {
                    messageDialog.alert(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            messageDialog.alert(e.message ?: e.toString())
        } finally {
            loading.value = false
        }
    }

    private suspend fun authInfo() {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi
                .account()
                .awaitCall()
                .json<ResponseData<UserInfo>>()
        }
        if (res.isSuccess) {
            userStore.setUserInfo(res.data)
            pageNavigation.popBackStack()
        } else {
            throw Exception(res.message)
        }
    }

    fun setVerifyType(value: VerifyType) {
        verifyType.value = value
    }

    fun sendClick() {
        biliGeetestUtil.startCustomFlow(this@TelVerifyPageViewModel)
    }

    /**
     * 发送短信验证码
     */
    private suspend fun sendSms(
        result: BiliGeetestUtil.GT3ResultBean
    ): Boolean {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi.smsSend(
                tmpCode = code,
                geeChallenge = result.geetest_challenge,
                geeSeccode = result.geetest_seccode,
                geeValidate = result.geetest_validate,
                recaptchaToken = recaptchaToken,
            ).awaitCall().json<ResponseData<SmsSendInfo>>(isLog = true)
        }
        if (res.isSuccess) {
            startCountdown(60)
            captchaKey = res.requireData().captcha_key!!
            PopTip.show("已发送短信验证码")
            return true
        } else {
            PopTip.show(res.message)
            return false
        }
    }

    /**
     * 发送邮箱验证码
     */
    private suspend fun sendEmail(
        result: BiliGeetestUtil.GT3ResultBean
    ): Boolean {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi.emailSend(
                tmpCode = code,
                geeChallenge = result.geetest_challenge,
                geeSeccode = result.geetest_seccode,
                geeValidate = result.geetest_validate,
                recaptchaToken = recaptchaToken,
            ).awaitCall().json<ResponseData<SmsSendInfo>>()
        }
        if (res.isSuccess) {
            startCountdown(60)
            captchaKey = res.requireData().captcha_key!!
            PopTip.show("已发送邮箱验证码")
            return true
        } else {
            PopTip.show(res.message)
            return false
        }
    }

    /**
     * 验证码回调，发送验证码
     */
    override suspend fun onGTDialogResult(
        result: BiliGeetestUtil.GT3ResultBean
    ): Boolean {
        if (verifyType.value == VerifyType.TEL) {
            return sendSms(result)
        } else if (verifyType.value == VerifyType.EMAIL) {
            return sendEmail(result)
        } else {
            PopTip.show("未知类型")
            return false
        }
    }

    override suspend fun getGTApiJson(): JSONObject? {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi.captchaPre()
                .awaitCall()
                .json<ResponseData<CaptchaPreInfo>>()
        }
        if (res.isSuccess) {
            val resData = res.requireData()
            val geeGt = resData.gee_gt
            val geeChallenge = resData.gee_challenge
            recaptchaToken = resData.recaptcha_token
            return JSONObject().apply {
                put("success", 1)
                put("challenge", geeChallenge)
                put("gt", geeGt)
            }
        } else {
            return null
        }
    }

    enum class VerifyType {
        TEL,
        EMAIL,
    }

}

@Composable
private fun TelVerifyPageCompose(
    viewModel: TelVerifyPageViewModel
) {
    PageConfig(title = "帐号验证")

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val verifyType by viewModel.verifyType.collectAsState()
    val tmpAccountInfo by viewModel.tmpAccountInfo.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val verifyCode by viewModel.verifyCode.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val verifyCodeKeyboardActions = remember(viewModel) {
        KeyboardActions {
            viewModel.verifyTel()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
            Text(
                text = "帐号验证",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 45.dp, bottom = 20.dp)
                    .fillMaxWidth()
            )
            if (verifyType == TelVerifyPageViewModel.VerifyType.TEL) {
                Text(
                    text = "手机号：${tmpAccountInfo?.hide_tel}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .fillMaxWidth()
                )
            } else if (verifyType == TelVerifyPageViewModel.VerifyType.EMAIL) {
                Text(
                    text = "邮箱：${tmpAccountInfo?.hide_mail}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .fillMaxWidth()
                )
            }
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
            TextField(
                label = {
                    if (verifyType == TelVerifyPageViewModel.VerifyType.TEL) {
                        Text(text = "短信验证码")
                    } else if (verifyType == TelVerifyPageViewModel.VerifyType.EMAIL)  {
                        Text(text = "邮箱验证码")
                    } else {
                        Text(text = "验证码")
                    }
                },
                value = verifyCode,
                onValueChange = viewModel::setVerifyCode,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Box(modifier = Modifier.padding(end = 5.dp)) {
                        Button(
                            onClick = viewModel::sendClick,
                            enabled = countdown == 0,
                            modifier = Modifier.width(120.dp)
                        ) {
                            if (countdown == 0) {
                                Text(text = "获取验证码")
                            } else {
                                Text(text = countdown.toString() + "秒")
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = verifyCodeKeyboardActions,
            )

//                }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = viewModel::verifyTel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    text = "确认"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (verifyType == TelVerifyPageViewModel.VerifyType.TEL
                    && tmpAccountInfo?.bind_mail == true
                    && tmpAccountInfo?.mail_verify == true
                ) {
                    TextButton(onClick = {
                        viewModel.setVerifyType(TelVerifyPageViewModel.VerifyType.EMAIL)
                    }) {
                        Text(text = "使用邮箱验证")
                    }
                }
                if (verifyType == TelVerifyPageViewModel.VerifyType.EMAIL
                    && tmpAccountInfo?.bind_mail == true
                    && tmpAccountInfo?.mail_verify == true
                ) {
                    TextButton(onClick = {
                        viewModel.setVerifyType(TelVerifyPageViewModel.VerifyType.TEL)
                    }) {
                        Text(text = "使用手机号验证")
                    }
                }
            }
            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))
        }
    }

}