package com.a10miaomiao.bilimiao.page.region

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu

class RankOrderPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val checkedValue: String,
) {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
    }

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "播放数").apply {
            isChecked = checkedValue == "click"
        }
        add(Menu.FIRST, 1, 0, "评论数").apply {
            isChecked = checkedValue == "scores"
        }
        add(Menu.FIRST, 2, 0, "收藏数").apply {
            isChecked = checkedValue == "stow"
        }
        add(Menu.FIRST, 3, 0, "硬币数").apply {
            isChecked = checkedValue == "coin"
        }
        add(Menu.FIRST, 4, 0, "弹幕数").apply {
            isChecked = checkedValue == "dm"
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    fun show() {
        popupMenu.show()
    }

//    <!--arrayOf("click", "scores", "stow", "coin", "dm")-->


}