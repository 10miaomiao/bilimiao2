package cn.a10miaomiao.bilimiao.compose.comm.mypage

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage

class PageConfigInfo(
    val page: MyPage
) {
    var title: String = ""
    var menus: List<MenuItemPropInfo>? = null
    var onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
}

internal val LocalPageConfigInfo: ProvidableCompositionLocal<PageConfigInfo?> =
    compositionLocalOf { null }

@Composable
fun PageConfig(
    title: String = "",
    menus: List<MenuItemPropInfo>? = null,
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(title, menus) {
        pageConfigInfo?.let {
            it.title = title
            it.menus = menus
            it.page.pageConfig.notifyConfigChanged()
        }
    }
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            pageConfigInfo?.let {
                it.title = ""
                it.menus = null
                it.page.pageConfig.notifyConfigChanged()
            }
        }
    }
}

@Composable
fun PageMenuItemClick(
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(onMenuItemClick) {
        pageConfigInfo?.onMenuItemClick = onMenuItemClick
    }
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            pageConfigInfo?.onMenuItemClick = null
        }
    }
}
@Composable
fun PageMenuItemClick(
    key1: Any?,
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(key1, onMenuItemClick) {
        pageConfigInfo?.onMenuItemClick = onMenuItemClick
    }
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            pageConfigInfo?.onMenuItemClick = null
        }
    }
}
@Composable
fun PageMenuItemClick(
    key1: Any?,
    key2: Any?,
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(key1, key2, onMenuItemClick) {
        pageConfigInfo?.onMenuItemClick = onMenuItemClick
    }
}
@Composable
fun PageMenuItemClick(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(key1, key2, onMenuItemClick) {
        pageConfigInfo?.onMenuItemClick = onMenuItemClick
    }
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            pageConfigInfo?.onMenuItemClick = null
        }
    }
}
@Composable
fun PageMenuItemClick(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    key4: Any?,
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(key1, key2, key3, key4, onMenuItemClick) {
        pageConfigInfo?.onMenuItemClick = onMenuItemClick
    }
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            pageConfigInfo?.onMenuItemClick = null
        }
    }
}