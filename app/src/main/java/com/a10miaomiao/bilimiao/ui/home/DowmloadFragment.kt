package com.a10miaomiao.bilimiao.ui.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.bilimiao.utils.startFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI


class DowmloadFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MainActivity.of(context!!).dynamicTheme(this) { render().view }
    }

    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    private fun render() = UI {
        verticalLayout {
            headerView {
                title("下载")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    MainActivity.of(context!!).openDrawer()
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