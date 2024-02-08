package com.a10miaomiao.bilimiao.page.user

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
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.kongzue.dialogx.dialogs.PopTip


class UserMorePopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val viewModel: UserViewModel,
): PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        if (viewModel.isSelf) {
            add(Menu.FIRST, 0, 0, "退出登录")
        } else {
            if (viewModel.isFiltered) {
                add(Menu.FIRST, 1, 0, "取消屏蔽该UP主")
            } else {
                add(Menu.FIRST, 2, 0, "屏蔽该UP主")
            }
        }
        add(Menu.FIRST, 3, 0, "用浏览器打开")
        add(Menu.FIRST, 4, 0, "复制链接")
        add(Menu.FIRST, 5, 0, "分享")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> {
                viewModel.logout()
            }
            1 -> {
                viewModel.filterUpperDelete()
            }
            2 -> {
                viewModel.filterUpperAdd()
            }
            3 -> {
                val url = viewModel.getUserSpaceUrl()
                BiliUrlMatcher.toUrlLink(activity, url)
            }
            4 -> {
                val clipboard =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val label = "url"
                val text = viewModel.getUserSpaceUrl()
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)
                PopTip.show("已复制：$text")
            }
            5 -> {
                val info = viewModel.dataInfo
                val url = viewModel.getUserSpaceUrl()
                val shareIntent = Intent().also {
                    it.action = Intent.ACTION_SEND
                    it.type = "text/plain"
                    it.putExtra(Intent.EXTRA_SUBJECT, "这个UP主非常nice")
                    it.putExtra(
                        Intent.EXTRA_TEXT,
                        info?.card?.name + " " + url
                    )
                }
                activity.startActivity(Intent.createChooser(shareIntent, "分享"))
            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }


}