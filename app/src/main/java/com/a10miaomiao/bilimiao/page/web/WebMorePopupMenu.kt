package com.a10miaomiao.bilimiao.page.web

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
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateFragment
import com.a10miaomiao.bilimiao.page.download.DownloadVideoCreateParam
import com.a10miaomiao.bilimiao.page.video.VideoInfoViewModel
import com.kongzue.dialogx.dialogs.PopTip

internal class WebMorePopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val url: String,
) : PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "用浏览器打开")
        add(Menu.FIRST, 1, 0, "分享")
        add(Menu.FIRST, 2, 0, "复制链接")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                activity.startActivity(intent)
            }
            1 -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "链接分享")
                    putExtra(Intent.EXTRA_TEXT, url)
                }
                activity.startActivity(Intent.createChooser(shareIntent, "分享"))
            }
            2 -> {
                val clipboard =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("url", url)
                clipboard.setPrimaryClip(clip)
                PopTip.show("已复制：$url")
            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }

}