package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import com.a10miaomiao.bilimiao.entity.Page
import com.a10miaomiao.bilimiao.entity.Relate
import com.a10miaomiao.bilimiao.entity.ResultInfo
import com.a10miaomiao.bilimiao.entity.VideoInfo
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.toast
import java.net.URL


class VideoInfoViewModel(
        val context: Context,
        val type: String,
        var id: String
) : ViewModel() {
    val info = MiaoLiveData<VideoInfo?>(null)
    val relates = MiaoList<Relate>()
    val pages = MiaoList<Page>()

    var loading = MiaoLiveData<Boolean>(false)
    val loadState = MiaoLiveData<LoadMoreView.State>(LoadMoreView.State.LOADING)

    val state = MutableLiveData<String>()

    private var loadDataDisposable: Disposable? = null

    init {
        loadData()
    }

    fun loadData() {
        state.value = null
        val url = if (type == "AV") {
            BiliApiService.getVideoInfo(id)
        }else{
            BiliApiService.getVideoInfoByBvid(id)
        }
        val filterStore = Store.from(context).filterStore
        loading set true
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    if (r.code == 0) {
                        val data = r.data
                        data.desc = BiliUrlMatcher.customString(data.desc)
                        info set data
                        relates.clear()
                        relates.addAll(
                                data.relates.filter {
                                    filterStore.filterWord(it.title)
                                            && it.owner != null
                                            && filterStore.filterUpper(it.owner.mid)
                                }
                        )
                        pages.clear()
                        data.pages.forEach {
                            it.part = if (it.part.isNotEmpty()) it.part
                            else data.title
                        }
                        pages.addAll(data.pages)
                    } else if (r.code == -403) {
                        state.value = "绝对领域，拒绝访问＞﹏＜"
                    } else {
                        state.value = r.message
                    }
                    loading set false
                }, { err ->
                    state.value = "网络错误"
                    err.printStackTrace()
                    loading set false
                })
    }

    fun clear() {
        info set null
        relates.clear()
        pages.clear()
    }

    /**
     * 分享按钮点击事件
     */
    val columnShareClick = View.OnClickListener {
        val videoInfo = info.value
        if (videoInfo == null) {
            context.toast("视频信息未加载完成，请稍后再试")
            return@OnClickListener
        }
        var shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "bilibili视频分享")
            putExtra(Intent.EXTRA_TEXT, "${videoInfo.title} https://www.bilibili.com/video/${videoInfo.bvid}")
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享"))
    }

    /**
     * 点赞按钮点击事件
     */
    val columnLikeClick = View.OnClickListener {
        val url = "https://app.bilibili.com/x/v2/view/like"
        val params = ApiHelper.createParams(
                "aid" to id,
                "dislike" to "0",
                "from" to "7",
                "like" to "0"
        )
        DebugMiao.log(url)
        MiaoHttp.postString(url) {
            body = RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded"),
                    ApiHelper.urlencode(params)
            )
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    DebugMiao.log(r)
                }, { e ->
                    DebugMiao.log(e)
                })

    }

    /**
     * 评论按钮点击事件
     */
    val columnCommClick = View.OnClickListener {
        val videoInfo = info.value
        if (videoInfo == null) {
            context.toast("视频信息未加载完成，请稍后再试")
            return@OnClickListener
        }
        MainActivity.of(context).start(VideoCommentFragment.newInstance(videoInfo.aid.toString()))
    }


    override fun onCleared() {
        super.onCleared()
        loadDataDisposable?.dispose()
    }

    fun toLink(link: String){
        val urlInfo = BiliUrlMatcher.findIDByUrl(link)
        val urlType = urlInfo[0]
        val urlId = urlInfo[1]
        when(urlType){
            "AV" -> MainActivity.of(context).start(VideoInfoFragment.newInstance(urlInfo[1]))
            "BV" -> MainActivity.of(context).start(VideoInfoFragment.newInstanceByBvid(urlInfo[1]))
            else -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(link)
                context.startActivity(intent)
            }
        }
    }

}
