package cn.a10miaomiao.bilimiao.compose.pages

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
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance


class TestPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val userStore by instance<UserStore>()

}


@Composable
fun ColorBox(
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
fun TestPage() {
    val viewModel: TestPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberDI { instance() }
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.windowInsets

    val scrollState = rememberScrollState()

    PageConfig(title = "测试页面")
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        Text(text = "hello world")
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
    }
}