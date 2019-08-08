package com.a10miaomiao.bilimiao.ui.user

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.ResultListInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast

class UserViewModel(
        val context: Context
) : ViewModel() {

    val loading = MiaoLiveData(false)
    val list = MiaoList<MediaListInfo>()

    init {
        loadData()
    }

    fun loadData() {
        loading set true
        list.clear()
        val url = BiliApiService.gatMedialist()
        MiaoHttp.getJson<ResultListInfo<DataInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    if (it.code == 0) {
                        val mediaList = it.data[0].mediaListResponse.list
                        list.addAll(mediaList)
                    } else {
                        context.toast(it.msg)
                    }
                }, { e ->
                    e.printStackTrace()
                    context.toast("网络错误")
                }, {
                    loading set false
                })
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