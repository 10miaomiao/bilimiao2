package com.a10miaomiao.bilimiao.page.user.favourite

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.entity.video.SubmitVideosInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserFavouriteDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }

    var triggered = false
    var list = PaginationInfo<MediasInfo>()

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
            val res = BiliApiService.userApi
                .mediaDetail(
                    media_id = id,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                )
                .awaitCall()
                .gson<ResultInfo<MediaDetailInfo>>()
            if (res.code == 0) {
                val result = res.data.medias
                if (result.size < list.pageSize) {
                    ui.setState { list.finished = true }
                }
                ui.setState {
                    if (pageNum == 1) {
                        list.data = arrayListOf()
                    }
                    list.data.addAll(result)
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
}