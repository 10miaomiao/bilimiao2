package cn.a10miaomiao.bilimiao.compose.comm.mypage

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage

class PageConfigInfo(
    val page: MyPage
) {
    var title: String = ""
    var menus: List<MenuItemPropInfo>? = null
}

internal val LocalPageConfigInfo: ProvidableCompositionLocal<PageConfigInfo?> =
    compositionLocalOf { null }

@Composable
fun PageConfig(
    title: String = "",
    menus: List<MenuItemPropInfo>? = null
) {
    val pageConfigInfo = LocalPageConfigInfo.current
    LaunchedEffect(title, menus) {
        pageConfigInfo?.let {
            it.title = title
            it.menus = menus
            it.page.pageConfig.notifyConfigChanged()
        }
    }
}