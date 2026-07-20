package cn.a10miaomiao.bilimiao.compose.common.navigation

import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
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
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyDetailListPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.H5LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster

object BilibiliNavigation {

    private fun isNumeric(str: String): Boolean {
        return str.all { it.isDigit() }
    }

    private data class SimpleUri(
        val scheme: String?,
        val host: String?,
        val path: String?,
        val queryParams: Map<String, String>,
    ) {
        val queryParameterNames: Set<String> get() = queryParams.keys
        fun getQueryParameter(key: String): String? = queryParams[key]
    }

    private fun parseUrl(url: String): SimpleUri {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd == -1) return SimpleUri(null, null, url, emptyMap())
        val scheme = url.substring(0, schemeEnd)
        val rest = url.substring(schemeEnd + 3)
        val pathStart = rest.indexOf('/')
        val queryStart = rest.indexOf('?')
        val host = when {
            pathStart != -1 -> rest.substring(0, pathStart)
            queryStart != -1 -> rest.substring(0, queryStart)
            else -> rest
        }
        val path = if (pathStart != -1) {
            val end = if (queryStart != -1) queryStart else rest.length
            rest.substring(pathStart, end)
        } else null
        val query = if (queryStart != -1) rest.substring(queryStart + 1) else ""
        val queryParams = if (query.isNotEmpty()) {
            query.split("&").associate {
                val eq = it.indexOf('=')
                if (eq != -1) it.substring(0, eq) to it.substring(eq + 1)
                else it to ""
            }
        } else emptyMap()
        return SimpleUri(scheme, host, path, queryParams)
    }

    /**
     * 将 URI 解析为 [ComposePage]（NavKey）。无法识别返回 null。
     * 统一 bilimiao:// 与 bilibili:// 两种 scheme 的解析。
     */
    fun resolveUri(url: String): ComposePage? {
        miaoLogger() debug url
        // 1. 优先用 Nav3 声明式 UriDeepLinkMatcher 解析简单 pattern
        BilimiaoDeepLinks.match(url)?.let { return it as ComposePage }

        // 2. fallback: 复杂正则规则（BV/av/ss/ep/md 号、space.bilibili.com 等）
        val uri = parseUrl(url)
        if (uri.scheme == "http" || uri.scheme == "https") {
            Regex("BV([a-zA-Z0-9]{5,})").find(url)?.let {
                return VideoDetailPage(id = "BV${it.groupValues[1]}")
            }
            Regex("ss(\\d+)").find(url)?.let {
                return BangumiDetailPage(id = it.groupValues[1])
            }
            Regex("ep(\\d+)").find(url)?.let {
                return BangumiDetailPage(epId = it.groupValues[1])
            }
            Regex("md(\\d+)").find(url)?.let {
                return BangumiDetailPage(mediaId = it.groupValues[1])
            }
        }
        if (uri.host == "space.bilibili.com") {
            val mid = uri.path?.replace("/", "") ?: ""
            if (isNumeric(mid)) return UserSpacePage(mid)
        }
        if (uri.queryParameterNames.contains("avid")) {
            return VideoDetailPage(id = uri.getQueryParameter("avid")!!)
        }
        return null
    }


    fun navigationTo(
        pageNavigation: PageNavigator,
        url: String,
    ): Boolean {
        val page = resolveUri(url)
        if (page != null) {
            pageNavigation.navigate(page)
            return true
        }
        return pageNavigation.navigateByUri(url)
    }

    fun navigationToWeb(
        pageNavigation: PageNavigator,
        url: String,
    ) {
        val fullUrl = if ("://" in url) url else "http://$url"
        val uri = parseUrl(fullUrl)
        if (uri.scheme != "http" && uri.scheme != "https") {
            GlobalToaster.show("不支持的链接：$url")
            return
        }
        val host = uri.host ?: ""
        if ("bilibili.com" in host
            || "bilibili.tv" in host
            || "b23.tv" in host
            || "b23.snm0516.aisee.tv" in host) {
            pageNavigation.navigate(WebPage(fullUrl))
        } else {
            pageNavigation.launchWebBrowser(fullUrl)
        }
    }

}
