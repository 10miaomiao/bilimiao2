package com.a10miaomiao.bilimiao.page.search.result

import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView

abstract class BaseResultFragment : Fragment() {
    abstract val title: String
    abstract val menus: List<MenuItemView.MenuItemPropInfo>
    abstract fun onMenuItemClick(view: MenuItemView)

    fun notifyConfigChanged() {
        val pFragment = parentFragment
        if (pFragment is MyPage) {
            pFragment.pageConfig.notifyConfigChanged()
        }
    }
}