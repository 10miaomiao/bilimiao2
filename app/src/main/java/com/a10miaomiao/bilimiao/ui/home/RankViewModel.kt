package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.entity.ResultListInfo
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RankViewModel : ViewModel() {

    var loading = MiaoLiveData(false)
    var list = MiaoList<BiliMiaoRank>()

    init {
        loadData()
    }

    fun loadData() {
        loading set true
        MiaoHttp.getJson<ResultListInfo<BiliMiaoRank>>("https://10miaomiao.cn/miao/bilimiao/rank/list")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    if (data.code == 0) {
                        list.clear()
                        list.addAll(data.data)
                    } else {

                    }
                }, { err ->
                    err.printStackTrace()
                }, {
                    loading set false
                })
    }

    fun refreshList(){
        loadData()
    }

}