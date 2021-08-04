package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ListCount
import com.a10miaomiao.bilimiao.entity.MediaListInfo
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FavViewModel(
        val context: Context,
        val mid: Long
) : ViewModel() {

    val list = MiaoList<MediaListInfo>()
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
        val userStore = Store.from(context).userStore
        val url = if (userStore.isSelf(mid)) BiliApiService.gatMedialist()
            else  BiliApiService.gatMedialist(mid)
        loading set true
        MiaoHttp.getJson<ResultInfo<List<DataInfo>>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.code == 0) {
                        list.addAll(it.data[0].mediaListResponse.list)
                        if (it.data[0].mediaListResponse.list.size < pageSize) {
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
            var id: Int,
            var mediaListResponse: ListCount<MediaListInfo>,
            var name: String
    )

}