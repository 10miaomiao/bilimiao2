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

class VideoInfoViewModel(val id: String) : MiaoViewModel() {

    private val _info = MutableLiveData<PageInfo>()
    val info: LiveData<PageInfo> get() = _info
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
                        _info.value = PageInfo(data)
                        relates.clear()
                        relates.addAll(data.relates)
                        pages.clear()
                        pages.addAll(data.pages)
                    } else if (r.code == -403) {
                        state.value = "绝对领域，拒绝访问＞﹏＜"
                    } else if (r.code == -404) {
                        // 有可能是番剧
                        loadEpData()
                    } else {
                        state.value = r.message
                    }
                }, { err ->
                    state.value = "网络错误"
                    err.printStackTrace()
                })
    }

    private fun loadEpData() {
        val http = MiaoHttp.getString("https://www.bilibili.com/video/av$id/") {
            headers = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36"
            )
        }
        http.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ html ->
                    var a = "window.__INITIAL_STATE__={"
                    var n = html.indexOf(a)
                    var m = html.indexOf("};", n + a.length)
                    var json = html.substring(n + a.length - 1, m + 1)
                    DebugMiao.log(json)
                    val jsonParser = JSONTokener(json)
                    val jsonObject = (jsonParser.nextValue() as JSONObject)
                    val epInfo = jsonObject.getJSONObject("epInfo")
                    val mediaInfo = jsonObject.getJSONObject("mediaInfo")
//                    val stat = mediaInfo.getJSONObject("stat")
                    val uper = mediaInfo.getJSONObject("upInfo")
                    _info.value = PageInfo(
                            cid = epInfo.getString("cid"),
                            title = jsonObject.getString("h1Title"),
                            pic = epInfo.getString("cover"),
                            owner = Owner(
                                    uper.getString("avatar"),
                                    uper.getInt("mid"),
                                    uper.getString("name")
                            ),
                            owner_ext = OwnerExt(0),
                            stat = Stat(
//                                    stat.getString("danmakus"),
//                                    stat.getString("views")
                                    "", ""
                            ),
                            pubdate = 0L,
                            desc = mediaInfo.getString("evaluate")
                    )
                }, { err ->
                    state.value = "网络错误"
                    err.printStackTrace()
                })
    }


    fun playVideo() {

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
