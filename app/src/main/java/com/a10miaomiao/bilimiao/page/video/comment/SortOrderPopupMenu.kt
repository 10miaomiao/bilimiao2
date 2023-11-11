package com.a10miaomiao.bilimiao.page.video.comment

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
//        add(Menu.FIRST, 1, 0, "默认排序").apply {
//            isChecked = checkedValue == 1
//        }
        add(Menu.FIRST, 2, 0, "按时间").apply {
            isChecked = checkedValue == 2
        }
        add(Menu.FIRST, 3, 0, "按热度").apply {
            isChecked = checkedValue == 3
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
//            1 -> "默认排序"
            2 -> "按时间"
            3 -> "按热度"
            else -> "评论排序"
        }
    }

}