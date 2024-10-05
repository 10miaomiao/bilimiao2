package cn.a10miaomiao.bilimiao.compose.pages.user

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.navigation.findComposeNavController
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.SeriesInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.SeriesListInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class UserArchiveViewModel(
    override val di: DI,
    private val vmid: String,
) : ViewModel(), DIAware {

    val fragment: Fragment by instance()
    //    var regionList = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
//        CheckPopupMenu.MenuItemInfo("全部(0)", 0),
//    )
//    var region = regionList[0]
//

    var rankOrder = MutableStateFlow("pubdate")

    val isRefreshing = MutableStateFlow(false)
    //    var total = 0
    val list = FlowPaginationInfo<ArchiveInfo>()
    private var lastAid: String = ""

    private val _seriesList = MutableStateFlow<List<SeriesInfo>>(listOf())
    val seriesList: StateFlow<List<SeriesInfo>> = _seriesList

    private val _seriesTotal = MutableStateFlow(0)
    val seriesTotal: StateFlow<Int> = _seriesTotal

    //
    init {
//        loadData("")
        loadSeriesList()
    }

    fun loadData(
        aid: String = lastAid
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            list.loading.value = true
            val res = BiliApiService.userApi
                .upperVideoList(
                    vmid = vmid,
//                    tid = region.value,
                    order = rankOrder.value,
                    aid = aid,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo<ArchiveCursorInfo>>()
            if (res.code == 0) {
                val items: List<ArchiveInfo> = res.data.item ?: emptyList()
                if (aid.isBlank()) {
                    list.data.value = items.toMutableList()
                } else {
                    list.data.value = mutableListOf<ArchiveInfo>().apply {
                        addAll(list.data.value)
                        addAll(items)
                    }
                }
                lastAid = items.lastOrNull()?.param ?: ""
//                if (region.value == 0) {
//                    total = res.data.count
//                }
                list.finished.value = !res.data.has_next
            } else {
                PopTip.show(res.message)
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished.value && !list.loading.value) {
            loadData(lastAid)
        }
    }

    fun refreshList() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
    }

    fun changeRankOrder(value: String) {
        rankOrder.value = value
        loadData("")
    }

    private fun loadSeriesList() = viewModelScope.launch(Dispatchers.IO){
        try {
            val res = BiliApiService.userApi.upperSeriesList(
                vmid,
                pageNum = 1,
                pageSize = 5,
            ).awaitCall().gson<ResultInfo<SeriesListInfo>>()
            if (res.code == 0) {
                val result = res.data
                _seriesList.value = result.items
                _seriesTotal.value = result.page.total
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun toSeriesList() {
        fragment.findComposeNavController()
            .navigate(UserMedialistPage()) {
                mid set vmid
            }
    }

    fun toSeriesDetail(item: SeriesInfo) {
        fragment.findComposeNavController()
            .navigate(UserMedialistPage()) {
                mid set vmid
                bizId set item.param
                bizType set item.type
                bizTitle set item.title
            }
    }

    fun toVideoDetail(item: ArchiveInfo) {
        fragment.findNavController()
            .navigate(
                Uri.parse("bilimiao://video/" + item.param),
                defaultNavOptions,
            )
    }

}