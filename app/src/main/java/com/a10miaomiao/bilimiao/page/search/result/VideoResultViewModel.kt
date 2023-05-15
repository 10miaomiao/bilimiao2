package com.a10miaomiao.bilimiao.page.search.result

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo2
import com.a10miaomiao.bilimiao.comm.entity.article.ArticleInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchResultInfo
import com.a10miaomiao.bilimiao.comm.entity.search.SearchVideoInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class VideoResultViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val filterStore: FilterStore by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text, "") }

    var list = PaginationInfo<SearchVideoInfo>()
    var triggered = false


    val rankOrdersMenus = listOf<CheckPopupMenu.MenuItemInfo<String>>(
        CheckPopupMenu.MenuItemInfo("默认排序", "default"),
        CheckPopupMenu.MenuItemInfo("相关度", "ranklevel"),
        CheckPopupMenu.MenuItemInfo("新发布", "pubdate"),
        CheckPopupMenu.MenuItemInfo("播放多", "click"),
        CheckPopupMenu.MenuItemInfo("弹幕多", "dm"),
        CheckPopupMenu.MenuItemInfo("评论多", "scores"),
        CheckPopupMenu.MenuItemInfo("收藏多", "stow"),
    )
    var rankOrder = rankOrdersMenus[0]

    val durationMenus = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
        CheckPopupMenu.MenuItemInfo("全部时长", 0),
        CheckPopupMenu.MenuItemInfo("0-10分钟", 1),
        CheckPopupMenu.MenuItemInfo("10-30分钟", 2),
        CheckPopupMenu.MenuItemInfo("30-60分钟", 3),
        CheckPopupMenu.MenuItemInfo("60分钟+", 4),
    )
    var duration = durationMenus[0]

    var regionId = 0
    var regionName = "全部分区"

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }

            val res = BiliApiService.searchApi
                .searchArchive(
                    keyword = keyword,
                    order = rankOrder.value,
                    duration = duration.value,
                    rid = regionId,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                )
                .awaitCall()
                .gson<ResultInfo<SearchResultInfo<SearchArchiveInfo>>>()
            if (res.code == 0) {
                var result = res.data.items.archive
                if (result == null) {
                    ui.setState {
                        list.finished = true
                    }
                } else {
                    var totalCount = result.size // 屏蔽前数量
                    result = result.filter {
                        filterStore.filterWord(it.title)
                                && filterStore.filterUpper(it.mid.toLong())
                    }
                    ui.setState {
                        list.finished = totalCount == 0
                        if (pageNum == 1) {
                            list.data = arrayListOf()
                        }
                        list.data.addAll(result)
                    }
                }
                list.pageNum = pageNum
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


    private fun _loadData(pageNum: Int = list.pageNum) {
        loadData(pageNum)
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(
                pageNum = pageNum + 1
            )
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData()
        }
    }


}