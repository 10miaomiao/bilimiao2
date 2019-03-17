package com.a10miaomiao.bilimiao.ui.video

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.a10miaomiao.bilimiao.entity.Page
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
    val pages = MiaoList<Page>()

    val state = MutableLiveData<String>()

    init {
        loadData()
    }

    fun loadData() {
        state.value = null
        val url = BiliApiService.getVideoInfo(id)
        MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    if (r.code == 0) {
                        val data = r.data
                        _info.value = data
                        relates.clear()
                        relates.addAll(data.relates)
                        pages.clear()
                        pages.addAll(data.pages)
                    } else if (r.code == -403) {
                        state.value = "绝对领域，拒绝访问＞﹏＜"
                    } else {
                        state.value = r.message
                    }
                }, { err ->
                    state.value = "网络错误"
                    err.printStackTrace()
                })
    }

    fun playVideo() {

    }


}
