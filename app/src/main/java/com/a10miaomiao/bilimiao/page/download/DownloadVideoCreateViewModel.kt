package com.a10miaomiao.bilimiao.page.download

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.download.DownloadService
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
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

    val video = fragment.requireArguments().getParcelable<DownloadVideoCreateParam>(MainNavArgs.video)!!

    var acceptDescription = listOf<String>()
    var acceptQuality = listOf<Int>()
    var quality = 64
    val qualityIndex get() = acceptQuality.indexOf(quality)
    val qualityDescription get() = acceptDescription[qualityIndex] ?: "未知"
    val selectedList = mutableListOf<String>()
    var downloadedList = mutableListOf<BiliDownloadEntryInfo>()

    init {
        getDownloadedList()
        loadAcceptQuality()
    }

    private fun getDownloadedList() = viewModelScope.launch(Dispatchers.IO) {
        val downloadService = DownloadService.getService(context)
        _getDownloadedList(downloadService)
        downloadService.downloadListVersion.collect {
            _getDownloadedList(downloadService)
        }
    }

    private fun _getDownloadedList(downloadService: DownloadService) {
        ui.setState {
            downloadedList = downloadService.downloadList.filter {
                it.entry.avid?.toString() == video.aid
            }.map { it.entry }.toMutableList()
        }
    }

    private fun loadAcceptQuality() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            quality = prefs.getInt("player_quality", 64)
            val aid = video.aid
            val cid = video.pages[0].cid
            val res = BiliApiService.playerAPI.getVideoPalyUrl(
                aid, cid, quality, fnval = 4048
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

    fun selectedQuality(value: Int) {
        ui.setState {
            quality = value
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

    fun selectedItem(item: DownloadVideoCreateParam.Page) {
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
        val pageData = BiliDownloadEntryInfo.PageInfo(
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
        val currentTime = System.currentTimeMillis()
        val biliVideoEntry = BiliDownloadEntryInfo(
            media_type = 2,
            has_dash_audio = true,
            is_completed = false,
            total_bytes = 0,
            downloaded_bytes = 0,
            title = video.title,
            type_tag = quality.toString(),
            cover = video.pic,
            prefered_video_quality = quality,
            quality_pithy_description = qualityDescription,
            guessed_total_bytes = 0,
            total_time_milli = 0,
            danmaku_count = 1000,
            time_update_stamp = currentTime,
            time_create_stamp = currentTime,
            can_play_in_advance = true,
            interrupt_transform_temp_file = false,
            avid = video.aid.toLong(),
            spid = 0,
            season_id = null,
            ep = null,
            source = null,
            bvid = video.bvid,
            owner_id = video.mid.toLong(),
            page_data = pageData
        )
        viewModelScope.launch {
            val downloadService = DownloadService.getService(context)
            downloadService.createDownload(biliVideoEntry)
        }
    }
}