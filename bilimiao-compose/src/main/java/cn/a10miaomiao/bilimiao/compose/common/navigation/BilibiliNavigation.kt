package cn.a10miaomiao.bilimiao.compose.common.navigation

import android.app.Activity
import android.net.Uri
import android.util.TypedValue
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.NavController
import androidx.navigation.Navigation
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.PopTip
import java.util.regex.Pattern

object BilibiliNavigation {

    private fun isNumeric(str: String): Boolean {
        val pattern = Pattern.compile("[0-9]*")
        return pattern.matcher(str).matches()
    }

    fun navigationTo(
        pageNavigation: PageNavigation,
        url: String,
    ): Boolean {
        miaoLogger() debug url
        val uri = Uri.parse(url)
        if (uri.scheme == "http" || uri.scheme == "https") {
            var compile = Pattern.compile("BV([a-zA-Z0-9]{5,})")
            var matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                pageNavigation.navigateToVideoInfo("BV$id")
                return true
            }
            compile = Pattern.compile("ss(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                pageNavigation.navigate(
                    BangumiDetailPage(
                        id = matcher.group(1)
                    )
                )
                return true
            }
            compile = Pattern.compile("ep(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                pageNavigation.navigate(
                    BangumiDetailPage(
                        epId = matcher.group(1)
                    )
                )
                return true
            }
            compile = Pattern.compile("md(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                pageNavigation.navigate(
                    BangumiDetailPage(
                        mediaId = matcher.group(1)
                    )
                )
                return true
            }
        }
        val host = uri.host
        if (host == "space.bilibili.com") {
            val path = uri.path!!.replace("/", "")
            val mid = if (isNumeric(path)) { path } else { "" }
            pageNavigation.navigate(
                UserSpacePage(mid)
            )
            return true
        }
        val queryParameterNames = uri.queryParameterNames
        if (queryParameterNames.contains("avid")) {
            val aid = uri.getQueryParameter("avid")!!
            pageNavigation.navigateToVideoInfo(aid)
            return true
        }

        return pageNavigation.navigateByUri(uri)
    }

    fun navigationToWeb(
        pageNavigation: PageNavigation,
        url: String,
    ) {
        val uri = Uri.parse(
            if ("://" in url) {
                url
            } else {
                "http://$url"
            }
        )
        if (uri.scheme != "http" && uri.scheme != "https") {
            PopTip.show("不支持的链接：${url}")
            return
        }
        val host = uri.host ?: ""
        if ("bilibili.com" in host
            || "bilibili.tv" in host
            || "b23.tv" in host
            || "b23.snm0516.aisee.tv" in host) {
            // b站网页使用内部浏览器打开
            pageNavigation.navigate(
                WebPage(url)
            )
        } else {
            // 非B站网页使用外部浏览器打开
            pageNavigation.launchWebBrowser(uri)
        }
    }

}