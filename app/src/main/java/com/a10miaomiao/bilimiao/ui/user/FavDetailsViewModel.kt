package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FavDetailsViewModel(
        val context: Context,
        val id: Long
) : ViewModel() {

    val list = MiaoList<MediasInfo>()
    val loading = MiaoLiveData(false)
    val loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    var pageNum = 1
    val pageSize = 20

    init {
        loadData()
    }

    fun loadData() {
        if (-loading) {
            return
        }
        val url = BiliApiService.gatMedialistDetail(
                id, pageNum, pageSize
        )
        loading set true
        MiaoHttp.getJson<ResultInfo<DataInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.code == 0) {
                        pageNum++
                        list.addAll(it.data.medias)
                        if (it.data.medias.size < pageSize) {
                            loadState set LoadMoreView.State.NOMORE
                        }
                    } else {
                        loadState set LoadMoreView.State.FAIL
                    }
                    DebugMiao.log(it)
                }, { e ->
                    e.printStackTrace()
                    loadState set LoadMoreView.State.FAIL
                }, {
                    loading set false
                })
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState set LoadMoreView.State.LOADING
        loadData()
    }


    data class DataInfo(
            val medias: List<MediasInfo>
    )

    data class MediasInfo(
            val id: Long,
            val cover: String,
            val ctime: Long,
            val title: String,
            val upper: UpperInfo,
            val cnt_info: CntInfo
    )

    data class UpperInfo(
            val face: String,
            val name: String,
            val mid: String
    )

    data class CntInfo(
            val coin: Int,
            val collect: Int,
            val danmaku: Int,
            val play: Int,
            val reply: Int,
            val share: Int,
            val thumb_down: Int,
            val thumb_up: Int
    )

}