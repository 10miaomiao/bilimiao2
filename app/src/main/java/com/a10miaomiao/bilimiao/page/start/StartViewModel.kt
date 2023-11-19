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
import com.a10miaomiao.bilimiao.comm.navigation.closeSearchDrawer
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
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

    var historyList = mutableListOf<String>()
    var suggestList = mutableListOf<SuggestInfo>()

    var searchFocus = false

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
//        StartNavInfo(
//            title = "稍后看",
//            pageUrl = "",
//            iconRes = R.drawable.ic_nav_watchlater,
//            isNeedAuth = true,
//        ),
    )

    private val searchHistoryDB = SearchHistoryDB(activity, SearchHistoryDB.DB_NAME, null, 1)

    init {
        historyList = searchHistoryDB.queryAllHistory()
    }

    private fun composePageUrl(url: String): String {
        return "bilimiao://compose?url=${Uri.encode(url)}"
    }

    fun initSuggestData(keyword: String) {
        suggestList = mutableListOf(
            SuggestInfo(
                text = "直接搜索“${keyword}”",
                type = "SEARCH",
                value = keyword,
            )
        )
        if (isNumeric(keyword)) {
            suggestList.add(
                SuggestInfo(
                    text = "查看视频“AV${keyword}”",
                    type = "AV",
                    value = keyword,
                )
            )
            suggestList.add(
                SuggestInfo(
                    text = "查看番剧“SS${keyword}”",
                    type = "SS",
                    value = keyword,
                )
            )
        }
    }

    /**
     * 加载搜索提示
     */
    fun loadSuggestData(keyword: String, editText: EditText) = viewModelScope.launch(Dispatchers.IO) {
        if (keyword.isEmpty()) {
            ui.setState {
                suggestList.clear()
            }
            return@launch
        }
        ui.setState {
            initSuggestData(keyword)
        }
        try {
            val res = BiliApiService.searchApi.suggestList(keyword).awaitCall()
            val jsonStr = res.body!!.string()
            val jsonParser = JSONTokener(jsonStr)
            val jsonArray = (jsonParser.nextValue() as JSONObject).getJSONObject("result").getJSONArray("tag")
            if (keyword == editText.text.toString()) {
                ui.setState {
                    initSuggestData(keyword)
                    for (i in 0 until jsonArray.length()) {
                        val value = jsonArray.getJSONObject(i).getString("value")
                        suggestList.add(SuggestInfo(
                            text = value,
                            value = value,
                            type = "TEXT"
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开始搜索
     */
    fun startSearch(keyword: String, view: View) {
        if (keyword.isEmpty()) {
            PopTip.show("请输入ID或关键字")
            return
        }
        searchHistoryDB.deleteHistory(keyword)
        searchHistoryDB.insertHistory(keyword)
        activity.getScaffoldView().closeSearchDrawer()
        if (searchMode == 0) {
            val nav = activity.findNavController(R.id.nav_host_fragment)
            val args = bundleOf(
                MainNavArgs.text to keyword
            )
            nav.navigate(SearchResultFragment.actionId, args)
        } else {
            (activity as? MainActivity)?.searchSelfPage(keyword)
        }
        ui.setState {
            historyList = searchHistoryDB.queryAllHistory()
        }

    }

    fun deleteSearchHistory(text: String){
        searchHistoryDB.deleteHistory(text)
        ui.setState {
            historyList = searchHistoryDB.queryAllHistory()
        }
    }

    fun deleteAllSearchHistory(){
        searchHistoryDB.deleteAllHistory()
        ui.setState {
            historyList.clear()
        }
    }

    fun setSearchFocusState(focus: Boolean) {
        ui.setState {
            searchFocus = focus
        }
    }

    /**
     * 字符串是否为数字
     */
    fun isNumeric(s: String): Boolean {
        return s.toCharArray().all { Character.isDigit(it) }
    }

    data class SuggestInfo(
        val text: String, // 显示文字
        val type: String, // 类型：TEXT:普通文字、SEARCH:直接搜索、AV:视频ID、SS:番剧ID
        val value: String,
    )

    data class StartNavInfo(
        val title: String,
        val pageUrl: String,
        @DrawableRes
        val iconRes: Int? = null,
        val iconUrl: String? = null,
        val isNeedAuth: Boolean = false,
    )

}