package cn.a10miaomiao.bilimiao.compose.pages

import android.webkit.CookieManager
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance
import kotlinx.serialization.Serializable

@Serializable
class TestPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: TestPageViewModel = diViewModel()
        TestPageContent(viewModel)
    }

}

private class TestPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val userStore by instance<UserStore>()

    val userName: String
        get() {
            val info = userStore.state.info
            return if (info != null) {
                info.name + "(${info.mid})"
            } else {
                "未登录"
            }
        }

    val cookie: String by lazy {
        val cookieManager = CookieManager.getInstance()
        cookieManager.getCookie("https://passport.bilibili.com/")
    }

}

@Composable
private fun ColorBox(
    name: String,
    color: Color,
) {
    Row() {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color)
        )
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TestPageContent(
    viewModel: TestPageViewModel,
) {
    val windowStore: WindowStore by rememberDI { instance() }
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.windowInsets

    val scrollState = rememberScrollState()

    PageConfig(title = "测试页面")
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(windowInsets.toPaddingValues())
    ) {
        Text(
            text = "当前主题配色参考：",
            Modifier.padding(10.dp)
        )
        ColorBox("primary", MaterialTheme.colorScheme.primary)
        ColorBox("onPrimary", MaterialTheme.colorScheme.onPrimary)
        ColorBox("primaryContainer", MaterialTheme.colorScheme.primaryContainer)
        ColorBox("onPrimaryContainer", MaterialTheme.colorScheme.onPrimaryContainer)
        ColorBox("inversePrimary", MaterialTheme.colorScheme.inversePrimary)

        ColorBox("secondary", MaterialTheme.colorScheme.secondary)
        ColorBox("onSecondary", MaterialTheme.colorScheme.onSecondary)
        ColorBox("secondaryContainer", MaterialTheme.colorScheme.secondaryContainer)
        ColorBox("onSecondaryContainer", MaterialTheme.colorScheme.onSecondaryContainer)

        ColorBox("tertiary", MaterialTheme.colorScheme.tertiary)
        ColorBox("onTertiary", MaterialTheme.colorScheme.onTertiary)
        ColorBox("tertiaryContainer", MaterialTheme.colorScheme.tertiaryContainer)
        ColorBox("onTertiaryContainer", MaterialTheme.colorScheme.onTertiaryContainer)


        ColorBox("background", MaterialTheme.colorScheme.background)
        ColorBox("onBackground", MaterialTheme.colorScheme.onBackground)
        ColorBox("surface", MaterialTheme.colorScheme.surface)
        ColorBox("onSurface", MaterialTheme.colorScheme.onSurface)

        ColorBox("outline", MaterialTheme.colorScheme.outline)
        ColorBox("outlineVariant", MaterialTheme.colorScheme.outlineVariant)

        val showLonginInfo = remember {
            mutableStateOf(false)
        }
        Text(
            text = "当前登录信息(重要信息，请勿泄露给他人)：",
            Modifier.padding(10.dp)
        )
        Button(onClick = {
            showLonginInfo.value = !showLonginInfo.value
        }) {
            if (showLonginInfo.value) {
                Text(text = "隐藏")
            } else {
                Text(text = "显示")
            }
        }
        if (showLonginInfo.value) {
            Row {
                Text(
                    text = "登录用户：",
                    Modifier.width(120.dp)
                )
                Text(
                    text = remember {
                        viewModel.userName
                    }
                )
            }
            Row {
                Text(
                    text = "Buvid：",
                    Modifier.width(120.dp)
                )
                Text(
                    text = remember {
                        BilimiaoCommApp.commApp.getBilibiliBuvid()
                    }
                )
            }
            Row {
                Text(
                    text = "LoginInfo：",
                    Modifier.width(120.dp)
                )
                Text(
                    text = remember {
                        BilimiaoCommApp.commApp.loginInfo?.toString() ?: ""
                    }
                )
            }

            Row {
                Text(
                    text = "Cookie：",
                    Modifier.width(120.dp)
                )
                Text(
                    text = viewModel.cookie
                )
            }
        }
        Spacer(
            modifier = Modifier.height(
                windowStore.bottomAppBarHeightDp.dp +
                        windowInsets.bottomDp.dp
            )
        )

    }
}