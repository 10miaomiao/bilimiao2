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


class UserBangumiViewModel(
        val context: Context,
        val vmid: Long
) : ViewModel() {

    val list = MiaoList<ItemInfo>()
    val loading = MiaoLiveData(false)
    val loadState = MiaoLiveData(LoadMoreView.State.LOADING)
    var pn = 1
    private val ps = 20

    init {
        loadData()
    }

    fun loadData() {
        val url = BiliApiService.getFollowBangumi(vmid, pn, ps)
        DebugMiao.log(url)
        loading set true
        MiaoHttp.getJson<ResultInfo<DataInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val (code, data) = it
                    if (code == 0) {
                        DebugMiao.log(data)
                        list.addAll(data.item)
                        if (data.item.size < ps) {
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
        val count: Int,
        val item: List<ItemInfo>
    )

    data class ItemInfo(
            val attention: String,
            val cover: String,
            val finish: Int,
            val goto: String,
            val index: String,
            val is_finish: String,
            val is_started: Int,
            val mtime: Int,
            val newest_ep_id: String,
            val newest_ep_index: String,
            val `param`: String,
            val title: String,
            val total_count: String,
            val uri: String
    )

}