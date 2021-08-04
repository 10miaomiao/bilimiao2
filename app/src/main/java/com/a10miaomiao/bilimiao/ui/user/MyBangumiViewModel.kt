package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo2
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyBangumiViewModel(
        val context: Context,
        val vmid: Long
) : ViewModel() {
    val list = MiaoList<DataInfo>()
    val loading = MiaoLiveData(false)
    val loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    var pn = 1
    private val ps = 20

    init {
        loadData()
    }

    fun loadData() {
        if (-loading) {
            return
        }
        val url = BiliApiService.getFollowBangumi(pn, ps)
        loading set true
        MiaoHttp.getJson<ResultInfo2<List<DataInfo>>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.code == 0) {
                        DebugMiao.log(it.result)
                        list.addAll(it.result)
                        if (it.result.size < ps) {
                            loadState set LoadMoreView.State.NOMORE
                        }
                    } else {
                        loadState set LoadMoreView.State.FAIL
                    }
                }, {
                    it.printStackTrace()
                    loadState set LoadMoreView.State.FAIL
                }, {
                    loading set false
                })
    }

    fun refreshList() {
        list.clear()
        pn = 1
        loadData()
    }

    data class DataInfo(
            val badge: String,
            val badge_type: Int,
            val can_watch: Int,
            val cover: String,
            val follow: Int,
            val is_finish: Int,
            val movable: Int,
            val mtime: Int,
            val new_ep: NewEp,
            val progress: Progress?,
            val season_id: Int,
            val season_type: Int,
            val season_type_name: String,
            val series: Series,
            val square_cover: String,
            val title: String,
            val url: String
    )

    data class Progress(
            val index_show: String,
            val last_ep_id: Int,
            val last_time: Int
    )

    data class NewEp(
            val cover: String,
            val duration: Int,
            val id: Int,
            val index_show: String,
            val is_new: Int
    )

    data class Series(
            val count: Int,
            val id: Int,
            val title: String
    )

}