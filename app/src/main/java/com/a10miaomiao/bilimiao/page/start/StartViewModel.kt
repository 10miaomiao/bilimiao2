package com.a10miaomiao.bilimiao.page.start

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
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
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.MyFollow,
        ),
        StartNavInfo(
            title = "粉丝",
            iconRes = R.drawable.ic_nav_follower,
            isNeedAuth = true,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.MyFans,
        ),
        StartNavInfo(
            title = "收藏",
            iconRes = R.drawable.ic_nav_fav,
            isNeedAuth = true,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.UserFavourite,
            composeParam = "{mid}"
        ),
        StartNavInfo(
            title = "追番",
            iconRes = R.drawable.ic_nav_bangumi,
            isNeedAuth = true,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.BangumiFollow,
        ),
        StartNavInfo(
            title = "下载",
            iconRes = R.drawable.ic_nav_download,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.DownloadList,
        ),
        StartNavInfo(
            title = "历史",
            iconRes = R.drawable.ic_nav_history,
            isNeedAuth = false,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.History,
        ),
        StartNavInfo(
            title = "稍后看",
            iconRes = R.drawable.ic_nav_watchlater,
            isNeedAuth = true,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.WatchLater,
        ),
        StartNavInfo(
            title = "设置",
            iconRes = R.drawable.ic_nav_setting,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.Setting,
        ),
        StartNavInfo(
            title = "歌词",
            iconRes = R.drawable.ic_nav_lyric,
            isComposePage = true,
            composeEntry = BilimiaoPageRoute.Entry.Lyric,
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
        val isComposePage: Boolean = false,
        val composeEntry: BilimiaoPageRoute.Entry? = null,
        val composeParam: String? = null,
    )

}