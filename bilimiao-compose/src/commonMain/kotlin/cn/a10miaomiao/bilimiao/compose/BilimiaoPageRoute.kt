package cn.a10miaomiao.bilimiao.compose

import androidx.navigation3.runtime.EntryProviderScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.H5LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.QrCodeLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.SMSLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiEpisodesPage
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyListPage
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyDetailListPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicOpusPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.pages.filter.FilterSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import cn.a10miaomiao.bilimiao.compose.pages.mine.HistoryPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.WatchLaterPage
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.rank.RankPage
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.AboutPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.FlagsSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.HomeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.AutoStopTimerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.AddProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.EditProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeRegionDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.SearchFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserLikeArchivePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserMedialistPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSeasonDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceSearchPage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoPagesPage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ThemeSettingPage
import androidx.navigation3.runtime.NavKey

/**
 * 所有页面的 NavEntry 注册表。
 * 在 entryProvider { } DSL 中调用 entries(this) 注册全部页面。
 * 深链接解析在 BilibiliNavigation 中独立实现，此处不声明 navDeepLink。
 */
object BilimiaoPageRoute {

    fun entries(scope: EntryProviderScope<NavKey>) {
        // 通用
        scope.entry<BlankPage> { it.Content() }
        scope.entry<TestPage> { it.Content() }

        // home
        scope.entry<HomePage> { it.Content() }

        // search
        scope.entry<SearchResultPage> { it.Content() }

        // auth
        scope.entry<LoginPage> { it.Content() }
        scope.entry<QrCodeLoginPage> { it.Content() }
        scope.entry<TelVerifyPage> { it.Content() }
        scope.entry<H5LoginPage> { it.Content() }
        scope.entry<SMSLoginPage> { it.Content() }

        // video
        scope.entry<VideoDetailPage> { it.Content() }
        scope.entry<VideoPagesPage> { it.Content() }

        // bangumi
        scope.entry<BangumiDetailPage> { it.Content() }
        scope.entry<BangumiEpisodesPage> { it.Content() }

        // dynamic
        scope.entry<DynamicPage> { it.Content() }
        scope.entry<DynamicDetailPage> { it.Content() }
        scope.entry<DynamicOpusPage> { it.Content() }

        // rank
        scope.entry<RankPage> { it.Content() }

        // download
        scope.entry<DownloadListPage> { it.Content() }
        scope.entry<DownloadDetailPage> { it.Content() }
        scope.entry<DownloadBangumiCreatePage> { it.Content() }

        // filter
        scope.entry<FilterSettingPage> { it.Content() }

        // message
        scope.entry<MessagePage> { it.Content() }

        // player
        scope.entry<SendDanmakuPage> { it.Content() }

        // playlist
        scope.entry<PlayListPage> { it.Content() }

        // setting
        scope.entry<SettingPage> { it.Content() }
        scope.entry<HomeSettingPage> { it.Content() }
        scope.entry<ThemeSettingPage> { it.Content() }
        scope.entry<VideoSettingPage> { it.Content() }
        scope.entry<AutoStopTimerPage> { it.Content() }
        scope.entry<DanmakuSettingPage> { it.Content() }
        scope.entry<DanmakuDisplaySettingPage> { it.Content() }
        scope.entry<FlagsSettingPage> { it.Content() }
        scope.entry<ProxySettingPage> { it.Content() }
        scope.entry<AddProxyServerPage> { it.Content() }
        scope.entry<EditProxyServerPage> { it.Content() }
        scope.entry<SelectProxyServerPage> { it.Content() }
        scope.entry<AboutPage> { it.Content() }

        // time
        scope.entry<TimeSettingPage> { it.Content() }
        scope.entry<TimeRegionDetailPage> { it.Content() }

        // mine
        scope.entry<MyBangumiPage> { it.Content() }
        scope.entry<MyFollowPage> { it.Content() }
        scope.entry<HistoryPage> { it.Content() }
        scope.entry<WatchLaterPage> { it.Content() }

        // user
        scope.entry<UserSpacePage> { it.Content() }
        scope.entry<UserSpaceSearchPage> { it.Content() }
        scope.entry<UserFollowPage> { it.Content() }
        scope.entry<SearchFollowPage> { it.Content() }
        scope.entry<UserBangumiPage> { it.Content() }
        scope.entry<UserLikeArchivePage> { it.Content() }
        scope.entry<UserFavouritePage> { it.Content() }
        scope.entry<UserFavouriteDetailPage> { it.Content() }
        scope.entry<UserSeasonDetailPage> { it.Content() }
        scope.entry<UserMedialistPage> { it.Content() }

        // community
        scope.entry<MainReplyListPage> { it.Content() }
        scope.entry<ReplyDetailListPage> { it.Content() }

        // lyric
        scope.entry<LyricPage> { it.Content() }

        // web
        scope.entry<WebPage> { it.Content() }
    }
}
