package cn.a10miaomiao.bilimiao.compose

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.QrCodeLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.pages.filter.FilterSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.AboutPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.FlagsSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.HomeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.AddProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.EditProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.SearchFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserLikeArchivePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserMedialistPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSeasonDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage

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
        +BangumiFollowPage()
        +BangumiDetailPage()

        // dynamic
        +DynamicPage()
        +DynamicDetailPage()

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
        +SettingPage()
        +HomeSettingPage()
        +VideoSettingPage()
        +DanmakuSettingPage()
        +DanmakuDisplaySettingPage()
        +FlagsSettingPage()
        +ProxySettingPage()
        +AddProxyServerPage()
        +EditProxyServerPage()
        +SelectProxyServerPage()
        +AboutPage()

        // time
        +TimeSettingPage()

        // user
        +UserSpacePage()
        +MyFollowPage()
        +UserFollowPage()
        +UserBangumiPage()
        +SearchFollowPage()
        +UserLikeArchivePage()
        +UserFavouritePage()
        +UserFavouriteDetailPage()
        +UserSeasonDetailPage()
        +UserMedialistPage()

        //lyric
        +LyricPage()
    }

    private operator fun ComposePage.unaryPlus() {
        builder.composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            enterTransition = ::enterTransition,
            exitTransition = ::exitTransition,
            popEnterTransition = ::popEnterTransition,
            popExitTransition = ::popExitTransition,
        ) {
            Content(it)
        }
    }

}