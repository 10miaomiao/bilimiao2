package cn.a10miaomiao.bilimiao.compose

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.get
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.QrCodeLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.filter.FilterSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.AddProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.EditProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class PageRouteBuilder (
    val builder: NavGraphBuilder
) {

    fun initRoute() {
        +BlankPage()
        +TestPage()

        // auth
        +LoginPage()
        +QrCodeLoginPage()
        +TelVerifyPage()

        // bangumi
        +BangumiDetailPage()

        // download
        +DownloadListPage()
        +DownloadDetailPage()
        +DownloadBangumiCreatePage()

        // filter
        +FilterSettingPage()

        // message
        +MessagePage()

        // player
        +SendDanmakuPage()

        // playlist
        +PlayListPage()

        // setting
        +ProxySettingPage()
        +AddProxyServerPage()
        +EditProxyServerPage()
        +SelectProxyServerPage()

        // time
        +TimeSettingPage()

        // user
        +UserFollowPage()
    }

    private operator fun ComposePage.unaryPlus() {
        builder.composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
        ) {
            Content(it)
        }
    }

}