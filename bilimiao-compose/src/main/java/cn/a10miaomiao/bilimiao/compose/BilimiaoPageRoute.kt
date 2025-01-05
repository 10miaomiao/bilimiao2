package cn.a10miaomiao.bilimiao.compose

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.serialization.decodeArguments
import cn.a10miaomiao.bilimiao.compose.animation.materialFadeThroughIn
import cn.a10miaomiao.bilimiao.compose.animation.materialFadeThroughOut
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.H5LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.QrCodeLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.SMSLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPage
import cn.a10miaomiao.bilimiao.compose.pages.filter.FilterSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
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
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.AddProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.EditProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeRegionDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.SearchFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserLikeArchivePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserMedialistPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSeasonDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlinx.serialization.serializer

class BilimiaoPageRoute (
    val builder: NavGraphBuilder
) {

    enum class Entry {
        Default,
        Login,
        DownloadList,
        PlayList,
        DownloadBangumiCreate,
        Message,
        MyFollow,
        UserSpace,
        UserSeasonDetail,
        UserFavourite,
        BangumiFollow,
        BangumiDetail,
        Setting,
        TimeSetting,
        DanmakuDisplaySetting,
        VideoSetting,
        SelectProxyServer,
        SendDanmaku,
        Lyric,
        Search,
    }

    companion object {
        fun getEntryRoute(
            entry: Entry,
            param: String,
        ): ComposePage {
            return when(entry) {
                Entry.Default -> HomePage
                Entry.Login -> LoginPage()
                Entry.DownloadList -> DownloadListPage()
                Entry.PlayList -> PlayListPage()
                Entry.DownloadBangumiCreate -> {
                    DownloadBangumiCreatePage(param)
                }
                Entry.Message -> MessagePage()
                Entry.MyFollow -> MyFollowPage()
                Entry.UserSpace -> {
                    UserSpacePage(param)
                }
                Entry.UserSeasonDetail -> {
                    val paramMap = UrlUtil.getQueryKeyValueMap(
                        Uri.parse("bilimiao://user/season?$param")
                    )
                    UserSeasonDetailPage(
                        id = paramMap["id"]!!,
                        title = paramMap["title"]!!,
                    )
                }
                Entry.UserFavourite -> {
                    UserFavouritePage(
                        mid = param
                    )
                }
                Entry.BangumiFollow -> BangumiFollowPage()
                Entry.BangumiDetail -> {
                    val paramMap = UrlUtil.getQueryKeyValueMap(
                        Uri.parse("bilimiao://bangumi?$param")
                    )
                    BangumiDetailPage(
                        id = paramMap["id"] ?: "",
                        epId = paramMap["epid"] ?: "",
                        mediaId = paramMap["mediaid"] ?: ""
                    )
                }
                Entry.Setting -> SettingPage()
                Entry.TimeSetting -> TimeSettingPage()
                Entry.DanmakuDisplaySetting -> {
                    DanmakuDisplaySettingPage(param)
                }
                Entry.VideoSetting -> VideoSettingPage()
                Entry.SelectProxyServer -> SelectProxyServerPage()
                Entry.SendDanmaku -> SendDanmakuPage()
                Entry.Lyric -> LyricPage()
                Entry.Search -> SearchResultPage(param)
            }
        }
    }

    fun initRoute() {
        composable<BlankPage>()
        composable<TestPage>()

        // home
        composable<HomePage>()
//        builder.composable<HomePage> {
//            val page = it.toRoute<HomePage>()
//            page.Content()
//        }
        
        // search
        composable<SearchResultPage>()

        // auth
        composable<LoginPage>()
        composable<QrCodeLoginPage>()
        composable<TelVerifyPage>()
        composable<H5LoginPage>()
        composable<SMSLoginPage>()

        // bangumi
        composable<BangumiFollowPage>()
        composable<BangumiDetailPage>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://www.bilibili.com/bangumi/play/ss{id}/"
                }
            )
        )

        // dynamic
        composable<DynamicPage>()
        composable<DynamicDetailPage>(
            deepLinks = listOf(
                navDeepLink<DynamicDetailPage>(
                    basePath = "bilibili://opus/detail"
                )
            )
        )

        // rank
        composable<RankPage>(
            deepLinks = listOf(
                navDeepLink<RankPage>(
                    basePath = "bilibili://rank"
                )
            )
        )

        // download
        composable<DownloadListPage>()
        composable<DownloadDetailPage>()
        composable<DownloadBangumiCreatePage>()

        // filter
        composable<FilterSettingPage>()

        // message
        composable<MessagePage>()

        // player
        composable<SendDanmakuPage>()

        // playlist
        composable<PlayListPage>()

        // setting
        composable<SettingPage>()
        composable<HomeSettingPage>()
        composable<VideoSettingPage>()
        composable<DanmakuSettingPage>()
        composable<DanmakuDisplaySettingPage>()
        composable<FlagsSettingPage>()
        composable<ProxySettingPage>()
        composable<AddProxyServerPage>()
        composable<EditProxyServerPage>()
        composable<SelectProxyServerPage>()
        composable<AboutPage>()

        // time
        composable<TimeSettingPage>()
        composable<TimeRegionDetailPage>()

        // user
        composable<UserSpacePage>(
            deepLinks = listOf(
                navDeepLink<UserSpacePage>(
                    basePath = "bilibili://author"
                )
            )
        )
        composable<MyFollowPage>()
        composable<SearchFollowPage>()
        composable<UserBangumiPage>()
        composable<SearchFollowPage>()
        composable<UserLikeArchivePage>()
        composable<UserFavouritePage>()
        composable<UserFavouriteDetailPage>()
        composable<UserSeasonDetailPage>()
        composable<UserMedialistPage>()

        //lyric
        composable<LyricPage>()

        // web
        composable<WebPage>()
    }

    fun defaultEnterTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>
    ): @JvmSuppressWildcards EnterTransition? {
        return materialFadeThroughIn(initialScale = 0.85f)
    }

    fun defaultExitTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>
    ): @JvmSuppressWildcards ExitTransition? {
        return materialFadeThroughOut()
    }

    fun defaultPopEnterTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>
    ): @JvmSuppressWildcards EnterTransition? {
        return materialFadeThroughIn(initialScale = 1.15f)
    }

    fun defaultPopExitTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>
    ): @JvmSuppressWildcards ExitTransition? {
        return materialFadeThroughOut()
    }

    @SuppressLint("RestrictedApi")
    inline fun <reified T: ComposePage> composable(
        deepLinks: List<NavDeepLink> = emptyList(),
        noinline enterTransition:
        (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
            EnterTransition?)? = ::defaultEnterTransition,
        noinline exitTransition:
        (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
            ExitTransition?)? = ::defaultExitTransition,
        noinline popEnterTransition:
        (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
            EnterTransition?)? = ::defaultPopEnterTransition,
        noinline popExitTransition:
        (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
            ExitTransition?)? = ::defaultPopExitTransition,
        noinline sizeTransform:
        (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
            SizeTransform?)? = null,
    ) {
        val serializer = serializer<T>()
        builder.composable<T>(
            deepLinks = deepLinks,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
            sizeTransform = sizeTransform,
        ) { backStackEntry ->
            val bundle = backStackEntry.arguments ?: Bundle()
            val typeMap = backStackEntry.destination.arguments.mapValues { it.value.type }
            val page = serializer.decodeArguments(bundle, typeMap)
            page.Content()
        }
    }
}