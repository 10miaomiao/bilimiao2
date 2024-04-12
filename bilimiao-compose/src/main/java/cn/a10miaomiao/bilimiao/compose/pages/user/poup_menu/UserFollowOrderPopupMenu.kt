package cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu

internal class UserFollowOrderPopupMenu (
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
        //  arrayOf("", "attention")
        add(Menu.FIRST, 0, 0, "最常访问").apply {
            isChecked = checkedValue == "attention"
        }
        add(Menu.FIRST, 1, 0, "关注顺序").apply {
            isChecked = checkedValue == ""
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