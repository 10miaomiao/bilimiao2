package com.a10miaomiao.bilimiao.ui.cover

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import kotlin.math.acos

class CoverViewModel(
        private val activity: Activity,
        private var type: String,
        private var id: String
) : ViewModel() {

    var coverBitmap = MutableLiveData<Bitmap>()
    var title = MutableLiveData<String>()
    var loading = MutableLiveData<Boolean>()

    private var loadDataDisposable: Disposable? = null

    init {
        loading.value = true
        loadData()
    }

    fun setConfig(type: String, id: String){
        this.type = type
        this.id = id
        this.loadData()
    }

    private fun loadData() {
        when (type) {
            "AV" -> loadAvData()
            "BV" -> loadBVData()
            "SS" -> loadSsData()
            "EP" -> loadEpData()
            "ROOM" -> loadRoomData()
            "CV" -> loadCvData()
            "AU" -> loadAuData()
        }
    }

    // 普通视频
    private fun loadAvData() {
        val url = BiliApiService.getVideoInfo(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    if (res.code == 0) {
                        val data = res.data
                        title.value = data.title
                        loadCover(data.pic)
                    } else {

                    }
                }, { err ->
                    err.printStackTrace()
                })
    }

    // 普通视频 BV
    private fun loadBVData() {
        val url = BiliApiService.getVideoInfoByBvid(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<VideoInfo>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    if (res.code == 0) {
                        val data = res.data
                        title.value = data.title
                        loadCover(data.pic)
                    } else {

                    }
                }, { err ->
                    err.printStackTrace()
                })
    }

    // 番剧
    private fun loadSsData() {

    }

    // 番剧剧集
    private fun loadEpData() {
        val url = BiliApiService.getSeasonEpisodeInfo(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo2<SeasonEpisode>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->
                    if (res.code == 0) {
                        val data = res.result

                        val ep = data.episodes.find { it.id == id }
                        if (ep == null) {
                            title.value = data.title
                            loadCover(data.cover)
                        } else {
                            title.value = ep.long_title
                            loadCover(ep.cover)
                        }
                    } else {

                    }
                }, { err ->
                    err.printStackTrace()
                })
    }

    // 直播间
    private fun loadRoomData() {
        val url = BiliApiService.getRoomInfo(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<Room>>(url)
                .flatMap{
                    // 曲线救国，获取直播间信息接口没有封面信息了，但个人页面有
                    // 所以先取得直播间up主的uid
                    val uid = it.data.uid
                    val url = BiliApiService.getSpace(uid.toString())
                    MiaoHttp.getJson<ResultInfo<SpaceInfo>>(url)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    val (code, data, msg) = r
                    val liveInfo = data.live
                    if (code == 0) {
                        title.value = liveInfo.title
                        loadCover(liveInfo.cover)
                    } else {
                        activity.toast(msg)
                    }
                }, { e ->
                    e.printStackTrace()
                })
        val url2 = BiliApiService.getRoomInfo(id)
    }

    // 专栏
    private fun loadCvData() {
        val url = BiliApiService.getCvInfo(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<Article>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    val data = r.data
                    title.value = data.title
                    loadCover(data.banner_url)
                }, { e ->
                    e.printStackTrace()
                })
    }

    // 音频
    private fun loadAuData() {
        val url = BiliApiService.getAudioInfo(id)
        loadDataDisposable?.dispose()
        loadDataDisposable = MiaoHttp.getJson<ResultInfo<Audio>>(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r ->
                    val data = r.data
                    title.value = data.title
                    loadCover(data.cover_url)
                }, { e ->
                    e.printStackTrace()
                })
    }

    private fun loadCover(pic: String) {
        var newPic = pic.replace("http://", "https://")
        Glide.with(activity)
                .load(newPic)
                .asBitmap()
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                        loading.value = false
                        coverBitmap.value = resource
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        loadDataDisposable?.dispose()
    }

}