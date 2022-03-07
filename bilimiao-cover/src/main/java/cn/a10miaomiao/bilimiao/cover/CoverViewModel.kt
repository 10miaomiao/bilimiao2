package cn.a10miaomiao.bilimiao.cover

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.apis.ArticleAPI
import com.a10miaomiao.bilimiao.comm.apis.AudioAPI
import com.a10miaomiao.bilimiao.comm.apis.BangumiAPI
import com.a10miaomiao.bilimiao.comm.apis.VideoAPI
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.article.ArticleInfo
import com.a10miaomiao.bilimiao.comm.entity.audio.AudioInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonEpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoverViewModel(
    private val activity: Activity
) : ViewModel() {

    private var type: String = ""
    private var id: String = ""

    var coverBitmap = MutableLiveData<Bitmap>()
    var title = MutableLiveData<String>()
    var loading = MutableLiveData<Boolean>()
    var fileName = MutableLiveData<String>()


    init {

    }

    fun setConfig(type: String, id: String){
        this.type = type
        this.id = id
        this.fileName.value = type  + id
        loadData()
    }

    /**
     * 解析短链接出长链接，然后再正则匹配出类型和id
     */
    fun resolveUrl(url: String) = viewModelScope.launch(Dispatchers.IO){
        try {
            val res = MiaoHttp(url).get()
            val url = res.request().url().toString()
            val urlInfo = BiliUrlMatcher.findIDByUrl(url)
            type = urlInfo[0].toUpperCase()
            id = urlInfo[1]
            withContext(Dispatchers.Main) {
                setConfig(type, id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                loading.value = true
            }
            when (type) {
                "AV" -> loadAvData()
                "BV" -> loadBVData()
                "SS" -> loadSsData()
                "EP" -> loadEpData()
                "ROOM" -> loadRoomData()
                "CV" -> loadCvData()
                "AU" -> loadAuData()
                else -> {
                    withContext(Dispatchers.Main) {
                        loading.value = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }
    }

    // 普通视频
    private suspend fun loadAvData() {
        val res = VideoAPI().info(id).call().gson<ResultInfo<VideoInfo>>()
        if (res.code == 0) {
            val data = res.data
            withContext(Dispatchers.Main) {
                title.value = data.title
                loadCover(data.pic)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    // 普通视频 BV
    private suspend fun loadBVData() {
        val res = VideoAPI().info(id, type = "BV").call().gson<ResultInfo<VideoInfo>>()
        if (res.code == 0) {
            val data = res.data
            withContext(Dispatchers.Main) {
                title.value = data.title
                loadCover(data.pic)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    // 番剧
    private fun loadSsData() {

    }

    // 番剧剧集
    private suspend fun loadEpData() {
        val res = BangumiAPI().episodeInfo(id).call().gson<ResultInfo2<SeasonEpisodeInfo>>()
        if (res.code == 0) {
            val data = res.result
            val ep = data.episodes.find { it.id == id }
            withContext(Dispatchers.Main) {
                if (ep == null) {
                    title.value = data.title
                    loadCover(data.cover)
                } else {
                    title.value = ep.long_title
                    loadCover(ep.cover)
                }
            }

        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    // 直播间
    private fun loadRoomData() {
//        val url = BiliApiService.getRoomInfo(id)
//        loadDataDisposable?.dispose()
//        loadDataDisposable = MiaoHttp.getJson<ResultInfo<Room>>(url)
//            .flatMap{
//                // 曲线救国，获取直播间信息接口没有封面信息了，但个人页面有
//                // 所以先取得直播间up主的uid
//                val uid = it.data.uid
//                val url = BiliApiService.getSpace(uid.toString())
//                MiaoHttp.getJson<ResultInfo<SpaceInfo>>(url)
//            }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ r ->
//                val (code, data, msg) = r
//                val liveInfo = data.live
//                if (code == 0) {
//                    title.value = liveInfo.title
//                    loadCover(liveInfo.cover)
//                } else {
//                    activity.toast(msg)
//                }
//            }, { e ->
//                e.printStackTrace()
//            })
//        val url2 = BiliApiService.getRoomInfo(id)
    }

    // 专栏
    private suspend fun loadCvData() {
        val res = ArticleAPI().info(id).call().gson<ResultInfo<ArticleInfo>>()
        if (res.code == 0) {
            val data = res.data
            withContext(Dispatchers.Main) {
                title.value = data.title
                loadCover(data.banner_url)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    // 音频
    private suspend fun loadAuData() {
        val res = AudioAPI().info(id).call().gson<ResultInfo<AudioInfo>>()
        if (res.code == 0) {
            val data = res.data
            withContext(Dispatchers.Main) {
                title.value = data.title
                loadCover(data.cover_url)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    private fun loadCover(pic: String) {
        var newPic = pic.replace("http://", "https://")
        Glide.with(activity)
            .asBitmap()
            .load(newPic)
            .listener(object : RequestListener<Bitmap> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    activity.runOnUiThread {
                        loading.value = false
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    activity.runOnUiThread {
                        loading.value = false
                        coverBitmap.value = resource
                    }
                    return false
                }
            })
            .submit();
    }

    private fun toast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}