package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
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

class UserChannelDetailViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val mid by lazy { fragment.requireArguments().getString(MainNavArgs.parent, "") }
    val cid by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    val name by lazy { fragment.requireArguments().getString(MainNavArgs.name, "") }

    var triggered = false
    var list = PaginationInfo<VideoArchives>()

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
                .upperChanneVideo(
                    mid = mid,
                    cid = cid,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                )
                .awaitCall()
                .gson<ResultInfo<ChannelVideoData>>()
            if (res.code == 0) {
                val result = res.data.list.archives
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

    data class ChannelVideoData(
        var list: ChannelVideoList
    )
    data class ChannelVideoList(
        var archives: List<VideoArchives>
    )
    data class VideoArchives(
        var aid: String,
        var duration: Int,
        var title: String,
        var pic: String,
        var pubdate: Long,
        var stat: VideoStat
    )
    data class VideoStat(
        var danmaku: String,
        var view: String
    )

}