package cn.a10miaomiao.bilimiao.compose.pages

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.TextButtonContentPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.compose.rememberNavController
import bilibili.broadcast.v1.Mod
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchBangumiInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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