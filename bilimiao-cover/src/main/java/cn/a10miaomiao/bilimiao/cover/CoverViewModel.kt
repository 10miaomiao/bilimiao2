package cn.a10miaomiao.bilimiao.cover

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import com.a10miaomiao.bilimiao.comm.apis.*
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.article.ArticleInfo
import com.a10miaomiao.bilimiao.comm.entity.audio.AudioInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonEpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.live.RoomInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
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

    var coverUrl = MutableLiveData<String>()
    var coverBitmap = MutableLiveData<Bitmap>()
    var title = MutableLiveData<String>()
    var loading = MutableLiveData<Boolean>()
    var fileName = MutableLiveData<String>()


    init {

    }

    fun setConfig(type: String, id: String) {
        this.type = type
        this.id = id
        this.fileName.value = type + id
        loadData()
    }

    fun openMore() {
        val intent = Intent(Intent.ACTION_VIEW)
        when (type) {
            "AV" -> {
                intent.data = Uri.parse("bilimiao://video/$id")
                activity.startActivity(intent)
            }
            "BV" -> {
                intent.data = Uri.parse("bilimiao://video/BV$id")
                activity.startActivity(intent)
            }
            "SS" -> {
                intent.data = Uri.parse("bilimiao://bangumi/season/$id")
                activity.startActivity(intent)
            }
            "UID" -> {
                intent.data = Uri.parse("bilimiao://user/$id")
                activity.startActivity(intent)
            }
            "EP" -> {
                intent.data = Uri.parse("https://m.bilibili.com/bangumi/play/ep$id")
                activity.startActivity(intent)
            }
            "ROOM" -> {
                intent.data = Uri.parse("https://live.bilibili.com/$id")
                activity.startActivity(intent)
            }
            "CV" -> {
                intent.data = Uri.parse("https://www.bilibili.com/read/cv$id")
                activity.startActivity(intent)
            }
            "AU" -> {
                intent.data = Uri.parse("https://m.bilibili.com/audio/au$id")
                activity.startActivity(intent)
            }
            else -> {
                toast("暂不支持查看更多")
            }
        }
    }

    /**
     * 解析短链接出长链接，然后再正则匹配出类型和id
     */
    fun resolveUrl(url: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = MiaoHttp(url).get()
            val url = res.request.url.toString()
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
        val req = ViewReq(aid = id.toLong())
        val res = BiliGRPCHttp.request {
            ViewGRPC.view(req)
        }.awaitCall()
        val arc = res.arc ?: res.activitySeason?.arc
        if (arc != null) {
            withContext(Dispatchers.Main) {
                title.value = arc.title
                loadCover(arc.pic)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast("arc为空")
            }
        }
    }

    // 普通视频 BV
    private suspend fun loadBVData() {
        val req = ViewReq(bvid = id)
        val res = BiliGRPCHttp.request {
            ViewGRPC.view(req)
        }.awaitCall()
        val arc = res.arc ?: res.activitySeason?.arc
        if (arc != null) {
            withContext(Dispatchers.Main) {
                title.value = arc.title
                loadCover(arc.pic)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast("arc为空")
            }
        }
    }

    // 番剧
    private fun loadSsData() {

    }

    // 番剧剧集
    private suspend fun loadEpData() {
        val res = BangumiAPI().episodeInfo(id).call().json<ResponseResult<SeasonEpisodeInfo>>()
        if (res.code == 0) {
            val data = res.requireData()
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
    private suspend fun loadRoomData() {
        val res = LiveApi().info(id).call().json<ResponseData<RoomInfo>>()
        if (res.isSuccess) {
            val data = res.requireData()
            withContext(Dispatchers.Main) {
                title.value = data.title
                loadCover(data.user_cover)
            }
        } else {
            withContext(Dispatchers.Main) {
                toast(res.message)
            }
        }
    }

    // 专栏
    private suspend fun loadCvData() {
        val res = ArticleAPI().info(id).call().json<ResponseData<ArticleInfo>>()
        if (res.isSuccess) {
            val data = res.requireData()
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
        val res = AudioAPI().info(id).call().json<ResponseData<AudioInfo>>()
        if (res.isSuccess) {
            val data = res.requireData()
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
        var newPic = UrlUtil.autoHttps(pic)
        coverUrl.value = newPic
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