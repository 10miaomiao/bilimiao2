package com.a10miaomiao.bilimiao.comm.store

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import com.a10miaomiao.bilimiao.comm.db.FilterTagDB
import com.a10miaomiao.bilimiao.comm.db.FilterUpperDB
import com.a10miaomiao.bilimiao.comm.db.FilterWordDB
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.instance

class FilterStore(override val di: DI) :
    ViewModel(), BaseStore<FilterStore.State> {

    data class State (
        var filterWordList: MutableList<String> = mutableListOf(),
        var filterUpperList: MutableList<FilterUpperDB.Upper> = mutableListOf(),
        var filterTagList: MutableList<String> = mutableListOf()
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val activity: AppCompatActivity by instance()

    private val filterWordDB = FilterWordDB(activity)

    private val filterUpperDB = FilterUpperDB(activity)

    private val filterTagDB = FilterTagDB(activity)

    val filterWordCount get() = state.filterWordList.size

    val filterUpperCount get() = state.filterUpperList.size

    val filterTagCount get() = state.filterTagList.size
    init {
        queryFilterWord()
        queryFilterUpper()
        queryFilterTag()
    }

    fun queryFilterWord() {
        val list = filterWordDB.queryAll()
        setState {
            filterWordList = list
        }
    }

    fun queryFilterUpper() {
        val list = filterUpperDB.queryAll()
        setState {
            filterUpperList = list
        }
    }

    fun queryFilterTag() {
        val list = filterTagDB.queryAll()
        setState {
            filterTagList = list
        }
    }

    fun filterWord(text: String): Boolean {
        state.filterWordList.forEach {
            if (it.length > 2 && it.startsWith('/')  && it.endsWith('/')) {
                val regEx = it.substring(1, it.length - 1).toRegex()
                if (regEx.containsMatchIn(text))
                    return false
            } else {
                if (it in text)
                    return false
            }
        }
        return true
    }

    fun addWord(keyword: String) {
        filterWordDB.insert(keyword)
        queryFilterWord()
        PopTip.show("添加成功")
    }

    fun setWord(oldWord: String, newWord: String) {
        filterWordDB.updateKeyword(oldWord, newWord)
        queryFilterWord()
    }

    fun deleteWord(index: Int) {
        val keyword = state.filterWordList[index]
        filterWordDB.deleteByKeyword(keyword)
        queryFilterWord()
        PopTip.show("删除成功")
    }

    fun deleteWord(keywordList: List<String>) {
        keywordList.forEach {
            filterWordDB.deleteByKeyword(it)
        }
        queryFilterWord()
        PopTip.show("删除成功")
    }

    fun addUpper(mid: Long, name: String) {
        filterUpperDB.insert(mid, name)
        queryFilterUpper()
        PopTip.show("已添加屏蔽")
    }

    fun deleteUpper(mid: Long) {
        filterUpperDB.deleteByMid(mid)
        queryFilterUpper()
        PopTip.show("已取消屏蔽")
    }

    fun deleteUpper(midList: List<Long>) {
        midList.forEach {
            filterUpperDB.deleteByMid(it)
        }
        queryFilterUpper()
        PopTip.show("删除成功")
    }

    fun filterUpper(mid: String) = filterUpper(mid.toLong())
    fun filterUpper(mid: Long): Boolean {
        state.filterUpperList.forEach {
            if (it.mid == mid)
                return false
        }
        return true
    }

    fun filterUpperName(name: String): Boolean {
        // TODO: 筛选UP主昵称
        return true
    }

    fun filterTag(text: List<String>): Boolean {
        text.forEach {
            if (state.filterTagList.contains(it)) {
                return false
            }
        }
        return true
    }

    /**
     * 根据av号或bv号筛选视频标签
     */
    suspend fun filterTag(
        id: String, // av号或bv号
    ): Boolean {
        if (state.filterTagList.isEmpty()) {
            return true
        }
        val req = if (id.startsWith("BV")) {
            ViewReq(
                bvid = id,
            )
        } else {
            ViewReq(
                aid = id.toLong(),
            )
        }
        val res = BiliGRPCHttp.request {
            ViewGRPC.view(req)
        }.awaitCall()
        val tags = res.tag.map { it.name }
        miaoLogger() debug tags
        return filterTag(tags)
    }

    fun addTag(name: String) {
        filterTagDB.insert(name)
        queryFilterTag()
        PopTip.show("添加成功")
    }

    fun setTag(old: String, new: String) {
        filterTagDB.updateTagName(old, new)
        queryFilterWord()
    }

    fun deleteTag(index: Int) {
        val name = state.filterTagList[index]
        filterTagDB.deleteByTagName(name)
        queryFilterTag()
        PopTip.show("删除成功")
    }

    fun deleteTag(tagList: List<String>) {
        tagList.forEach {
            filterTagDB.deleteByTagName(it)
        }
        queryFilterTag()
        PopTip.show("删除成功")
    }

    fun filterTagListIsEmpty() = state.filterTagList.isEmpty()
}