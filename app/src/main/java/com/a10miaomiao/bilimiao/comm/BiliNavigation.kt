package com.a10miaomiao.bilimiao.comm

import android.net.Uri
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
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
        if (url.indexOf("http") == 0) {
            var compile = Pattern.compile("BV([a-zA-Z0-9]{5,})")
            var matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                val args = bundleOf(
                    MainNavGraph.args.id to "BV$id",
                )
                Navigation.findNavController(view)
                    .navigate(MainNavGraph.action.global_to_videoInfo, args)
                return true
            }
            compile = Pattern.compile("ss(\\d+)")
            matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                val args = bundleOf(
                    MainNavGraph.args.id to id,
                )
                Navigation.findNavController(view)
                    .navigate(MainNavGraph.action.global_to_bangumiDetail, args)
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
            actionId = MainNavGraph.action.global_to_user
        }
        if (queryParameterNames.contains("avid")) {
            argId = uri.getQueryParameter("avid")!!
            actionId = MainNavGraph.action.global_to_videoInfo
        }

        if (actionId != 0) {
            val args = bundleOf(
                MainNavGraph.args.id to argId,
            )
            Navigation.findNavController(view)
                .navigate(actionId, args)
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
            DebugMiao.log("未知url:$url")
            false
        }
    }

}