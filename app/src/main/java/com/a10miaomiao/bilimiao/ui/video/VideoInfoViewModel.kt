package com.a10miaomiao.bilimiao.ui.video

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.a10miaomiao.bilimiao.entity.Relate
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.binding.MiaoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VideoInfoViewModel(val id: String) : MiaoViewModel() {

    private val _info = MutableLiveData<VideoInfo>()
    val info: LiveData<VideoInfo> get() = _info
    val relates = MiaoList<Relate>()


    init {
        loadData()
    }

    fun loadData() {
        val url = BiliApiService.getVideoInfo(id)
        MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    if (data.code == 0) {
                        _info.value = data.data
                        relates.clear()
                        relates.addAll(data.data.relates)
                    } else {

                    }
                }, { err ->
                    err.printStackTrace()
                })
    }

    fun playVideo() {

    }


}
