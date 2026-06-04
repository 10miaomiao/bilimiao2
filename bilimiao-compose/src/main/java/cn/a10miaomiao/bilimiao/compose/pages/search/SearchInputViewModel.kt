package cn.a10miaomiao.bilimiao.compose.pages.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.db.createSearchHistoryDatabase
import com.a10miaomiao.bilimiao.comm.db.entity.SearchHistoryEntity
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONTokener
import org.kodein.di.DI
import org.kodein.di.DIAware

class SearchInputViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val historyListFlow = MutableStateFlow(listOf<SuggestInfo>())
    val historyList get() = historyListFlow
    val suggestListFlow = MutableStateFlow(listOf<SuggestInfo>())
    val suggestList get() = suggestListFlow.value

    var config: SearchConfigInfo? = null
    var searchMode = 0 // 0为全站搜索，1为页面自身搜索

    private val searchHistoryDao = createSearchHistoryDatabase().searchHistoryDao()

    init {
        updateHistoryList()
    }

    private fun updateHistoryList() {
        historyListFlow.value = runBlocking { searchHistoryDao.queryAllHistory() }.map {
            SuggestInfo(
                text = it.keyword,
                type = SuggestType.HISTORY,
                value = it.keyword,
            )
        }
    }

    private fun getInitSuggestData(
        keyword: String
    ) = mutableListOf<SuggestInfo>().apply {
        if (isNumeric(keyword)) {
            add(
                SuggestInfo(
                    text = "AV$keyword",
                    type = SuggestType.AV,
                    value = keyword,
                )
            )
            add(
                SuggestInfo(
                    text = "SS$keyword",
                    type = SuggestType.SS,
                    value = keyword,
                )
            )
        }
    }

    fun loadSuggestData(keyword: String, currentText: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (keyword.isEmpty()) {
                return@launch
            }
            suggestListFlow.value = getInitSuggestData(keyword)
            try {
                val res = BiliApiService.searchApi.suggestList(keyword).awaitCall()
                val jsonStr = res.body!!.string()
                val jsonParser = JSONTokener(jsonStr)
                val jsonArray =
                    (jsonParser.nextValue() as JSONObject).getJSONObject("result")
                        .getJSONArray("tag")
                if (keyword == currentText) {
                    suggestListFlow.value = getInitSuggestData(keyword).apply {
                        for (i in 0 until jsonArray.length()) {
                            val value = jsonArray.getJSONObject(i).getString("value")
                            add(
                                SuggestInfo(
                                    text = value,
                                    value = value,
                                    type = SuggestType.TEXT
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun addSearchHistory(text: String) {
        runBlocking {
            searchHistoryDao.deleteHistory(text)
            searchHistoryDao.insertHistory(SearchHistoryEntity(keyword = text))
        }
        updateHistoryList()
    }

    fun deleteSearchHistory(text: String) {
        runBlocking { searchHistoryDao.deleteHistory(text) }
        updateHistoryList()
    }

    fun deleteAllSearchHistory() {
        runBlocking { searchHistoryDao.deleteAllHistory() }
        updateHistoryList()
    }

    fun isNumeric(s: String): Boolean {
        return s.toCharArray().all { Character.isDigit(it) }
    }

    enum class SuggestType {
        TEXT, // 普通文本
        SEARCH, // 直接搜索
        AV, // 视频ID，AV号跳转
        SS, // 番剧ID，SS号跳转
        HISTORY, // 历史搜索
    }

    data class SuggestInfo(
        val text: String, // 显示文字
        val type: SuggestType,
        val value: String,
    )
}

