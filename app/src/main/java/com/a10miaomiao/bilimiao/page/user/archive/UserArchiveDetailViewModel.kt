package com.a10miaomiao.bilimiao.page.user.archive

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserArchiveDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

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
        loadData("")
    }

    private fun loadData(
        aid: String = _aid
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi
                .upperVideoList(
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
        loadData()
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(
                _aid
            )
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData("")
        }
    }

}