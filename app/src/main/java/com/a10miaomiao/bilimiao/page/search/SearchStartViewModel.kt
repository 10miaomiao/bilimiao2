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
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
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
    var suggestList = mutableListOf<String>()

    private val searchHistoryDB = SearchHistoryDB(activity, SearchHistoryDB.DB_NAME, null, 1)

    init {
        historyList = searchHistoryDB.queryAllHistory()
    }

    /**
     * 加载搜索提示
     */
    fun loadSuggestData(keyword: String, editText: EditText) = viewModelScope.launch(Dispatchers.IO) {
        if (keyword.isEmpty()) {
            ui.setState {
                suggestList.clear()
            }
        }
        try {
            val res = BiliApiService.searchApi.suggestList(keyword).awaitCall()
            val jsonStr = res.body()!!.string()
            val jsonParser = JSONTokener(jsonStr)
            val jsonArray = (jsonParser.nextValue() as JSONObject).getJSONObject("result").getJSONArray("tag")
            if (keyword == editText.text.toString()) {
                ui.setState {
                    suggestList = (0 until jsonArray.length()).map {
                        jsonArray.getJSONObject(it).getString("value")
                    }.toMutableList()
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
        Navigation.findNavController(view).popBackStack()
        val nav = activity.findNavController(R.id.nav_host_fragment)
        val args = bundleOf(
            MainNavGraph.args.text to keyword
        )
        nav.navigate(MainNavGraph.action.global_to_searchResult, args)
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

}