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

    fun navigationTo(
        view: View,
        url: String,
    ): Boolean {
        val nav = Navigation.findNavController(view)
        val uri = Uri.parse(url)
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