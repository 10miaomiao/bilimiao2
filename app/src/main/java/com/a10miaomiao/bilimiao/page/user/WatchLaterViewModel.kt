package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.HistoryGrpc
import bilibili.app.interfaces.v1.HistoryOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class WatchLaterViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    var list = PaginationInfo<VideoInfo>()
    var triggered = false

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi
                .videoHistoryToview(
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo<ListAndCountInfo<VideoInfo>>>()
            if (res.code == 0) {
                val listData = res.data.list
                if (listData.size < list.pageSize) {
                    ui.setState { list.finished = true }
                }
                ui.setState {
                    if (pageNum == 1) {
                        list.data = arrayListOf()
                    }
                    list.data.addAll(listData)
                }
                list.pageNum = pageNum
            } else {
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
            withContext(Dispatchers.Main) {
                PopTip.show(e.message ?: e.toString())
            }
        } finally {
            ui.setState {
                list.loading = false
                triggered = false
            }
        }
    }

    fun deleteVideoHistoryToview(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val item = list.data[position]
        try {
            val res = BiliApiService.userApi
                .videoHistoryToviewDel(item.aid)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.code == 0) {
                withContext(Dispatchers.Main) {
                    PopTip.show("已从稍后再看移出")
                }
                ui.setState {
                    list.data.removeAt(position)
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("失败:$e")
            }
        }
    }

    private fun _loadData() {
        loadData(1)
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(pageNum + 1)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
        }
        loadData(1)
    }
}