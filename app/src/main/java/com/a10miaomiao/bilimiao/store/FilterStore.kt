package com.a10miaomiao.bilimiao.store

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.db.FilterUpperDB
import com.a10miaomiao.bilimiao.comm.db.FilterWordDB
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.instance
import splitties.toast.toast
import java.util.regex.Pattern

class FilterStore(override val di: DI) :
    ViewModel(), BaseStore<FilterStore.State> {

    data class State (
        var filterWordList: MutableList<String> = mutableListOf(),
        var filterUpperList: MutableList<FilterUpperDB.Upper> = mutableListOf(),
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val activity: AppCompatActivity by instance()

    private val filterWordDB = FilterWordDB(activity)

    private val filterUpperDB = FilterUpperDB(activity)

    val filterWordCount get() = state.filterWordList.size

    val filterUpperCount get() = state.filterUpperList.size

    init {
        queryFilterWord()
        queryFilterUpper()
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


    fun filterWord(text: String): Boolean {
        state.filterWordList.forEach {
            if (it.length > 2 && it[0] == '/' && it[it.length - 1] == '/') {
                val regEx = it.substring(1, it.length - 1)
                val pattern = Pattern.compile(regEx)
                if (pattern.matcher(text).matches())
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
        activity.toast("添加成功")
    }

    fun setWord(oldWord: String, newWord: String) {
        filterWordDB.updateKeyword(oldWord, newWord)
        queryFilterWord()
    }

    fun deleteWord(index: Int) {
        val keyword = state.filterWordList[index]
        filterWordDB.deleteByKeyword(keyword)
        queryFilterWord()
        activity.toast("删除成功")
    }

    fun deleteWord(keywordList: List<String>) {
        keywordList.forEach {
            filterWordDB.deleteByKeyword(it)
        }
        queryFilterWord()
        activity.toast("删除成功")
    }

    fun addUpper(mid: Long, name: String) {
        filterUpperDB.insert(mid, name)
        queryFilterUpper()
        activity.toast("已添加屏蔽")
    }

    fun deleteUpper(mid: Long) {
        filterUpperDB.deleteByMid(mid)
        queryFilterUpper()
        activity.toast("已取消屏蔽")
    }

    fun deleteUpper(midList: List<Long>) {
        midList.forEach {
            filterUpperDB.deleteByMid(it)
        }
        queryFilterUpper()
        activity.toast("删除成功")
    }

    fun filterUpper(mid: String) = filterUpper(mid.toLong())
    fun filterUpper(mid: Long): Boolean {
        state.filterUpperList.forEach {
            if (it.mid == mid)
                return false
        }
        return true
    }
}