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
import splitties.toast.toast

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
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }


}