package com.a10miaomiao.bilimiao.comm.delegate.player

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu

class QualityPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val list: List<String>,
    private val value: String,
) {
    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
    }

    private fun Menu.initMenu() {
        list.forEachIndexed { index, item ->
            add(Menu.FIRST, index, 0, item).apply {
                isChecked = value == item
            }
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    fun show() {
        popupMenu.show()
    }
}