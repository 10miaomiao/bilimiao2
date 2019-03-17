package com.a10miaomiao.bilimiao.ui.upper

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.SubmitVideo
import com.a10miaomiao.bilimiao.entity.SubmitVideos
import com.a10miaomiao.bilimiao.entity.UpperChannel
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UpperChannelVideoListViewModel(var channel: UpperChannel) : ViewModel() {
    val list = MiaoList<VideoArchives>()
    val loading = MutableLiveData<Boolean>()
    val loadState = MutableLiveData<LoadMoreView.State>()
    var pageNum = 1
    val pageSize = 10

    init {
        loadData()
        loading.value = true
        loadState.value = LoadMoreView.State.LOADING
    }

    fun loadData() {
        loading.value = true
        val url = BiliApiService.getUpperChanneVideo(channel.mid, channel.cid, pageNum, pageSize)
        MiaoHttp.getJson<ResultInfo<ChannelVideoData>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    loading.value = false
                    val archive = r.data.list.archives
                    list.addAll(archive)
                    if (archive.size < pageSize) {
                        loadState.value = LoadMoreView.State.NOMORE
                    }
                }, { e ->
                    loading.value = false
                    loadState.value = LoadMoreView.State.FAIL
                })
    }

    fun refreshList() {
        pageNum = 1
        list.clear()
        loadState.value = LoadMoreView.State.LOADING
        loadData()
    }


    data class ChannelVideoData(
            var list: ChannelVideoList
    )
    data class ChannelVideoList(
            var archives: List<VideoArchives>
    )
    data class VideoArchives(
            var aid: String,
            var duration: Int,
            var title: String,
            var pic: String,
            var pubdate: Long,
            var stat: VideoStat
    )
    data class VideoStat(
            var danmaku: Int,
            var view: Int
    )

}