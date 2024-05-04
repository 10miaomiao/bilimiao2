package com.a10miaomiao.bilimiao.page.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.DynamicCommonOuterClass
import bilibili.app.dynamic.v2.DynamicGrpc
import bilibili.app.dynamic.v2.DynamicOuterClass
import bilibili.app.dynamic.v2.ModuleOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
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
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val type = if (offset.isBlank()) {
                DynamicCommonOuterClass.Refresh.refresh_new
            } else {
                DynamicCommonOuterClass.Refresh.refresh_history
            }
            val req = DynamicOuterClass.DynVideoReq.newBuilder()
                .setRefreshType(type)
                .setLocalTime(8)
                .setOffset(offset)
                .setUpdateBaseline(_baseline)
                .build()
            val result = DynamicGrpc.getDynVideoMethod()
                .request(req)
                .awaitCall()
            if (result.hasDynamicList()) {
                val dynamicListData = result.dynamicList
                _offset = dynamicListData.historyOffset
                _baseline = dynamicListData.updateBaseline
                ui.setState {
                    val itemsList = dynamicListData.listList.filter { item->
                        item.cardType != DynamicCommonOuterClass.DynamicType.dyn_none
                                && item.cardType != DynamicCommonOuterClass.DynamicType.ad
                    }.map { item ->
                        val modules = item.modulesList
                        val userModule = modules.first { it.hasModuleAuthor() }.moduleAuthor
                        val descModule = modules.find { it.hasModuleDesc() }?.moduleDesc
                        val dynamicModule = modules.first { it.hasModuleDynamic() }.moduleDynamic
                        val statModule = modules.first { it.hasModuleStat() }.moduleStat
                        DataInfo(
                            mid = userModule.author.mid.toString(),
                            name = userModule.author.name,
                            face = userModule.author.face,
                            labelText = userModule.ptimeLabelText,
                            dynamicType = dynamicModule.typeValue,
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

    fun loadMode () {
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

    private fun getDynamicContent(dynamicModule: ModuleOuterClass.ModuleDynamic): DynamicContentInfo {
        return when(dynamicModule.type) {
            ModuleOuterClass.ModuleDynamicType.mdl_dyn_archive -> {
                val dynArchive = dynamicModule.dynArchive
                DynamicContentInfo(
                    id = dynArchive.avid.toString(),
                    title = dynArchive.title,
                    pic = dynArchive.cover,
                    remark = dynArchive.coverLeftText2 + "    " + dynArchive.coverLeftText3,
                    duration = dynArchive.coverLeftText1,
                )
            }
            ModuleOuterClass.ModuleDynamicType.mdl_dyn_pgc -> {
                val dynPgc = dynamicModule.dynPgc
                DynamicContentInfo(
                    id = dynPgc.seasonId.toString(),
                    title = dynPgc.title,
                    pic = dynPgc.cover,
                    remark = dynPgc.coverLeftText2 + "    " + dynPgc.coverLeftText3,
                )
            }
            else -> DynamicContentInfo("")
        }
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
