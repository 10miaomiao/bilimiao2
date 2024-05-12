package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.Cursor
import bilibili.app.interfaces.v1.CursorItem
import bilibili.app.interfaces.v1.CursorV2Req
import bilibili.app.interfaces.v1.HistoryGRPC
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    //    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text) }
    var keyword = ""

    var triggered = false
    var list = PaginationInfo<CursorItem>()

    private var _mapTp = 3
    private var _maxId = 0L
    private var _viewAt = 0L

    init {
        keyword = fragment.requireArguments().getString(MainNavArgs.text, "")
        loadData(0L)
    }

    private fun loadData(
        maxId: Long = _maxId
    ) = viewModelScope.launch(Dispatchers.IO) {
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
        val req = CursorV2Req(
            business = "archive",
            cursor = if (maxId != 0L) {
                Cursor(
                    max = maxId,
                    maxTp = _mapTp, // 本页最大值游标类型
                )
            } else {
                Cursor()
            }
        )
        val res = BiliGRPCHttp.request {
            HistoryGRPC.cursorV2(req)
        }.awaitCall()
        if (maxId == 0L) {
            list.data = mutableListOf()
        }
        ui.setState {
            list.data.addAll(res.items)
            res.cursor?.let {
                _maxId = it.max
                _mapTp = it.maxTp
            }
            if (!res.hasMore) {
                list.finished = true
            }
        }
    }

    private suspend fun searchList(
        _keyword: String,
        pageNum: Long,
    ) {
        val req = bilibili.app.interfaces.v1.SearchReq(
            business = "archive",
            keyword = _keyword,
            pn = pageNum,
        )
        val res = BiliGRPCHttp.request {
            HistoryGRPC.search(req)
        }.awaitCall()
        if (pageNum == 1L) {
            list.data = mutableListOf()
        }
        ui.setState {
            list.data.addAll(res.items)
            _maxId = res.page?.pn ?: 0
            if (!res.hasMore) {
                list.finished = true
            }
        }
    }

    fun deleteHistory(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val item = list.data[position]
        try {
            val req = bilibili.app.interfaces.v1.DeleteReq(
                hisInfo = bilibili.app.interfaces.v1.HisInfo(
                    business = item.business,
                    kid = item.kid,
                )
            )
            BiliGRPCHttp.request {
                HistoryGRPC.delete(req)
            }.awaitCall()
            withContext(Dispatchers.Main) {
                ui.setState {
                    list.data.removeAt(position)
                }
                PopTip.show("删除成功")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("删除失败:$e")
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