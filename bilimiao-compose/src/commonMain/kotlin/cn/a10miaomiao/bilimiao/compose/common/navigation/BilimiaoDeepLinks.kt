package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyDetailListPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicOpusPage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.HistoryPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.WatchLaterPage
import cn.a10miaomiao.bilimiao.compose.pages.rank.RankPage
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.H5LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import kotlinx.serialization.serializer

/**
 * Nav3 声明式深链接注册表。
 *
 * 用 [UriDeepLinkMatcher] 注册简单 pattern（路径参数 + query 参数），
 * 复杂规则（BV/av/ss/ep/md 号正则提取）由 [BilibiliNavigation.resolveUri] fallback 处理。
 *
 * pattern 语法：`scheme://host/path/{arg}` 或 `scheme://host/path?query={arg}`
 */
object BilimiaoDeepLinks {

    /**
     * 所有声明式深链接 matcher。顺序不重要，首个匹配生效。
     */
    val matchers: List<DeepLinkMatcher<out NavKey>> = listOf(
        // 视频
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://video/{id}"),
            serializer = serializer<VideoDetailPage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://video/{id}"),
            serializer = serializer<VideoDetailPage>(),
        ),
        // 用户
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://user/{id}"),
            serializer = serializer<UserSpacePage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://author/{id}"),
            serializer = serializer<UserSpacePage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://space/{id}"),
            serializer = serializer<UserSpacePage>(),
        ),
        // 番剧（path + query 参数组合）
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://bangumi/{id}?epId={epId}&mediaId={mediaId}"),
            serializer = serializer<BangumiDetailPage>(),
        ),
        // 动态
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://following/detail/{id}"),
            serializer = serializer<DynamicDetailPage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://opus/detail/{id}"),
            serializer = serializer<DynamicOpusPage>(),
        ),
        // 排行榜
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://rank"),
            serializer = serializer<RankPage>(),
        ),
        // 下载
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://download"),
            serializer = serializer<DownloadListPage>(),
        ),
        // 设置
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://setting"),
            serializer = serializer<SettingPage>(),
        ),
        // 我的
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://mine/bangumi"),
            serializer = serializer<MyBangumiPage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://mine/follow"),
            serializer = serializer<MyFollowPage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://mine/history"),
            serializer = serializer<HistoryPage>(),
        ),
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://mine/watchlater"),
            serializer = serializer<WatchLaterPage>(),
        ),
        // 收藏
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://user/favourite"),
            serializer = serializer<UserFavouritePage>(),
        ),
        // 评论
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://comment/{id}"),
            serializer = serializer<ReplyDetailListPage>(),
        ),
        // 歌词
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://lyric"),
            serializer = serializer<LyricPage>(),
        ),
        // 搜索（query 参数）
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilibili://search?keyword={keyword}"),
            serializer = serializer<SearchResultPage>(),
        ),
        // H5 登录
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://h5login"),
            serializer = serializer<H5LoginPage>(),
        ),
        // 手机验证
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://tel-verify?code={code}&requestId={requestId}&source={source}"),
            serializer = serializer<TelVerifyPage>(),
        ),
        // Web
        UriDeepLinkMatcher(
            uriPattern = DeepLinkUri("bilimiao://web?url={url}"),
            serializer = serializer<WebPage>(),
        ),
    )

    /**
     * 用声明式 matcher 解析 URI，返回匹配的 [NavKey] 或 null。
     * 复杂规则（BV/av/ss/ep/md 号）不在此处理，由 [BilibiliNavigation.resolveUri] fallback。
     */
    fun match(url: String): NavKey? {
        val request = DeepLinkRequest.fromUriString(url)
        return matchers.firstNotNullOfOrNull { matcher ->
            matcher.match(request)?.key
        }
    }
}
