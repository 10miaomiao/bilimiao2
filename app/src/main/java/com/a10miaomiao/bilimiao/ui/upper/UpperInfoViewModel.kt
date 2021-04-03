package com.a10miaomiao.bilimiao.ui.upper

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UpperInfoViewModel(val owner: Owner) : ViewModel() {

    var list = MiaoList<UpperChannel>()
    val noLike = MutableLiveData<Boolean>() // 不喜欢，是否屏蔽
    val loading = MutableLiveData<Int>()
    val loadState = MutableLiveData<LoadMoreView.State>()

    init {
        loadData()
        loadState.value = LoadMoreView.State.LOADING
        noLike.value = false
    }

    private fun loadData() {
        loading.value = 0
        loadSubmitData()
        loadChannelData()
    }

    /**
    * 加载投稿视频封面
    */
    private fun loadSubmitData() {
        MiaoHttp.getJson<ResultInfo<SubmitVideos>>(BiliApiService.getUpperVideo(owner.mid, 1, 1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    loading.value = loading.value!! + 1
                    val data = res.data
                    if (res.data.list.vlist.isNotEmpty()) {
                        list.add(0, UpperChannel(
                                cid = 0,
                                mid = 0,
                                name = "全部投稿",
                                count = 0,
                                cover = res.data.list.vlist[0].pic,
//                                archives = ArrayList<UpperArchives>(),
                                mtime = 0,
                                intro = "全部投稿",
                                isAll = true
                        ))
                    }
                }, { err ->
                    loading.value = loading.value!! + 1
                    err.printStackTrace()
                })
    }

    /**
     * 加载频道数据
     */
    private fun loadChannelData() {
        MiaoHttp.getJson<ResultListInfo<UpperChannel>>(BiliApiService.getUpperChanne(owner.mid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    loading.value = loading.value!! + 1
//                    loading = false
                    list.addAll(res.data)
                }, { err ->
                    loading.value = loading.value!! + 1
                    err.printStackTrace()
                })
    }


    /**
     * 清除列表
     */
    fun refreshList() {
        list.clear()
        loadData()
    }

}