package com.a10miaomiao.bilimiao.widget.menu

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu

class CheckPopupMenu<T>(
    private val context: Context,
    private val anchor: View,
    private val menus: List<MenuItemInfo<T>>,
    private val value: T,
) : PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(context, anchor)

    var onMenuItemClick: ((item: MenuItemInfo<T>) -> Unit)? = null

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        menus.forEachIndexed { index, item ->
            add(Menu.FIRST, index, 0, item.title).apply {
                isChecked = value == item.value
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        onMenuItemClick?.invoke(menus[item.itemId])
        return true
    }

    class MenuItemInfo<T>(
        var title: String,
        var value: T,
    )

}