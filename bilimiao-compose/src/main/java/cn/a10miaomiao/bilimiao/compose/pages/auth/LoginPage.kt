package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.WebKeyInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
class LoginPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: LoginPageViewModel = diViewModel()
        LaunchedEffect(Unit) {
            viewModel.checkLogin()
        }
        LoginPageContent(viewModel)
    }

}

private class LoginPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware, BiliGeetestUtil.GTCallBack {

    private var verifyUrl = ""
    private var recaptchaToken = ""

    private val pageNavigation by instance<PageNavigation>()
    private val userStore by instance<UserStore>()
    private val biliGeetestUtil by instance<BiliGeetestUtil>()
    private val messageDialog by instance<MessageDialogState>()

    val loading = MutableStateFlow(false)
    val userName = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun setUserName(value: String) {
        userName.value = value
    }

    fun setPassword(value: String) {
        password.value = value
    }

    fun startLogin(
        gt3Result: BiliGeetestUtil.GT3ResultBean? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (userName.value.isBlank()) {
                messageDialog.alert("请输入用户名/邮箱/手机号")
                return@launch
            }
            if (password.value.isBlank()) {
                messageDialog.alert("请输入密码")
                return@launch
            }
            loading.value = true
            val webKey = getWebKey()
            val res = if (gt3Result == null) {
                // 不带验证码
                BiliApiService.authApi.oauth2Login(
                    username = userName.value,
                    passport = password.value,
                    key = webKey.key,
                    rhash = webKey.hash
                )
            } else {
                // 带验证码
                BiliApiService.authApi.oauth2Login(
                    username = userName.value,
                    passport = password.value,
                    key = webKey.key,
                    rhash = webKey.hash,
                    recaptchaToken = recaptchaToken,
                    geeValidate = gt3Result.geetest_validate,
                    geeSeccode = gt3Result.geetest_seccode,
                    geeChallenge = gt3Result.geetest_challenge,
                )
            }.awaitCall().json<ResponseData<LoginInfo.PasswordLoginInfo>>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val loginInfo = res.requireData()
                    if (loginInfo.status == 0) {
                        BilimiaoCommApp.commApp.saveAuthInfo(loginInfo.toLoginInfo())
                        authInfo()
                    } else if (loginInfo.url != null && "tmp_token=" in loginInfo.url) {
                        messageDialog.open(
                            title = "提示",
                            text = loginInfo.message,
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val params = UrlUtil.getQueryKeyValueMap(Uri.parse(loginInfo.url))
                                        if (params.containsKey("tmp_token")
                                            && params.containsKey("request_id")
                                            && params.containsKey("source")
                                        ) {
                                            pageNavigation.navigate(TelVerifyPage(
                                                code = params["tmp_token"]!!,
                                                requestId = params["request_id"]!!,
                                                source = params["source"]!!,
                                            ))
                                        } else {
                                            pageNavigation.launchWebBrowser(loginInfo.url)
                                        }
                                        messageDialog.close()
                                    }
                                ) {
                                    Text("请往验证")
                                }
                            }
                        )
                    } else {
                        messageDialog.open(
                            title = "登录失败，请稍后重试：" + loginInfo.status,
                            text = loginInfo.message,
                            confirmButton = {
                                if (!loginInfo.url.isNullOrBlank()) {
                                    TextButton(
                                        onClick = {
                                            pageNavigation.launchWebBrowser(loginInfo.url)
                                        }
                                    ) {
                                        Text("查看")
                                    }
                                }
                            }
                        )
                    }
                } else if (res.code == -105 && gt3Result == null) {
                    verifyUrl = res.data!!.url ?: ""
                    biliGeetestUtil.startCustomFlow(this@LoginPageViewModel)
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

    private suspend fun getWebKey(): WebKeyInfo {
        val res = BiliApiService.authApi
            .webKey()
            .awaitCall()
            .json<ResponseData<WebKeyInfo>>()
        if (res.isSuccess) {
            return res.requireData()
        }
        throw Exception(res.message)
    }

    private suspend fun authInfo() {
        val res = withContext(Dispatchers.IO) {
            BiliApiService.authApi
                .account()
                .awaitCall()
                .json<ResponseData<UserInfo>>()
        }
        if (res.isSuccess) {
            withContext(Dispatchers.Main) {
                userStore.setUserInfo(res.requireData())
                pageNavigation.popBackStack()
            }
        } else {
            throw Exception(res.message)
        }
    }

    fun checkLogin() {
        if (userStore.isLogin()) {
            pageNavigation.popBackStack()
        }
    }

    override suspend fun onGTDialogResult(
        result: BiliGeetestUtil.GT3ResultBean
    ): Boolean {
        startLogin(result)
        return true
    }

    override suspend fun getGTApiJson(): JSONObject? {
        val queryMap = UrlUtil.getQueryKeyValueMap(Uri.parse(verifyUrl))
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

    fun toH5LoginPage() {
        pageNavigation.navigate(H5LoginPage())
    }

    fun toQrLogin() {
        pageNavigation.navigate(QrCodeLoginPage())
    }

    fun toSMSLogin() {
        pageNavigation.navigate(SMSLoginPage())
    }
}

@Composable
private fun LoginPageContent(
    viewModel: LoginPageViewModel
) {
    PageConfig(title = "登录BILIBILI")
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(LocalView.current)
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val loading by viewModel.loading.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val password by viewModel.password.collectAsState()

    val scrollState = rememberScrollState()
    val passwordFocusRequester = remember { FocusRequester() }
    var passwordIsFocus by remember { mutableStateOf(false) }

    val usernameKeyboardActions = remember(passwordFocusRequester) {
        KeyboardActions(
            onNext = {
                passwordFocusRequester.requestFocus()
            }
        )
    }
    val passwordKeyboardActions = remember(viewModel) {
        KeyboardActions(
            onDone = {
                viewModel.startLogin()
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(80.dp)
                .padding(
                    top = windowInsets.topDp.dp,
                    bottom = windowInsets.bottomDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (passwordIsFocus) {
                Image(
                    painter = painterResource(id = R.drawable.ic_22_hide),
                    contentDescription = "22娘遮眼",
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_33_hide),
                    contentDescription = "33娘遮眼",
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_22),
                    contentDescription = "22娘",
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_33),
                    contentDescription = "33娘",
                )
            }
        }
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
                    text = "登录Bilibili",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            TextField(
                label = {
                    Text(text = "用户名/邮箱/手机号")
                },
                value = userName,
                onValueChange = viewModel::setUserName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = usernameKeyboardActions,
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                label = {
                    Text(text = "密码")
                },
                value = password,
                onValueChange = viewModel::setPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged { passwordIsFocus = it.isFocused },
                singleLine = true,
                // 显示密码样式
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                // 显示密码样式
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = passwordKeyboardActions,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = viewModel::startLogin,
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
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = viewModel::toSMSLogin) {
                    Text(text = "手机号登录")
                }
                Spacer(modifier = Modifier.width(20.dp))
                TextButton(onClick = viewModel::toQrLogin) {
                    Text(text = "二维码登录")
                }
            }
            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))

        }
    }

}