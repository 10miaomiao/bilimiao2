package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.space.v1.SpaceGrpc
import bilibili.app.space.v1.SpaceOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo2
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.comm.entity.video.SubmitVideosInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserArchiveListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val myPage: MyPage by instance()
    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    val name by lazy { fragment.requireArguments().getString(MainNavArgs.name) }

    var regionList = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
        CheckPopupMenu.MenuItemInfo("全部(0)", 0),
    )
    var region = regionList[0]

    val rankOrderList = listOf<CheckPopupMenu.MenuItemInfo<String>>(
        CheckPopupMenu.MenuItemInfo("最新发布", "pubdate"),
        CheckPopupMenu.MenuItemInfo("最多播放", "click"),
//        CheckPopupMenu.MenuItemInfo("最多收藏", "stow"),
    )
    var rankOrder = rankOrderList[0]

    var triggered = false
    var total = 0
    var list = PaginationInfo<ArchiveInfo>()
    private var _aid: String = ""

    init {
//        loadData(1)
        loadData2("")
    }

//    private fun loadData(
//        pageNum: Int = list.pageNum
//    ) = viewModelScope.launch(Dispatchers.IO){
//        try {
//            ui.setState {
//                list.loading = true
//            }
//            val res = BiliApiService.userApi
//                .upperVideoList(
//                    mid = id,
//                    tid = region.value,
//                    order = rankOrder.value,
//                    pageNum = pageNum,
//                    pageSize = list.pageSize,
//                )
//                .awaitCall()
//                .gson<ResultInfo<SubmitVideosInfo>>()
//            if (res.code == 0) {
//                val vlist = res.data.list.vlist
//                if (vlist.size < list.pageSize) {
//                    ui.setState { list.finished = true }
//                }
//                ui.setState {
//                    if (pageNum == 1) {
//                        list.data = arrayListOf()
//                    }
//                    list.data.addAll(vlist)
//                }
//                if (region.value == 0) {
//                    total = res.data.page.count
//                }
//                res.data.list.tlist?.let {
//                    regionList = listOf(
//                        CheckPopupMenu.MenuItemInfo("全部(${total})", 0),
//                        *it.values.map {
//                            CheckPopupMenu.MenuItemInfo("${it.name}(${it.count})", it.tid)
//                        }.toTypedArray()
//                    )
//                    if (region.value == 0) {
//                        region = regionList[0]
//                    }
//                    withContext(Dispatchers.Main) {
//                        myPage.pageConfig.notifyConfigChanged()
//                    }
//                }
//                list.pageNum = pageNum
//            } else {
//                withContext(Dispatchers.Main) {
//                    context.toast(res.message)
//                }
//                throw Exception(res.message)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ui.setState {
//                list.fail = true
//            }
//        } finally {
//            ui.setState {
//                list.loading = false
//                triggered = false
//            }
//        }
//    }

    private fun loadData2(
        aid: String = _aid
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi
                .upperVideoList2(
                    vmid = id,
//                    tid = region.value,
                    order = rankOrder.value,
                    aid = aid,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .gson<ResultInfo<ArchiveCursorInfo>>()
            if (res.code == 0) {
                val items: List<ArchiveInfo>? = res.data.item
                ui.setState {
                    if (aid.isBlank()) {
                        list.data = mutableListOf()
                    }
                    items?.let {
                        list.data.addAll(it)
                        _aid = it.last().param
                    }
                }
                if (region.value == 0) {
                    total = res.data.count
                }
                list.finished = !res.data.has_next
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
                throw Exception(res.message)
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

    private fun _loadData() {
        loadData2()
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData2(
                _aid
            )
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData2("")
        }
    }

}