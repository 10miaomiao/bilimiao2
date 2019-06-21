package com.a10miaomiao.bilimiao.ui.home

import android.support.v7.widget.Toolbar
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.bilimiao.utils.startFragment
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.anko.MiaoUI
import org.jetbrains.anko.*


class DowmloadFragment : MiaoFragment() {

    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    override fun render() = MiaoUI {
        verticalLayout {
            headerView {
                title("下载")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(onMenuItemClick)
            }
            imageView(R.drawable.gugugu){
                scaleType = ImageView.ScaleType.CENTER
            }.lparams(matchParent, matchParent)
        }
    }
}