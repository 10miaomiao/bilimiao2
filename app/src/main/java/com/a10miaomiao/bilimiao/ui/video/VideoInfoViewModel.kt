package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoViewModel
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.FormBody

class VideoInfoViewModel(
        val context: Context,
        var id: String
) : ViewModel() {

    val info = MiaoLiveData<PageInfo?>(null)
    val relates = MiaoList<Relate>()
    val pages = MiaoList<Page>()
    var pageIndex = MiaoLiveData(0)

    val state = MutableLiveData<String>()

    init {
        loadData()
    }

    fun loadData() {
        state.value = null
        val url = BiliApiService.getVideoInfo(id)
        val filterStore = MainActivity.of(context).filterStore
        MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    if (r.code == 0) {
                        val data = r.data
                        info set PageInfo(data)
                        relates.clear()
                        relates.addAll(
                                data.relates.filter {
                                    filterStore.filterWord(it.title)
                                            && it.owner != null
                                            && filterStore.filterUpper(it.owner.mid)
                                }
                        )
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
        pageIndex set 0
        info set null
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
