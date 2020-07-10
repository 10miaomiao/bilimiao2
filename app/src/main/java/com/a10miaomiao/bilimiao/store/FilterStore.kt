package com.a10miaomiao.bilimiao.store

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.db.FilterUpperDB
import com.a10miaomiao.bilimiao.db.FilterWordDB
import org.jetbrains.anko.toast
import java.util.regex.Pattern

class FilterStore(
        val context: Context
): ViewModel() {

    private val filterWordDB = FilterWordDB(context)
    private val _filterWordList = MutableLiveData<ArrayList<String>>()
    val filterWordList: LiveData<ArrayList<String>>
        get() = _filterWordList
    val filterWordCount get() = filterWordList.value?.size ?: 0

    private val filterUpperDB = FilterUpperDB(context)
    private val _filterUpperList = MutableLiveData<ArrayList<FilterUpperDB.Upper>>()
    val filterUpperList: LiveData<ArrayList<FilterUpperDB.Upper>>
        get() = _filterUpperList
    val filterUpperCount get() = filterUpperList.value?.size ?: 0


    init {
        queryFilterWord()
        queryFilterUpper()
    }

    fun queryFilterWord() {
        val list = filterWordDB.queryAll()
        _filterWordList.value = list
    }

    fun queryFilterUpper() {
        val list = filterUpperDB.queryAll()
        _filterUpperList.value = list
    }


    fun filterWord(text: String): Boolean {
        filterWordList.value?.forEach {
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
        context.toast("添加成功")
    }

    fun setWord(oldWord: String, newWord: String) {
        filterWordDB.updateKeyword(oldWord, newWord)
        queryFilterWord()
    }

    fun deleteWord(index: Int) {
        val keyword = filterWordList.value!![index]
        filterWordDB.deleteByKeyword(keyword)
        queryFilterWord()
        context.toast("删除成功")
    }

    fun deleteWord(keywordList: List<String>) {
        keywordList.forEach {
            filterWordDB.deleteByKeyword(it)
        }
        queryFilterWord()
        context.toast("删除成功")
    }

    fun addUpper(mid: Long, name: String) {
        filterUpperDB.insert(mid, name)
        queryFilterUpper()
        context.toast("已添加屏蔽")
    }

    fun deleteUpper(mid: Long) {
        filterUpperDB.deleteByMid(mid)
        queryFilterUpper()
        context.toast("已取消屏蔽")
    }

    fun deleteUpper(midList: List<Long>) {
        midList.forEach {
            filterUpperDB.deleteByMid(it)
        }
        queryFilterUpper()
        context.toast("删除成功")
    }

    fun filterUpper(mid: Long): Boolean {
        filterUpperList.value?.forEach {
            if (it.mid == mid)
                return false
        }

        return true
    }


}