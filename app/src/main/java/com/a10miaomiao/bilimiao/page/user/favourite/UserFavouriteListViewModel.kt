package com.a10miaomiao.bilimiao.page.user.favourite

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserSpaceFavFolderInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserFavouriteListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }

    var triggered = false
    var list = PaginationInfo<MediaListInfo>()

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
            val res = BiliApiService.userApi.favFolderList(
                id,
                pageNum = pageNum,
                pageSize = list.pageSize
            ).awaitCall().gson<ResultInfo<ListAndCountInfo<MediaListInfo>>>()
            if (res.code == 0) {
                val result = res.data
                ui.setState {
                    if (pageNum == 1) {
                        list.data = result.list.toMutableList()
                    } else {
                        list.data.addAll(result.list)
                    }
                    list.finished = !result.has_more
                }
                list.pageNum = pageNum
            } else {
                context.toast(res.message)
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

    data class DataInfo(
        val favorite: FavoriteInfo,
    )

    data class FavoriteInfo(
        val count: Int,
        val items: List<FavoriteItemInfo>,
    )

    data class FavoriteItemInfo(
        val media_id: String,
        val fid: Long,
        val mid: Long,
        val name: String,
        val cur_count: Int,
        val state: Int,
        val cover: List<CoverInfo>,
    )

    data class CoverInfo(
        val aid: String,
        val pic: String,
        val type: Int,
    )

}