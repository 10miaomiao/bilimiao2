package com.a10miaomiao.bilimiao.ui.home

import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.entity.ResultListInfo
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.binding.MiaoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RankViewModel : MiaoViewModel() {

    var loading by miao(false)

    var list = MiaoList<BiliMiaoRank>()

    init {
        loadData()
    }

    fun loadData() {
        loading = true
        MiaoHttp.getJson<ResultListInfo<BiliMiaoRank>>("https://10miaomiao.cn/miao/bilimiao/rank/list")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    loading = false
                    if (data.code == 0) {
                        list.clear()
                        list.addAll(data.data)
                    } else {

                    }
                }, { err ->
                    loading = false
                    err.printStackTrace()
                })
    }

    fun refreshList(){
        loadData()
    }

}