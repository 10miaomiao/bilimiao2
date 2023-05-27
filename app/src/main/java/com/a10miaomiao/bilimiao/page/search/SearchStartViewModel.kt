package com.a10miaomiao.bilimiao.page.search

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.db.SearchHistoryDB
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class SearchStartViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: AppCompatActivity by instance()
    val ui: MiaoBindingUi by instance()

    var historyList = mutableListOf<String>()
    var suggestList = mutableListOf<SuggestInfo>()

    val allSearchAction = SearchResultFragment.actionId
    var selfSearchAction = -1
    var searchAction = allSearchAction

    private val searchHistoryDB = SearchHistoryDB(activity, SearchHistoryDB.DB_NAME, null, 1)

    init {
        historyList = searchHistoryDB.queryAllHistory()
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
            toast("请输入ID或关键字")
            return
        }
        searchHistoryDB.deleteHistory(keyword)
        searchHistoryDB.insertHistory(keyword)

        activity.getScaffoldView().closeDrawer()
//        Navigation.findNavController(view).popBackStack()

        val nav = activity.findNavController(R.id.nav_host_fragment)
        val args = bundleOf(
            MainNavArgs.text to keyword
        )
        if (searchAction == -1) {
            nav.navigate(allSearchAction, args)
        } else {
            nav.navigate(searchAction, args)
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

}