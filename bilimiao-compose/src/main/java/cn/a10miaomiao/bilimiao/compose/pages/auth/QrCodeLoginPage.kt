package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.WindowStore
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.skydoves.landscapist.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class QrCodeLoginPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()

    private var _authCode = ""

    val loading = MutableStateFlow(false)
    val qrImage = MutableStateFlow<Drawable?>(null)
    var error = MutableStateFlow("")
    var isScaned = MutableStateFlow(false)

    fun loadQrImage() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            error.value = ""
            isScaned.value = false
            val res = BiliApiService.authApi
                .qrCode()
                .awaitCall()
                .gson<ResultInfo<QRLoginInfo>>()
            if (res.isSuccess) {
                renderQrcode(res.data.url)
                launch {
                    val authCode = res.data.auth_code
                    _authCode = authCode
                    checkQRCode(authCode)
                }
            } else {
                error.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error.value = e.message ?: e.toString()
        } finally {
            loading.value = false
        }
    }

    fun renderQrcode(
        url: String,
    ) {
        qrImage.value = QrCodeDrawable(
            data = QrData.Url(url)
        )
    }


    private suspend fun checkQRCode(
        authCode: String,
    ) {
        try {
            val res = BiliApiService.authApi
                .checkQrCode(authCode)
                .awaitCall()
                .gson<ResultInfo<LoginInfo.QrLoginInfo>>()
            when(res.code) {
                86039 -> {
                    // 未确认
                    delay(3000)
                    if (_authCode == authCode) {
                        checkQRCode(authCode)
                    }
                }
                86090 -> {
                    // 已扫码未确认
                    isScaned.value = true
                    delay(2000)
                    if (_authCode == authCode) {
                        checkQRCode(authCode)
                    }
                }
                86038, -3 -> {
                    // 过期、失效
                    error.value = "二维码已过期，请刷新"
                }
                0 -> {
                    // 成功
                    val loginInfo = res.data.toLoginInfo()
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                    authInfo()
                }
                else -> {
                    // 发生错误
                    error.value = "登录失败，请稍后重试\n" + res.message
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error.value = e.message ?: e.toString()
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
}


@Composable
fun QrCodeLoginPage() {
    PageConfig(
        title = "二微码登录"
    )
    val viewModel: QrCodeLoginPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val scrollState = rememberScrollState()

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val qrImage by viewModel.qrImage.collectAsState()
    val isScaned by viewModel.isScaned.collectAsState()


    LaunchedEffect(viewModel) {
        viewModel.loadQrImage()
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
        Text(
            text = "请使用哔哩哔哩客户端扫码登录",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
            if (qrImage != null) {
                Image(
                    painter = rememberDrawablePainter(drawable = qrImage),
                    contentDescription = "",
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                )
                if (isScaned) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "扫描成功\n\n请在扫码端确认登录",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    strokeWidth = 3.dp,
                )
            }
            if (error.isNotBlank()) {
                Text(
                    text = error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        viewModel.loadQrImage()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                ) {
                    Text(text = "重新加载")
                }
            }
        }


    }
}