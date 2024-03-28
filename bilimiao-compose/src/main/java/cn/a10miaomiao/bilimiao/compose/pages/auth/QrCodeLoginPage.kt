package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.navigation.tryPopBackStack
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.king.zxing.util.CodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class QrCodeLoginPage : ComposePage() {
    override val route: String
        get() = "auth/qr_login"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: QrCodeLoginPageViewModel = diViewModel()
        QrCodeLoginPageContent(viewModel)
    }

}

internal class QrCodeLoginPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()

    private val loginSessionId = ApiHelper.getUUID()
    private var _authCode = ""

    val loading = MutableStateFlow(false)
    val qrImage = MutableStateFlow<ImageBitmap?>(null)
    val error = MutableStateFlow("")
    val isScaned = MutableStateFlow(false)

    fun loadQrImage() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            error.value = ""
            isScaned.value = false
            val res = BiliApiService.authApi
                .qrCode(loginSessionId)
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
        ImageBitmap
        qrImage.value = CodeUtils.createQRCode(
            url, 600
        ).asImageBitmap()
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
            fragment.findNavController().tryPopBackStack()
        } else {
            throw Exception(res.message)
        }
    }
}


@Composable
internal fun QrCodeLoginPageContent(
    viewModel: QrCodeLoginPageViewModel
) {
    PageConfig(
        title = "二微码登录"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    val scrollState = rememberScrollState()

    val loading = viewModel.loading.collectAsState().value
    val error = viewModel.error.collectAsState().value
    val qrImage = viewModel.qrImage.collectAsState().value
    val isScaned = viewModel.isScaned.collectAsState().value
//    val isFullScreenQrcode by viewModel.isScaned.collectAsState()

    var isFullScreenQrcode by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(viewModel) {
        viewModel.loadQrImage()
    }

    if (
        isFullScreenQrcode
        && !isScaned
        && qrImage != null
    ) {
        Image(
            bitmap = qrImage,
            contentDescription = "",
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp)
                .clickable {
                    isFullScreenQrcode = false
                }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                color = MaterialTheme.colorScheme.onBackground,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable {
                        isFullScreenQrcode = true
                    },
            ) {
                if (qrImage != null) {
                    Image(
                        bitmap = qrImage,
                        contentDescription = "",
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .background(Color.White)
                            .padding(5.dp)
                    )
                    if (isScaned) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                        ) {
                            Text(
                                text = "扫描成功\n\n请在扫码端确认登录",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
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
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Button(
                        onClick = {
                            viewModel.loadQrImage()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                    ) {
                        Text(
                            text = "重新加载"
                        )
                    }
                }
            }
        }
    }

}