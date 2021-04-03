package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.BiliVideoPageData
import cn.a10miaomiao.player.VideoSource
import com.a10miaomiao.bilimiao.entity.Page
import com.a10miaomiao.bilimiao.netword.PlayurlHelper
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import io.reactivex.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.toast

class CreateDownloadViewModel(
        val context: Context,
        val aid: String,
        val bvid: String,
        val title: String,
        val cover: String,
        val mid: Long,
        val pages: ArrayList<Page>,
        val index: Int
) : ViewModel() {

    var acceptDescription = MiaoLiveData(listOf<String>())
    var acceptQuality = listOf<Int>()
    var quality = 64
    val selectedList = MiaoList<String>()

    init {
        val cid = pages[index].cid
        PlayurlHelper.getVideoPalyUrl(aid, cid, quality)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    acceptQuality = it.accept_quality
                    quality = it.quality
                    acceptDescription set it.accept_description
                }, {
                    context.toast(it.message ?: "网络错误")
                })
    }

    fun startDownload () {
        if (selectedList.size == 0) {
            context.toast("请选择分P")
            return
        }
        selectedList.forEach { cid ->
            val page = pages.find { it.cid == cid }
            if (page != null) {
                downloadVideo(page)
            }
        }
        context.toast("成功创建${selectedList.size}条记录")
        selectedList.clear()
    }

    private fun downloadVideo(page: Page) {
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
                download_subtitle = title
        )
        val biliVideoEntry = BiliVideoEntry(
                media_type = 1,
                has_dash_audio = false,
                is_completed = false,
                total_bytes = 0,
                downloaded_bytes = 0,
                title = title,
                type_tag = quality.toString(),
                cover = cover,
                prefered_video_quality = 112,
                guessed_total_bytes = 0,
                total_time_milli = 0,
                danmaku_count = 1000,
                time_update_stamp = 1589831292571L,
                time_create_stamp = 1589831261539L,
                can_play_in_advance = true,
                interrupt_transform_temp_file = false,
                avid = aid.toLong(),
                spid = 0,
                seasion_id = 0,
                bvid = bvid,
                owner_id = mid,
                page_data = pageData
        )
        val downloadService = MainActivity.of(context!!)
                .downloadDelegate
                .downloadService
        downloadService.createDownload(biliVideoEntry)
    }

}