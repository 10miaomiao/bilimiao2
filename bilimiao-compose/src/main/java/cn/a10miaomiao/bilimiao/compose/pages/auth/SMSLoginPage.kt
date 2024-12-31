package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.pages.auth.components.InternationalDialingPrefixSelect
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.SmsSendInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.WebKeyInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
@Serializable
class SMSLoginPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SMSLoginPageViewModel = diViewModel()
        SMSLoginPageCompose(viewModel)
    }

}

private class SMSLoginPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware, BiliGeetestUtil.GTCallBack {

    private var recaptchaUrl = ""
    private var recaptchaToken = ""

    private var captchaKey = ""

    private val pageNavigation by instance<PageNavigation>()
    private val userStore by instance<UserStore>()
    private val messageDialog by instance<MessageDialogState>()

    private val biliGeetestUtil by instance<BiliGeetestUtil>()

    val selectedCid = MutableStateFlow("86") // 国际区号
    val telNumber = MutableStateFlow("")
    val countdown = MutableStateFlow(0)
    val verifyCode = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    fun setSelectedCid(value: String) {
        selectedCid.value = value
    }

    fun setVerifyCode(value: String) {
        verifyCode.value = value
    }

    fun setTelNumber(value: String) {
        telNumber.value = value
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


    fun smsLogin() = viewModelScope.launch(Dispatchers.Main) {
        try {
            val tel = telNumber.value
            val code = verifyCode.value
            if (captchaKey.isBlank()) {
                messageDialog.alert("请先获取验证码")
                return@launch
            }
            if (tel.isBlank()) {
                messageDialog.alert("请输入手机号")
                return@launch
            }
            if (code.isBlank()) {
                messageDialog.alert("请输入验证码")
                return@launch
            }
            loading.value = true
            val webKeyRes = withContext(Dispatchers.IO) {
                BiliApiService.authApi
                    .webKey()
                    .awaitCall()
                    .json<ResponseData<WebKeyInfo>>(isLog = true)
            }
            if (!webKeyRes.isSuccess) {
                messageDialog.alert(webKeyRes.message)
                return@launch
            }
            val key = webKeyRes.requireData().key
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi
                    .smsLogin(
                        cid = selectedCid.value,
                        tel = tel,
                        code = code,
                        captchaKey = captchaKey,
                        key = key,
                    )
                    .awaitCall()
                    .json<ResponseData<LoginInfo>>()
            }
            if (res.isSuccess) {
                val loginInfo = res.requireData()
                BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                authInfo()
            } else {
                messageDialog.alert(res.message)
            }
            loading.value = false
        } catch (e: Exception) {
            PopTip.show("网络错误：$e")
            e.printStackTrace()
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

    fun sendClick() {
        if (telNumber.value.isBlank()) {
            messageDialog.alert("请输入手机号")
            return
        }
        viewModelScope.launch {
            sendSms()
        }
    }

    /**
     * 发送短信验证码
     */
    private suspend fun sendSms(
        gt3Result: BiliGeetestUtil.GT3ResultBean? = null
    ): Boolean {
        val cid = selectedCid.value
        val tel = telNumber.value
        val res = withContext(Dispatchers.IO) {
            if (gt3Result == null) {
                BiliApiService.authApi.smsLoginSend(
                    cid = cid,
                    tel = tel,
                ).awaitCall().json<ResponseData<SmsSendInfo>>()
            } else {
                BiliApiService.authApi.smsLoginSend(
                    cid = cid,
                    tel = tel,
                    geeChallenge = gt3Result.geetest_challenge,
                    geeSeccode = gt3Result.geetest_seccode,
                    geeValidate = gt3Result.geetest_validate,
                    recaptchaToken = recaptchaToken,
                ).awaitCall().json<ResponseData<SmsSendInfo>>()
            }
        }
        if (res.isSuccess) {
            val resData = res.data!!
            if (!resData.recaptcha_url.isNullOrBlank()) {
                recaptchaUrl = resData.recaptcha_url!!
                biliGeetestUtil.startCustomFlow(this)
                return false
            }
            captchaKey = res.requireData().captcha_key!!
            startCountdown(60)
            PopTip.show("已发送短信验证码")
            return true
        } else if (res.code == -105 && gt3Result == null) {
            recaptchaUrl = res.requireData().recaptcha_url!!
            biliGeetestUtil.startCustomFlow(this)
            return false
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
        return sendSms(result)
    }

    override suspend fun getGTApiJson(): JSONObject? {
        val queryMap = UrlUtil.getQueryKeyValueMap(Uri.parse(recaptchaUrl))
        if (queryMap.containsKey("recaptcha_token")) {
            recaptchaToken = queryMap["recaptcha_token"] ?: ""
            return JSONObject().apply {
                put("success", 1)
                put("challenge", queryMap["gee_challenge"] ?: "")
                put("gt", queryMap["gee_gt"] ?: "")
            }
        } else {
            messageDialog.alert("加载验证码出现错误")
            return null
        }
    }

}

@Composable
private fun SMSLoginPageCompose(
    viewModel: SMSLoginPageViewModel
) {
    PageConfig(title = "手机号登录")

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val selectedCid by viewModel.selectedCid.collectAsState()
    val telNumber by viewModel.telNumber.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val verifyCode by viewModel.verifyCode.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val verifyCodeFocusRequester = remember { FocusRequester() }

    val telNumberKeyboardActions = remember(verifyCodeFocusRequester) {
        KeyboardActions(
            onNext = {
                verifyCodeFocusRequester.requestFocus()
            }
        )
    }
    val verifyCodeKeyboardActions = remember(viewModel) {
        KeyboardActions {
            viewModel.smsLogin()
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
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "手机号登录",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            TextField(
                label = {
                    Text(text = "手机号")
                },
                value = telNumber,
                onValueChange = viewModel::setTelNumber,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    InternationalDialingPrefixSelect(
                        value = selectedCid,
                        onChange = viewModel::setSelectedCid
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = telNumberKeyboardActions,
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                label = {
                    Text(text = "验证码")
                },
                value = verifyCode,
                onValueChange = viewModel::setVerifyCode,
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(verifyCodeFocusRequester),
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
                onClick = viewModel::smsLogin,
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
                    text = "登录"
                )
            }
            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))
        }
    }

}