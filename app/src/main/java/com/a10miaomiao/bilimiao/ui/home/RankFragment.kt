package com.a10miaomiao.bilimiao.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorRes
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.widget.ArrayAdapter
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.MiaoUI
import org.jetbrains.anko.*


class RankFragment : MiaoFragment() {

    override fun render() = MiaoUI {
        verticalLayout {
            headerView {
                title("排行榜")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
            }
        }
    }
}