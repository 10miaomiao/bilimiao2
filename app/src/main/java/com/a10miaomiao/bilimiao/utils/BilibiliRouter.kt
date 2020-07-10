package com.a10miaomiao.bilimiao.utils

import android.content.Context
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import java.util.regex.Pattern

object BilibiliRouter {

    fun gotoUrl(context: Context, url: String): Boolean {
        val urlInfo = BiliUrlMatcher.findIDByUrl(url)
        val type = urlInfo[0].toUpperCase()
        val id = urlInfo[1]
        val activity = MainActivity.of(context)
        when(type){
            "AV" -> {
                activity.start(VideoInfoFragment.newInstance(id))
            }
            "BV" -> {
                activity.start(VideoInfoFragment.newInstanceByBvid(id))
            }
            "UID" -> {
                activity.start(UserFragment.newInstance(id.toLong()))
            }
            else -> {
                return false
            }
        }
        return true
    }

}