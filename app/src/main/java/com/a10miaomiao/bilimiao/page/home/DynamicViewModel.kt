package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicGRPC
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class DynamicViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    private var _offset = ""
    private var _baseline = ""
    var list = PaginationInfo<DataInfo>()
    var triggered = false

    init {
        loadData("")
    }


    private fun loadData(
        offset: String = _offset
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val type = if (offset.isBlank()) {
                bilibili.app.dynamic.v2.Refresh.NEW
            } else {
                bilibili.app.dynamic.v2.Refresh.HISTORY
            }
            val req = bilibili.app.dynamic.v2.DynVideoReq(
                refreshType = type,
                localTime = 8,
                offset = offset,
                updateBaseline = _baseline,
            )
            val result = BiliGRPCHttp.request {
                DynamicGRPC.dynVideo(req)
            }.awaitCall()
            val dynamicList = result.dynamicList
            if (dynamicList != null) {
                _offset = dynamicList.historyOffset
                _baseline = dynamicList.updateBaseline
                ui.setState {
                    val itemsList = dynamicList.list.filter { item ->
                        item.cardType != bilibili.app.dynamic.v2.DynamicType.DYN_NONE
                                && item.cardType != bilibili.app.dynamic.v2.DynamicType.AD
                    }.map { item ->
                        val modules = item.modules
                        val userModule = modules.first { it.moduleAuthor != null }.moduleAuthor!!
                        val descModule = modules.find { it.moduleDesc != null }?.moduleDesc
                        val dynamicModule = modules.first { it.moduleDynamic != null }.moduleDynamic!!
                        val statModule = modules.first { it.moduleStat != null }.moduleStat!!
                        val author = userModule.author!!
                        DataInfo(
                            mid = author.mid.toString(),
                            name = author.name,
                            face = author.face,
                            labelText = userModule.ptimeLabelText,
                            dynamicType = dynamicModule.type.value,
                            like = statModule.like,
                            reply = statModule.reply,
                            dynamicContent = getDynamicContent(dynamicModule),
                        )
                    }
                    if (offset.isBlank()) {
                        list.data = itemsList.toMutableList()
                    } else {
                        list.data.addAll(itemsList)
                    }
                }
            } else {
                ui.setState {
                    list.data = mutableListOf()
                    list.finished = true
                }
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


    fun tryAgainLoadData() {
        val (loading, finished) = this.list
        if (!finished && !loading) {
            loadData()
        }
    }

    fun loadMode() {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(_offset)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData("")
        }
    }

    private fun getDynamicContent(dynamicModule: bilibili.app.dynamic.v2.ModuleDynamic): DynamicContentInfo {
        return dynamicModule.dynArchive?.let {
            DynamicContentInfo(
                id = it.avid.toString(),
                title = it.title,
                pic = it.cover,
                remark = it.coverLeftText2 + "    " + it.coverLeftText3,
                duration = it.coverLeftText1,
            )
        } ?: dynamicModule.dynPgc?.let {
            DynamicContentInfo(
                id = it.seasonId.toString(),
                title = it.title,
                pic = it.cover,
                remark = it.coverLeftText2 + "    " + it.coverLeftText3,
            )
        } ?: DynamicContentInfo("")
    }

    data class DataInfo(
        val mid: String,
        val name: String,
        val face: String,
        val labelText: String,
        val like: Long,
        val reply: Long,
        val dynamicType: Int,
        val dynamicContent: DynamicContentInfo,
    )

    data class DynamicContentInfo(
        val id: String,
        val title: String = "",
        val pic: String = "",
        val remark: String? = null,
        val duration: String? = null,
    )
}
