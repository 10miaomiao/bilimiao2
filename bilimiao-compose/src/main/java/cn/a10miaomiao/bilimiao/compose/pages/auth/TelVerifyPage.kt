package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.PageRoute
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.*
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


class TelVerifyPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware, BiliGeetestUtil.GTCallBack {

    var code = ""
    var requestId = ""
    var source = ""
    var recaptchaToken = ""

    private var captchaKey = ""

    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()

    private val biliGeetestUtil =
        BiliGeetestUtil(fragment.requireActivity(), fragment.lifecycle, this)

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
                    .gson<ResultInfo<TmpUserInfo>>()
            }
            DebugMiao.log("getTmpUserInfo", res)
            if (res.isSuccess) {
                val info = res.data.account_info
                tmpAccountInfo.value = info
                if (info.bind_tel && info.tel_verify) {
                    verifyType.value = VerifyType.TEL
                } else if (info.bind_mail && info.mail_verify) {
                    verifyType.value = VerifyType.EMAIL
                } else {
                    MaterialAlertDialogBuilder(fragment.requireActivity()).apply {
                        setTitle("不支持验证")
                        setMessage("此帐号不支持手机号及邮箱验证，请去B站官方客户端或PC网页版完善帐号信息后再重新登录")
                        setNegativeButton("确定") { _, _ ->
                            fragment.findNavController().popBackStack()
                        }
                    }.show()
                }
            } else {
                Toast.makeText(fragment.requireActivity(), res.message, Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(fragment.requireActivity(), "网络错误：$e", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    fun verifyTel() = viewModelScope.launch(Dispatchers.Main) {
        try {
            if (verifyCode.value.isBlank()) {
                alert("请输入验证码")
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
                    ).awaitCall().gson<ResultInfo<VerifyTelInfo>>()
                } else {
                    BiliApiService.authApi.emailVerify(
                        code = verifyCode.value,
                        tmpCode = code,
                        requestId = requestId,
                        source = source,
                        captcha_key = captchaKey,
                    ).awaitCall().gson<ResultInfo<VerifyTelInfo>>()
                }
            }
            if (res.isSuccess) {
                getOauth2AccessToken(res.data.code)
            } else {
                Toast.makeText(fragment.requireActivity(), res.message, Toast.LENGTH_SHORT)
                    .show()
                loading.value = false
            }
        } catch (e: Exception) {
            Toast.makeText(fragment.requireActivity(), "网络错误：$e", Toast.LENGTH_SHORT)
                .show()
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
                ).awaitCall().gson<ResultInfo<LoginInfo>>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val loginInfo = res.data
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                    authInfo()
                } else {
                    alert(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                alert(e.message ?: e.toString())
            }
        } finally {
            loading.value = false
        }
    }

    private suspend fun authInfo() {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi
                .account()
                .awaitCall()
                .gson<ResultInfo<UserInfo>>()
        }
        if (res.isSuccess) {
            userStore.setUserInfo(res.data)
            fragment.findNavController().popBackStack()
        } else {
            throw Exception(res.message)
        }
    }

    private fun alert(title: String) {
        alert(title) {}
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun alert(title: String, initBuilder: (MaterialAlertDialogBuilder.() -> Unit)) {
        contract {
            callsInPlace(initBuilder, InvocationKind.EXACTLY_ONCE)
        }
        MaterialAlertDialogBuilder(fragment.requireActivity()).apply {
            setTitle(title)
            setNegativeButton("关闭", null)
            initBuilder()
        }.show()
    }

    fun setVerifyType(value: VerifyType) {
        verifyType.value = value
    }

    fun sendClick() {
        biliGeetestUtil.startCustomFlow()
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
            ).awaitCall().gson<ResultInfo<SmsSendInfo>>()
        }
        if (res.isSuccess) {
            startCountdown(60)
            captchaKey = res.data.captcha_key
            Toast.makeText(fragment.requireActivity(), "已发送短信验证码", Toast.LENGTH_SHORT)
                .show()
            return true
        } else {
            Toast.makeText(fragment.requireActivity(), res.message, Toast.LENGTH_SHORT)
                .show()
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
            ).awaitCall().gson<ResultInfo<SmsSendInfo>>()
        }
        if (res.isSuccess) {
            startCountdown(60)
            captchaKey = res.data.captcha_key
            Toast.makeText(fragment.requireActivity(), "已发送邮箱验证码", Toast.LENGTH_SHORT)
                .show()
            return true
        } else {
            Toast.makeText(fragment.requireActivity(), res.message, Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(fragment.requireActivity(), "未知类型", Toast.LENGTH_SHORT)
                .show()
            return false
        }
    }

    override suspend fun getGTApiJson(): JSONObject? {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi.captchaPre()
                .awaitCall()
                .gson<ResultInfo<CaptchaPreInfo>>()
        }
        if (res.isSuccess) {
            val geeGt = res.data.gee_gt
            val geeChallenge = res.data.gee_challenge
            recaptchaToken = res.data.recaptcha_token
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelVerifyPage(
    code: String,
    requestId: String,
    source: String,
) {
    PageConfig(title = "帐号验证")

    val viewModel: TelVerifyPageViewModel = diViewModel()
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

    LaunchedEffect(code, requestId, source) {
        viewModel.code = code
        viewModel.requestId = requestId
        viewModel.source = source
        viewModel.getTmpUserInfo()
    }

    Box(
        modifier = Modifier.fillMaxSize()
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