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
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateFragment
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateParam
import splitties.toast.toast

class VideoMorePopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val viewModel: VideoInfoViewModel,
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
        add(Menu.FIRST, 0, 0, "用浏览器打开")
        if (detailInfo != null) {
            add(Menu.FIRST, 5, 0, "分享视频(${detailInfo.stat.share})")
        } else {
            add(Menu.FIRST, 5, 0, "分享视频")
        }
        add(Menu.FIRST, 2, 0, "复制AV号")
        add(Menu.FIRST, 3, 0, "复制BV号")
        add(Menu.FIRST, 4, 0, "下载视频")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> {
                val id = viewModel.id
                var intent = Intent(Intent.ACTION_VIEW)
                var url = "http://www.bilibili.com/video/av$id"
                intent.data = Uri.parse(url)
                activity.startActivity(intent)
            }
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
                    activity.toast("已复制：$text")
                } else {
                    activity.toast("请等待信息加载完成")
                }
            }
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
                    activity.toast("请等待信息加载完成")
                }
            }
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
                    toast("视频信息未加载完成，请稍后再试")
                }

            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }


}