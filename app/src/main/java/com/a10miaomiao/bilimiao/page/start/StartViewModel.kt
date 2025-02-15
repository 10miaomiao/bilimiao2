package com.a10miaomiao.bilimiao.page.start

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
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
    val playListStore: PlayListStore by instance()

    var config: SearchConfigInfo? = null
    var searchMode = 0 // 0为全站搜索，1为页面自身搜索

    val navList = mutableListOf(
        StartNavInfo(
            title = "关注",
            iconRes = R.drawable.ic_nav_following,
            isNeedAuth = true,
            pageUrl = "bilimiao://mine/follow",
        ),
        StartNavInfo(
            title = "粉丝",
            iconRes = R.drawable.ic_nav_follower,
            isNeedAuth = true,
            pageUrl = "bilimiao://web/${Uri.encode("https://space.bilibili.com/h5/follow?type=fans")}",
        ),
        StartNavInfo(
            title = "收藏",
            iconRes = R.drawable.ic_nav_fav,
            isNeedAuth = true,
            pageUrl = "bilimiao://user/favourite/{mid}",
        ),
        StartNavInfo(
            title = "追番/剧",
            iconRes = R.drawable.ic_nav_bangumi,
            isNeedAuth = true,
            pageUrl = "bilimiao://mine/bangumi",
        ),
        StartNavInfo(
            title = "下载",
            iconRes = R.drawable.ic_nav_download,
            pageUrl = "bilimiao://download",
        ),
        StartNavInfo(
            title = "历史",
            iconRes = R.drawable.ic_nav_history,
            isNeedAuth = false,
            pageUrl = "bilimiao://mine/history",
        ),
        StartNavInfo(
            title = "稍后看",
            iconRes = R.drawable.ic_nav_watchlater,
            isNeedAuth = true,
            pageUrl = "bilimiao://mine/watchlater",
        ),
        StartNavInfo(
            title = "设置",
            iconRes = R.drawable.ic_nav_setting,
            pageUrl = "bilimiao://setting",
        ),
        StartNavInfo(
            title = "歌词",
            iconRes = R.drawable.ic_nav_lyric,
            pageUrl = "bilimiao://lyric",
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

    data class StartNavInfo(
        val title: String,
        val pageUrl: String? = null,
        @DrawableRes
        val iconRes: Int? = null,
        val iconUrl: String? = null,
        val isNeedAuth: Boolean = false,
    )

}