package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
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
        val userStore = MainActivity.of(context).userStore
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
            var mediaListResponse: MediaInfo,
            var name: String
    )

    data class MediaInfo(
            var count: Int,
            var list: List<MediaListInfo>
    )

    /**
     * "cover": "",
    "cover_type": 0,
    "ctime": 1564047236,
    "fav_state": 0,
    "fid": 4984235,
    "id": 498423543,
    "intro": "",
    "like_state": 0,
    "media_count": 0,
    "mid": 384046343,
    "mtime": 1564047236,
    "state": 0,
    "title": "默认收藏夹",
    "type": 11,
     */
    data class MediaListInfo(
            var cover: String,
            var intro: String,
            var title: String,
            var cover_type: Int,
            var ctime: Long,
            var fav_state: Int,
            var fid: Long,
            var id: Long,
            var like_state: Int,
            var media_count: Int,
            var mid: Long,
            var mtime: Long,
            var state: Int,
            var type: Int
    )

}