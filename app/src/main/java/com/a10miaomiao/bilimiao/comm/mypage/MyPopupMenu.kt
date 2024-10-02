package com.a10miaomiao.bilimiao.comm.mypage

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.MessageDialog

class MyPopupMenu(
    private val activity: Activity,
    private val myPage: MyPage,
    private val myPageMenu: MyPageMenu,
    private val anchorView: View,
): PopupMenu.OnMenuItemClickListener{


    private fun Menu.addItems(
        groupId: Int,
        myMenu: MyPageMenu,
    ) {
        myMenu.items.forEach {
            val key = it.key ?: return
            add(groupId, key, 0, it.title).apply {
                if (myMenu.checkable) {
                    isChecked = myMenu.checkedKey == key
                }
            }
            it.childMenu?.let { childMenu ->
                addItems(key, childMenu)
            }
        }
        if (myMenu.checkable) {
            setGroupCheckable(groupId, true, true)
        }
    }

    private fun Menu.initMenu() {
        addItems(Menu.FIRST, myPageMenu)
    }

    private fun MyPageMenu.findMyItemByKey(key: Int): MenuItemPropInfo? {
        for (item in items) {
            if (item.key == key) {
                return item
            }
            val childMenu = item.childMenu ?: continue
            childMenu.findMyItemByKey(key)?.let {
                return@findMyItemByKey it
            }
        }
        return null
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val myItem = myPageMenu.findMyItemByKey(item.itemId)
        if (myItem != null) {
            myPage.onMenuItemClick(anchorView, myItem)
        }
        return false
    }

    fun show() {
        val popupMenu = PopupMenu(activity, anchorView)
        popupMenu.menu.initMenu()
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }


}