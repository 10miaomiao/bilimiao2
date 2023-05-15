package com.a10miaomiao.bilimiao.page.download

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.BiliVideoPageData
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class DownloadVideoCreateViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val downloadDelegate: DownloadDelegate by instance()

    val video = fragment.requireArguments().getParcelable<DownloadVideoCreateParam>(MainNavArgs.video)!!

    var acceptDescription = listOf<String>()
    var acceptQuality = listOf<Int>()
    var quality = 64
    val selectedList = mutableListOf<String>()

    init {
        loadAcceptQuality()
    }

    fun loadAcceptQuality() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            quality = prefs.getInt("player_quality", 64)
            val aid = video.aid
            val cid = video.pages[0].cid
            val res = BiliApiService.playerAPI.getVideoPalyUrl(
                aid, cid, quality, fnval = 1
            )
            ui.setState {
                acceptQuality = res.accept_quality
                quality = res.quality
                acceptDescription = res.accept_description
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                context.toast(e.message ?: "网络错误")
            }
        }
    }

    fun startDownload () {
        if (selectedList.size == 0) {
            context.toast("请选择分P")
            return
        }
        selectedList.forEach { cid ->
            val page = video.pages.find { it.cid == cid }
            if (page != null) {
                downloadVideo(page)
            }
        }
        context.toast("成功创建${selectedList.size}条记录")
        selectedList.clear()
    }

    fun selectedItem (item: DownloadVideoCreateParam.Page) {
        val index = selectedList.indexOf(item.cid)
        ui.setState {
            if (index == -1) {
                selectedList.add(item.cid)
            } else {
                selectedList.removeAt(index)
            }
        }
    }

    private fun downloadVideo(page: DownloadVideoCreateParam.Page) {
        val pageData = BiliVideoPageData(
            cid = page.cid.toLong(),
            page = page.page,
            from = page.from,
            part = page.part,
            vid = page.vid,
            has_alias = false,
            tid = 0,
            width = 0,
            height = 0,
            rotate = 0,
            download_title = "视频已缓存完成",
            download_subtitle = video.title
        )
        val biliVideoEntry = BiliVideoEntry(
            media_type = 1,
            has_dash_audio = false,
            is_completed = false,
            total_bytes = 0,
            downloaded_bytes = 0,
            title = video.title,
            type_tag = quality.toString(),
            cover = video.pic,
            prefered_video_quality = 112,
            guessed_total_bytes = 0,
            total_time_milli = 0,
            danmaku_count = 1000,
            time_update_stamp = 1589831292571L,
            time_create_stamp = 1589831261539L,
            can_play_in_advance = true,
            interrupt_transform_temp_file = false,
            avid = video.aid.toLong(),
            spid = 0,
            seasion_id = 0,
            bvid = video.bvid,
            owner_id = video.mid.toLong(),
            page_data = pageData
        )
        val downloadService = downloadDelegate.downloadService
        downloadService.createDownload(biliVideoEntry)
    }
}