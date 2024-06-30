package com.a10miaomiao.bilimiao.page.video

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateFragment
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateParam
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoMorePopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val viewModel: VideoInfoViewModel,
    private val playerStore: PlayerStore,
    private val playListStore: PlayListStore,
) : PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        val detailInfo = viewModel.info
        val playListState = playListStore.state
        add(Menu.FIRST, 0, 0, "用浏览器打开")
        if (detailInfo != null) {
            if (playListState.isEmpty()) {
                add(Menu.FIRST, 13, 0, "添加至稍后再看")
            } else {
                val current = playerStore.getPlayListCurrentPosition()
                addSubMenu(Menu.FIRST, 10, 0, "添加至...").also {
                    if (current != -1) {
                        it.add(10, 11, 0, "添加至下一个播放")
                    }
                    it.add(10, 12, 0, "添加至最后一个播放")
                    it.add(10, 13, 0, "添加至稍后再看")
                }
            }
            add(Menu.FIRST, 5, 0, "分享视频(${detailInfo.stat.share})")
            add(Menu.FIRST, 2, 0, "复制AV号")
            add(Menu.FIRST, 3, 0, "复制BV号")
            add(Menu.FIRST, 4, 0, "下载视频")
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // 用浏览器打开
            0 -> {
                val id = viewModel.id
                val url = "http://www.bilibili.com/video/av$id"
                BiliUrlMatcher.toUrlLink(activity, url)
            }
            // 复制AV、BV号
            2, 3 -> {
                val info = viewModel.info
                if (info != null) {
                    val clipboard =
                        activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var label: String
                    var text: String
                    if (item.itemId == 2) {
                        label = "AV"
                        text = info.aid
                    } else {
                        label = "BV"
                        text = info.bvid
                    }
                    val clip = ClipData.newPlainText(label, text)
                    clipboard.setPrimaryClip(clip)
                    PopTip.show("已复制：$text")
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            // 下载视频
            4 -> {
                val info = viewModel.info
                if (info != null) {
                    val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
                    val video = DownloadVideoCreateParam(
                        aid = info.aid,
                        bvid = info.bvid,
                        title = info.title,
                        pic = info.pic,
                        mid = info.owner.mid,
                        pages = info.pages.map {
                            DownloadVideoCreateParam.Page(
                                cid = it.cid,
                                page = it.page,
                                part = it.part,
                                from = it.from,
                                vid = it.vid,
                            )
                        }
                    )
                    val args = DownloadVideoCreateFragment.createArguments(video)
                    nav.navigate(DownloadVideoCreateFragment.actionId, args)
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            // 分享视频
            5 -> {
                val info = viewModel.info
                if (info != null) {
                    var shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "bilibili视频分享")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${info.title} https://www.bilibili.com/video/${info.bvid}"
                        )
                    }
                    activity.startActivity(Intent.createChooser(shareIntent, "分享"))
                } else {
                    PopTip.show("视频信息未加载完成，请稍后再试")
                }
            }
            // 添加至下一个播放
            11 -> {
                val info = viewModel.info
                if (info != null) {
                    val current = playerStore.getPlayListCurrentPosition()
                    if (current != -1) {
                        playListStore.run {
                            addItem(
                                info.toPlayListItem(),
                                current + 1
                            )
                        }
                        PopTip.show("已添加至下一个播放")
                    } else {
                        PopTip.show("添加失败，找不到正在播放的视频")
                    }
                } else {
                    PopTip.show("视频信息未加载完成，请稍后再试")
                }
            }
            // 添加至最后一个播放
            12 -> {
                val info = viewModel.info
                if (info != null) {
                    playListStore.run {
                        addItem(
                            info.toPlayListItem(),
                            state.items.size,
                        )
                    }
                    PopTip.show("已添加至最后一个播放")
                } else {
                    PopTip.show("视频信息未加载完成，请稍后再试")
                }
            }
            // 添加至稍后再看
            13 -> {
                val info = viewModel.info
                if (info != null) {
                    viewModel.addVideoHistoryToview()
                } else {
                    PopTip.show("视频信息未加载完成，请稍后再试")
                }
            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }
}