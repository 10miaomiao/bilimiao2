package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.*
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


class TelVerifyPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    var code = ""
    var requestId = ""
    var source = ""
    var geeChallenge = ""
    var recaptchaToken = ""

    private var captchaKey = ""

    internal val jsBridge = JsBridge(this)

    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()

    val hideTel = MutableStateFlow("")
    val geetestValidatorUrl = MutableStateFlow("")
    val countdown = MutableStateFlow(0)
    val verifyCode = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    fun setVerifyCode(value: String) {
        verifyCode.value = value
    }

    fun startCountdown(second: Int) {
        countdown.value = second
        flow<Int>{
            for (i in second downTo 0){
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Default)
            .onCompletion { countdown.value = 0 }
            .onEach { countdown.value = it }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    fun getUserInfo() = viewModelScope.launch(Dispatchers.Main){
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi.userInfo(tmpCode = code)
                    .awaitCall()
                    .gson<ResultInfo<TmpUserInfo>>()
            }
            if (res.isSuccess) {
                hideTel.value = res.data.account_info.hide_tel
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

    fun getCaptchaPre() = viewModelScope.launch(Dispatchers.Main){
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi.captchaPre()
                    .awaitCall()
                    .gson<ResultInfo<CaptchaPreInfo>>()
            }
            if (res.isSuccess) {
                val geeGt = res.data.gee_gt
                geeChallenge = res.data.gee_challenge
                recaptchaToken = res.data.recaptcha_token
                geetestValidatorUrl.value = "file:///android_asset/geetest-validator/index.html?gt=$geeGt&challenge=${geeChallenge}"
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

    fun sendSms(
        validate: String,
        seccode: String
    ) = viewModelScope.launch(Dispatchers.Main){
        try {
            geetestValidatorUrl.value = ""
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi.smsSend(
                    tmpCode = code,
                    geeChallenge = geeChallenge,
                    geeSeccode = seccode,
                    geeValidate = validate,
                    recaptchaToken = recaptchaToken,
                ).awaitCall().gson<ResultInfo<SmsSendInfo>>()
            }
            if (res.isSuccess) {
                startCountdown(60)
                captchaKey = res.data.captcha_key
                Toast.makeText(fragment.requireActivity(), "已发送短信验证码", Toast.LENGTH_SHORT)
                    .show()
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

    fun verifyTel() = viewModelScope.launch(Dispatchers.Main){
        try {
            if (verifyCode.value.isBlank()) {
                alert("请输入验证码")
                return@launch
            }
            loading.value = true
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi.telVerify(
                    code = verifyCode.value,
                    tmpCode = code,
                    requestId = requestId,
                    source = source,
                    captcha_key = captchaKey,
                ).awaitCall().gson<ResultInfo<VerifyTelInfo>>()
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
    private inline fun alert(title: String, initBuilder: (AlertDialog.Builder.() -> Unit)) {
        contract {
            callsInPlace(initBuilder, InvocationKind.EXACTLY_ONCE)
        }
        AlertDialog.Builder(fragment.requireActivity()).apply {
            setTitle(title)
            setNegativeButton("关闭", null)
            initBuilder()
        }.show()
    }

}

internal class JsBridge(val viewModel: TelVerifyPageViewModel) {
    @JavascriptInterface
    fun postMessage (validate: String, seccode: String) {
        viewModel.sendSms(validate, seccode)
    }
    @JavascriptInterface
    fun close() {
        viewModel.geetestValidatorUrl.value = ""
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

    val hideTel by viewModel.hideTel.collectAsState()
    val geetestValidatorUrl by viewModel.geetestValidatorUrl.collectAsState()
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
        viewModel.getUserInfo()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = {
                WebView(it).apply {
                    settings.apply {
                        javaScriptEnabled = true
                    }
                    addJavascriptInterface(viewModel.jsBridge, "JsBridge")
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if(it.tag != geetestValidatorUrl) {
                if (geetestValidatorUrl.isBlank()) {
                    it.visibility = View.GONE
                } else {
                    it.loadUrl(geetestValidatorUrl)
                    it.visibility = View.VISIBLE
                }
            }
            it.tag = geetestValidatorUrl
        }

        if (geetestValidatorUrl.isBlank()) {
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
                Text(
                    text = "手机号：$hideTel",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .fillMaxWidth()
                )
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
                    TextField(
                        label = {
                            Text(text = "短信验证码")
                        },
                        value = verifyCode,
                        onValueChange = viewModel::setVerifyCode,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Box(modifier = Modifier.padding(end = 5.dp)) {
                                Button(
                                    onClick = viewModel::getCaptchaPre,
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
                Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp))
            }
        }
    }
}