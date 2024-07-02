package com.a10miaomiao.bilimiao.comm.delegate.player

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu

class SpeedPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val value: Float,
    private val list: List<Float>,
) {
    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
    }

    private fun Menu.initMenu() {
        list.forEachIndexed { index, item ->
            add(Menu.FIRST, index, 0, item.toString()).apply {
                isChecked = value == item
            }
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnChangedSpeedListener(changedSpeed: (Float) -> Unit) {
        popupMenu.setOnMenuItemClickListener {
            val position = it.itemId
            val item = list[position]
            changedSpeed(item)
            false
        }
    }

    fun show() {
        popupMenu.show()
    }
}