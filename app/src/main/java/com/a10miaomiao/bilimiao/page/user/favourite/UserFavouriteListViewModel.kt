package com.a10miaomiao.bilimiao.page.user.favourite

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.UserStore
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

    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }

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
            val req = if (userStore.isSelf(id)) {
                getSelfMedialist(pageNum)
            } else {
                getOtherMedialist(pageNum)
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

    private suspend fun getOtherMedialist(pageNum: Int) {
        // TODO: 更新新版本的API
        val res = BiliApiService.userApi.medialist(id).awaitCall()
            .gson<ResultInfo<DataInfo>>()
        if (res.code == 0) {
            val result = res.data.favorite.items.map {
                val cover = if (it.cover.isEmpty()) {
                    ""
                } else {
                    it.cover[0].pic
                }
                MediaListInfo(
                    cover = cover,
                    intro = "${it.cur_count}个视频 ",
                    title = it.name,
                    cover_type = 0,
                    ctime = 0,
                    fav_state = 0,
                    fid = it.fid,
                    id = it.media_id,
                    like_state = 0,
                    media_count = it.cur_count,
                    mid = it.mid,
                    mtime = 0,
                    state = it.state,
                    type = 0
                )
            }
            ui.setState {
                list.finished = true
                list.data.addAll(result)
            }
            list.pageNum = pageNum
        } else {
            context.toast(res.message)
            throw Exception(res.message)
        }
    }

    private suspend fun getSelfMedialist (pageNum: Int){
        val res = BiliApiService.userApi.medialist().awaitCall()
            .gson<ResultInfo<List<MediaInfo>>>()
        if (res.code == 0) {
            val result = res.data[0].mediaListResponse.list
            ui.setState {
                list.finished = true
                list.data.addAll(result)
            }
            list.pageNum = pageNum
        } else {
            context.toast(res.message)
            throw Exception(res.message)
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