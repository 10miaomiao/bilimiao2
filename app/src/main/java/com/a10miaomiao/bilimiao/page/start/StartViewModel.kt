package com.a10miaomiao.bilimiao.page.start

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class StartViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: AppCompatActivity by instance()
    val ui: MiaoBindingUi by instance()

    val userStore: UserStore by instance()
    val messageStore: MessageStore by instance()
    val playerStore: PlayerStore by instance()

    var config: SearchConfigInfo? = null
    var searchMode = 0 // 0为全站搜索，1为页面自身搜索

    val navList = mutableListOf(
        StartNavInfo(
            title = "关注",
            pageUrl = "bilimiao://compose?url=user%2F{mid}%2Ffollow",
            iconRes = R.drawable.ic_nav_following,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "粉丝",
            pageUrl = "bilimiao://user/follow?id={mid}&type=fans&name={name}",
            iconRes = R.drawable.ic_nav_follower,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "收藏",
            pageUrl = "bilimiao://user/fav/list?mid={mid}&name={name}",
            iconRes = R.drawable.ic_nav_fav,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "追番",
            pageUrl = "bilimiao://user/bangumi",
            iconRes = R.drawable.ic_nav_bangumi,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "下载",
            pageUrl = composePageUrl(
                DownloadListPage().url()
            ),
            iconRes = R.drawable.ic_nav_download,
        ),
        StartNavInfo(
            title = "历史",
            pageUrl = "bilimiao://history",
            iconRes = R.drawable.ic_nav_history,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "稍后看",
            pageUrl = "bilimiao://watchlater",
            iconRes = R.drawable.ic_nav_watchlater,
            isNeedAuth = true,
        ),
        StartNavInfo(
            title = "设置",
            pageUrl = "bilimiao://setting",
            iconRes = R.drawable.ic_nav_setting,
        ),
    )

    fun showUnreadBadge(): Boolean {
        val unreadCount = messageStore.getUnreadCount()
        return unreadCount > 0
    }

    fun getUnreadCountText(): String {
        val unreadCount = messageStore.getUnreadCount()
        if (unreadCount > 99) {
            return "99+"
        }
        return unreadCount.toString()
    }

    private fun composePageUrl(url: String): String {
        return "bilimiao://compose?url=${Uri.encode(url)}"
    }

    data class StartNavInfo(
        val title: String,
        val pageUrl: String,
        @DrawableRes
        val iconRes: Int? = null,
        val iconUrl: String? = null,
        val isNeedAuth: Boolean = false,
    )

}