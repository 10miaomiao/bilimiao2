package com.a10miaomiao.bilimiao.page.search.result

import android.view.View
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.recycler.RecyclerViewFragment
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.scaffold.MenuItemView

abstract class BaseResultFragment : RecyclerViewFragment() {
    abstract val title: String
    abstract val menus: List<MenuItemPropInfo>
    abstract fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo)

    fun notifyConfigChanged() {
        val pFragment = parentFragment
        if (pFragment is MyPage) {
            pFragment.pageConfig.notifyConfigChanged()
        }
    }
}