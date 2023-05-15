package com.a10miaomiao.bilimiao.page

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate

class MainBackPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val basePlayerDelegate: BasePlayerDelegate,
): PopupMenu.OnMenuItemClickListener {

    private val popupMenu = PopupMenu(activity, anchor)

    init {
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
    }

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "返回首页")
        add(Menu.FIRST, 1, 0, "退出播放")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> {
                val nav = activity.findNavController(R.id.nav_host_fragment)
                return nav.popBackStack(MainNavGraph.dest.main, false)
            }
            1 -> {
                basePlayerDelegate.closePlayer()
            }
        }
        return false
    }

    fun show() {
        popupMenu.show()
    }


}