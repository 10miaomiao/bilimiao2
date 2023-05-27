package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.middleware.v1.Preload
import bilibili.app.interfaces.v1.HistoryGrpc
import bilibili.app.interfaces.v1.HistoryOuterClass
import bilibili.main.community.reply.v1.ReplyOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class HistoryViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text) }

    var triggered = false
    var list = PaginationInfo<HistoryOuterClass.CursorItem>()

    private var _mapTp = 3
    private var _maxId = 0L
    private var _viewAt = 0L

    init {
        loadData(0L)
    }

    private fun loadData(
        maxId: Long = _maxId
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val _keyword = keyword ?: ""
            if (_keyword.isBlank()) {
                loadList(maxId)
            } else {
                searchList(_keyword!!, maxId + 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
        } finally {
            ui.setState {
                list.loading = false
                triggered = false
            }
        }
    }

    private suspend fun loadList(
        maxId: Long,
    ) {
        val req = HistoryOuterClass.CursorV2Req.newBuilder().apply {
            business = "archive"
            cursor = HistoryOuterClass.Cursor.newBuilder().apply {
                if (maxId != 0L) {
                    max = maxId
                    maxTp = _mapTp // 本页最大值游标类型
                }
            }.build()
        }.build()
        val res = HistoryGrpc.getCursorV2Method()
            .request(req)
            .awaitCall()
        if (maxId == 0L){
            list.data = mutableListOf()
        }
        ui.setState {
            list.data.addAll(res.itemsList)
            _maxId = res.cursor.max
            _mapTp = res.cursor.maxTp
            if (!res.hasMore) {
                list.finished = true
            }
        }
    }

    private suspend fun searchList(
        _keyword: String,
        pageNum: Long,
    ) {
        val req = HistoryOuterClass.SearchReq.newBuilder().apply {
            business = "archive"
            keyword = _keyword
            pn = pageNum
        }.build()
        val res = HistoryGrpc.getSearchMethod()
            .request(req)
            .awaitCall()
        if (pageNum == 1L){
            list.data = mutableListOf()
        }
        ui.setState {
            list.data.addAll(res.itemsList)
            _maxId = res.page.pn
            if (!res.hasMore) {
                list.finished = true
            }
        }
    }

    private fun _loadData() {
        loadData()
    }

    fun loadMode() {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(_maxId)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            _viewAt = 0
            _maxId = 0
            loadData(0L)
        }
    }
}