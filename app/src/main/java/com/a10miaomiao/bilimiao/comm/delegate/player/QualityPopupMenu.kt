package com.a10miaomiao.bilimiao.comm.delegate.player

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.a10miaomiao.bilimiao.comm.delegate.player.model.PlayerSourceInfo
import kotlin.reflect.KFunction1

class QualityPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val list: List<PlayerSourceInfo.AcceptInfo>,
    private val value: Int,
) {
    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
    }

    private fun Menu.initMenu() {
        list.forEachIndexed { index, item ->
            add(Menu.FIRST, index, 0, item.description).apply {
                isChecked = value == item.quality
            }
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnChangedQualityListener(changedQuality: (Int) -> Unit) {
        popupMenu.setOnMenuItemClickListener {
            val position = it.itemId
            val item = list[position]
            changedQuality(item.quality)
            false
        }
    }

    fun show() {
        popupMenu.show()
    }
}