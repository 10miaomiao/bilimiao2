package com.a10miaomiao.bilimiao.ui.video

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.binding.MiaoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.json.JSONTokener
import java.text.DateFormat
import java.util.*

class VideoInfoViewModel(var id: String) : MiaoViewModel() {

    private val _info = MutableLiveData<PageInfo>()
    val info: LiveData<PageInfo> get() = _info
    val relates = MiaoList<Relate>()
    val pages = MiaoList<Page>()
    var pageIndex = MutableLiveData<Int>()

    val state = MutableLiveData<String>()

    init {
        pageIndex.value = 0
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
                        _info.value = PageInfo(data)
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

    fun clear() {
        pageIndex.value = 0
        _info.value = null
        relates.clear()
        pages.clear()
    }

    data class PageInfo(
            var cid: String,
            var title: String,
            var pic: String,
            var owner: Owner,
            var owner_ext: OwnerExt,
            var stat: Stat,
            var pubdate: Long,
            var desc: String
    ) {

        constructor(info: VideoInfo) : this(
                info.cid.toString(), info.title, info.pic,
                info.owner, info.owner_ext,
                info.stat, info.pubdate, info.desc
        )
    }


}
