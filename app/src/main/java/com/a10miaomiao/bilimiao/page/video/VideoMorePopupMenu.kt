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
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import splitties.toast.toast

class VideoMorePopupMenu (
    private val activity: Activity,
    private val anchor: View,
    private val viewModel: VideoInfoViewModel,
): PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "用浏览器打开")
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
                    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                    val args = bundleOf(
                        MainNavGraph.args.video to info
                    )
                    nav.navigate(MainNavGraph.action.global_to_downloadVideoCreate, args)
                } else {
                    activity.toast("请等待信息加载完成")
                }
            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }


}