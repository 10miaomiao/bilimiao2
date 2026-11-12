package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import com.a10miaomiao.bilimiao.comm.db.FilterImportPreview
import com.a10miaomiao.bilimiao.comm.db.FilterMergeResult
import com.a10miaomiao.bilimiao.comm.db.createFilterDatabase
import com.a10miaomiao.bilimiao.comm.db.entity.FilterUpperEntity
import com.a10miaomiao.bilimiao.comm.db.mergeFilterBackup
import com.a10miaomiao.bilimiao.comm.db.previewFilterBackup
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupInfo
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupSelection
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupUpper
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import kotlin.time.Clock

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

    /**
     * 生成屏蔽设置备份，供导出为文本文件
     */
    fun buildBackup(selection: FilterBackupSelection = FilterBackupSelection.All): FilterBackupInfo =
        FilterBackupInfo(
            exportedAt = Clock.System.now().toString(),
            filterWords = state.filterWordList.toList(),
            filterUppers = state.filterUpperList.map { FilterBackupUpper(uid = it.mid, name = it.name) },
            filterTags = state.filterTagList.toList(),
        ).selected(selection)

    /**
     * 预览导入结果（不写库），供确认对话框展示“共 n 条 / 可导入 x 条 …”
     */
    suspend fun previewImport(
        backup: FilterBackupInfo,
        selection: FilterBackupSelection = FilterBackupSelection.All,
    ): FilterImportPreview = previewFilterBackup(db, backup, selection)

    /**
     * 把备份合并进当前设置，返回各项实际新增的条数。
     * 相同内容不会重复写入，因此同一份备份重复导入是安全的。
     */
    fun importBackup(
        backup: FilterBackupInfo,
        selection: FilterBackupSelection = FilterBackupSelection.All,
    ): FilterMergeResult {
        val result = runBlocking { mergeFilterBackup(db, backup, selection) }
        // 只刷新真正发生变化的列表，避免无谓的状态抖动
        if (result.addedWords > 0) queryFilterWord()
        if (result.addedUppers > 0) queryFilterUpper()
        if (result.addedTags > 0) queryFilterTag()
        return result
    }
}