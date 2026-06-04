package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import com.a10miaomiao.bilimiao.comm.db.createFilterDatabase
import com.a10miaomiao.bilimiao.comm.db.entity.FilterUpperEntity
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI

class FilterStore(override val di: DI) :
    ViewModel(), BaseStore<FilterStore.State> {

    data class State (
        var filterWordList: MutableList<String> = mutableListOf(),
        var filterUpperList: MutableList<FilterUpperEntity> = mutableListOf(),
        var filterTagList: MutableList<String> = mutableListOf()
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val db = createFilterDatabase()
    private val filterWordDao = db.filterWordDao()
    private val filterUpperDao = db.filterUpperDao()
    private val filterTagDao = db.filterTagDao()

    val filterWordCount get() = state.filterWordList.size

    val filterUpperCount get() = state.filterUpperList.size

    val filterTagCount get() = state.filterTagList.size
    init {
        queryFilterWord()
        queryFilterUpper()
        queryFilterTag()
    }

    fun queryFilterWord() {
        val list = runBlocking { filterWordDao.queryAll().map { it.keyword }.toMutableList() }
        setState {
            filterWordList = list
        }
    }

    fun queryFilterUpper() {
        val list = runBlocking { filterUpperDao.queryAll().toMutableList() }
        setState {
            filterUpperList = list
        }
    }

    fun queryFilterTag() {
        val list = runBlocking { filterTagDao.queryAll().map { it.name }.toMutableList() }
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
        runBlocking { filterWordDao.insert(com.a10miaomiao.bilimiao.comm.db.entity.FilterWordEntity(keyword = keyword)) }
        queryFilterWord()
        GlobalToaster.show("添加成功")
    }

    fun setWord(oldWord: String, newWord: String) {
        runBlocking { filterWordDao.updateKeyword(oldWord, newWord) }
        queryFilterWord()
    }

    fun deleteWord(index: Int) {
        val keyword = state.filterWordList[index]
        runBlocking { filterWordDao.deleteByKeyword(keyword) }
        queryFilterWord()
        GlobalToaster.show("删除成功")
    }

    fun deleteWord(keywordList: List<String>) {
        keywordList.forEach {
            runBlocking { filterWordDao.deleteByKeyword(it) }
        }
        queryFilterWord()
        GlobalToaster.show("删除成功")
    }

    fun addUpper(mid: Long, name: String) {
        runBlocking { filterUpperDao.insert(FilterUpperEntity(mid = mid, name = name)) }
        queryFilterUpper()
        GlobalToaster.show("已添加屏蔽")
    }

    fun deleteUpper(mid: Long) {
        runBlocking { filterUpperDao.deleteByMid(mid) }
        queryFilterUpper()
        GlobalToaster.show("已取消屏蔽")
    }

    fun deleteUpper(midList: List<Long>) {
        midList.forEach {
            runBlocking { filterUpperDao.deleteByMid(it) }
        }
        queryFilterUpper()
        GlobalToaster.show("删除成功")
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
        return filterTag(tags)
    }

    fun addTag(name: String) {
        runBlocking { filterTagDao.insert(com.a10miaomiao.bilimiao.comm.db.entity.FilterTagEntity(name = name)) }
        queryFilterTag()
        GlobalToaster.show("添加成功")
    }

    fun setTag(old: String, new: String) {
        runBlocking { filterTagDao.updateTagName(old, new) }
        queryFilterTag()
    }

    fun deleteTag(index: Int) {
        val name = state.filterTagList[index]
        runBlocking { filterTagDao.deleteByTagName(name) }
        queryFilterTag()
        GlobalToaster.show("删除成功")
    }

    fun deleteTag(tagList: List<String>) {
        tagList.forEach {
            runBlocking { filterTagDao.deleteByTagName(it) }
        }
        queryFilterTag()
        GlobalToaster.show("删除成功")
    }

    fun filterTagListIsEmpty() = state.filterTagList.isEmpty()
}