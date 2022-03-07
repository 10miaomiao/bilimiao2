package com.a10miaomiao.bilimiao.page.video.comment

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu

class SortOrderPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val checkedValue: Int,
) {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
    }

    private fun Menu.initMenu() {
        add(Menu.FIRST, 2, 0, "按热度").apply {
            isChecked = checkedValue == 2
        }
        add(Menu.FIRST, 1, 0, "按回复").apply {
            isChecked = checkedValue == 1
        }
        add(Menu.FIRST, 0, 0, "按时间").apply {
            isChecked = checkedValue == 0
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    fun show() {
        popupMenu.show()
    }

    companion object {
        fun getText(value: Int) = when(value) {
            2 -> "按热度"
            1 -> "按回复"
            0 -> "按时间"
            else -> "评论排序"
        }
    }

}