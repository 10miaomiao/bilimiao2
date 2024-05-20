package com.a10miaomiao.bilimiao.comm

import android.app.Activity
import android.net.Uri
import android.util.TypedValue
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.navigation.pointerOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.stopSameIdAndArgs
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.page.web.WebFragment
import java.util.regex.Pattern

object BiliNavigation {

    private fun isNumeric(str: String): Boolean {
        val pattern = Pattern.compile("[0-9]*")
        return pattern.matcher(str).matches()
    }

    fun navigationTo(
        view: View,
        url: String,
    ): Boolean {
        val nav = Navigation.findNavController(view)
        return navigationTo(nav, url)
    }
    fun navigationTo(
        nav: NavController,
        url: String,
    ): Boolean {
        if (url.indexOf("http") == 0) {
            var compile = Pattern.compile("BV([a-zA-Z0-9]{5,})")
            var matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                val args = VideoInfoFragment.createArguments("BV$id")
                nav.navigate(VideoInfoFragment.actionId, args)
                return true
            }
            compile = Pattern.compile("ss(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                nav.navigateToCompose(BangumiDetailPage()) {
                    id set matcher.group(1)
                }
                return true
            }
            compile = Pattern.compile("ep(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                nav.navigateToCompose(BangumiDetailPage()) {
                    epId set matcher.group(1)
                }
                return true
            }
            compile = Pattern.compile("md(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                nav.navigateToCompose(BangumiDetailPage()) {
                    mediaId set matcher.group(1)
                }
                return true
            }
        }
        val uri = Uri.parse(url)
        val queryParameterNames = uri.queryParameterNames
        var argId = ""
        var actionId = 0
        val host = uri.host

        if (host == "space.bilibili.com") {
            val path = uri.path!!.replace("/", "")
            argId = if (isNumeric(path)) { path } else { "" }
            actionId = UserFragment.actionId
        }
        if (queryParameterNames.contains("avid")) {
            argId = uri.getQueryParameter("avid")!!
            actionId = VideoInfoFragment.actionId
        }

        if (actionId != 0) {
            val args = bundleOf(
                MainNavArgs.id to argId,
            )
            nav.navigate(actionId, args)
            return true
        }
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.miao_fragment_open_enter)
            .setExitAnim(R.anim.miao_fragment_open_exit)
            .setPopEnterAnim(R.anim.miao_fragment_close_enter)
            .setPopExitAnim(R.anim.miao_fragment_close_exit)
            .build()
        return try {
            nav.navigate(uri, navOptions)
            true
        } catch (e: IllegalArgumentException) {
            miaoLogger() debug "未知url:$url"
            false
        }
    }

    fun navigationToWeb(
        activity: Activity,
        url: String,
    ) {
        val uri = Uri.parse(
            if ("://" in url) {
                url
            } else {
                "http://$url"
            }
        )
        val host = uri.host ?: ""
        if ("bilibili.com" in host
            || "bilibili.tv" in host
            || "b23.tv" in host
            || "b23.snm0516.aisee.tv" in host) {
            // b站网页使用内部浏览器打开
            val nav = activity.findNavController(R.id.nav_host_fragment).pointerOrSelf()
            val args = WebFragment.createArguments(uri.toString())
            nav.stopSameIdAndArgs(WebFragment.id,args)
                ?.navigate(
                    WebFragment.actionId,
                    args
                )
        } else {
            // 非B站网页使用外部浏览器打开
            val typedValue = TypedValue()
            val attrId = com.google.android.material.R.attr.colorSurfaceVariant
            activity.theme.resolveAttribute(attrId, typedValue, true)
            val intent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(ContextCompat.getColor(activity, typedValue.resourceId))
                        .build()
                )
                .build()
            intent.launchUrl(activity, uri)
        }
    }

}