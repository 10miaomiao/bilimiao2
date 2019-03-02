package com.a10miaomiao.bilimiao.ui.home

import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.anko.MiaoUI
import org.jetbrains.anko.*


class FilterFragment : MiaoFragment() {

    override fun render() = MiaoUI {
        verticalLayout {
            headerView {
                title("屏蔽设置")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
            }
        }
    }
}