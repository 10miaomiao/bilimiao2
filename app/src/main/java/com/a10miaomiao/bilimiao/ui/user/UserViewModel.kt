package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.ResultListInfo
import com.a10miaomiao.bilimiao.entity.SpaceInfo
import com.a10miaomiao.bilimiao.entity.UpperChannel
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast

class UserViewModel(
        val context: Context,
        val vmid: Long
) : ViewModel() {

    val loading = MiaoLiveData(false)
    val dataInfo = MiaoLiveData<SpaceInfo?>(null)
    var channelList = MiaoLiveData<List<UpperChannel>>(listOf())

    val noLike = MiaoLiveData<Boolean>(false) // 不喜欢，是否屏蔽

    init {
        loadData()
    }

    fun loadData() {
        loading set true
        val url = BiliApiService.getSpace(vmid.toString())
        DebugMiao.log(url)
        MiaoHttp.getJson<ResultInfo<SpaceInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val (code, data, msg) = it
                    if (code == 0) {
                        dataInfo set data
                    } else {
                        context.toast(msg)
                    }
                }, { e ->
                    e.printStackTrace()
                    context.toast("网络错误")
                }, {
                    loading set false
                })

        MiaoHttp.getJson<ResultListInfo<UpperChannel>>(BiliApiService.getUpperChanne(vmid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    if (res.code == 0){
                        channelList set res.data
                    }
                }, { err ->
                    err.printStackTrace()
                })
    }

}