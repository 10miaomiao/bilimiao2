package com.a10miaomiao.bilimiao.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.MiaoInstanceState
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.toast

class MainFragment : BaseFragment() {
    val homeFragment = HomeFragment()
    val rankFragment = RankFragment()
    val dowmloadFragment = DowmloadFragment()
    val filterFragment = FilterFragment()

    override fun layout() = R.layout.fragment_main

    override fun initView() {
        setSwipeBackEnable(false)
        mNavigation.setCheckedItem(R.id.nav_home)
        // 添加侧边菜单的点击事件
        mNavigation.setNavigationItemSelectedListener({ item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                }
                R.id.nav_rank -> {
                    switchFragment(rankFragment)
                }
                R.id.nav_dowmload -> {
                    switchFragment(dowmloadFragment)
                }
                R.id.nav_filter -> {
                    switchFragment(filterFragment)
                }
            //------------------------
                R.id.nav_theme -> {
                    toast("施工中")
                }
                R.id.nav_about -> {
                    toast("施工中")
                }
                R.id.nav_setting -> {
                    toast("施工中")
                }
            }
            mDlytContainer.closeDrawers()
            true
        })
        //切换到homeFragment
        switchFragment(homeFragment)
        //注册打开汉堡菜单事件
        RxBus.getInstance().on(ConstantUtil.OPEN_DRAWER) {
            mDlytContainer.openDrawer(Gravity.LEFT)
        }
    }

    var currentFragment: Fragment? = null
    private fun switchFragment(targetFragment: Fragment) {
        val trx = childFragmentManager.beginTransaction()
        currentFragment?.let {
            if (it == targetFragment)
                return@switchFragment
            trx.hide(it)
        }
        if (!targetFragment.isAdded) {
            trx.add(R.id.mContainer, targetFragment)
        }
        trx.show(targetFragment).commit()
        currentFragment = targetFragment
    }
}