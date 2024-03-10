package com.a10miaomiao.bilimiao.compose.ui.self

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.Log
import com.a10miaomiao.bilimiao.compose.state.UserState
import com.a10miaomiao.bilimiao.compose.ui.destinations.SelfScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.dsl.QrOptionsBuilderScope
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.delay


@Composable
@Destination
fun QrCodeLoginScreen(navigator: DestinationsNavigator) {
    var needFetch by remember { mutableStateOf(true) }
    var qrCodeContent by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }


    LaunchedEffect(needFetch){
        if (!needFetch) return@LaunchedEffect
        if (qrCodeContent.isNotEmpty()) return@LaunchedEffect
        val res = BiliApiService.authApi
            .qrCode()
            .awaitCall()
            .gson<ResultInfo<QRLoginInfo>>()

        if (res.isSuccess) {
            qrCodeContent = res.data.url
            authCode = res.data.auth_code
            needFetch = false
        } else {
            // do something
        }
    }
    LaunchedEffect(authCode){
        if (authCode.isEmpty()) return@LaunchedEffect
        while (true){
            val res = BiliApiService.authApi.checkQrCode(authCode).awaitCall().gson<ResultInfo<LoginInfo.QrLoginInfo>>()
            when(res.code) {
                86039 -> {
                    // 未确认
                    delay(3000)
                }
                86090 -> {
                    // 已扫码未确认
                    delay(2000)
                }
                86038, -3 -> {
                    // 过期、失效
                    needFetch = true
                    break
                    // alert user with info "二维码已过期，请刷新"
                }
                0 -> {
                    // 成功
                    val loginInfo = res.data.toLoginInfo()
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                    Log.debug { loginInfo.toString() }
                    var userState by UserState
                    userState = loginInfo
                    navigator.navigate(SelfScreenDestination)
                }
                else -> {
                    // 发生错误
                    // do something
                    // error.value = "登录失败，请稍后重试\n" + res.message
                    throw IllegalStateException("登录失败，请稍后重试")
                }
            }
        }
    }

    if (qrCodeContent.isNotEmpty()) {
        val qrcodePainter : Painter = rememberQrCodePainter(qrCodeContent, qrCodeContent){ customize() }
        Box (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(qrcodePainter,"",modifier = Modifier.background(Color.White))
        }
    }


}

fun QrOptionsBuilderScope.customize() {
    // TODO Maybe A bilimiao logo
//    logo {
//        painter = logoPainter
//        padding = QrLogoPadding.Natural(.1f)
//        shape = QrLogoShape.circle()
//        size = 0.2f
//    }
    shapes {
        ball = QrBallShape.circle()
        darkPixel = QrPixelShape.roundCorners()
        frame = QrFrameShape.roundCorners(.25f)
    }
    colors {
        dark = QrBrush.brush {
            Brush.linearGradient(
                0f to Color.Black,
                1f to Color.Black,
                end = Offset(it, it)
            )
        }
        frame = QrBrush.solid(Color.Black)
    }
}
