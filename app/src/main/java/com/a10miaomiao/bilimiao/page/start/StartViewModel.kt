package com.a10miaomiao.bilimiao.page.start

import android.net.Uri
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.PageRoute
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.db.SearchHistoryDB
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.search.SearchResultFragment
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class StartViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: AppCompatActivity by instance()
    val ui: MiaoBindingUi by instance()

    val userStore: UserStore by instance()
    val messageStore: MessageStore by instance()

    var config: SearchConfigInfo? = null
    var searchMode = 0 // 0为全站搜索，1为页面自身搜索

    val navList = mutableListOf(
        StartNavInfo(
            title = "关注",
            pageUrl = "bilimiao://compose?url=bilimiao%3A%2F%2Fuser%2F{mid}%2Ffollow",
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
                PageRoute.Download.list.url()
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