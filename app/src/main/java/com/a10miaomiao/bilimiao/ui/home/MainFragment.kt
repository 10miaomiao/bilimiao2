package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.ViewModelStore
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.bilimiao.utils.network
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.MiaoInstanceState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_main.*
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.support.v4.toast
import android.os.Build
import android.app.Activity
import com.a10miaomiao.bilimiao.ui.setting.AboutFragment
import com.a10miaomiao.bilimiao.ui.setting.SettingFragment
import com.a10miaomiao.bilimiao.utils.startFragment


class MainFragment : SupportFragment() {
    val homeFragment = HomeFragment()
    val rankFragment = RankFragment()
    val dowmloadFragment = DowmloadFragment()
    val filterFragment = FilterFragment()
    lateinit var mNavHeaderPic: ImageView
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout(), container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mNavHeaderPic = mNavigation.getHeaderView(0).findViewById(R.id.iv)
        initView()
    }

    private fun layout() = R.layout.fragment_main

    private fun initView() {
        Glide.with(context)
                .load(R.drawable.top_bg1)
                .centerCrop()
                .dontAnimate()
                .into(mNavHeaderPic) // 直接在xml设置，滑动会卡顿

//        setSwipeBackEnable(false)
        mNavigation.setCheckedItem(R.id.nav_home)
        // 添加侧边菜单的点击事件
        mNavigation.setNavigationItemSelectedListener { item ->
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
                    startFragment(AboutFragment())
                }
                R.id.nav_setting -> {
                    startFragment(SettingFragment())
                }
            }
            mDlytContainer.closeDrawers()
            true
        }
        if (viewModel.currentFragment == null) {
            switchFragment(homeFragment)
        } else {
            switchFragment(viewModel.currentFragment!!)
        }
        //注册打开汉堡菜单事件
        RxBus.getInstance().on(ConstantUtil.OPEN_DRAWER) {
            mDlytContainer.openDrawer(Gravity.LEFT)
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        val trx = childFragmentManager.beginTransaction()
        viewModel.currentFragment?.let {
            if (it == targetFragment)
                return@switchFragment
            trx.hide(it)
        }
        if (!targetFragment.isAdded) {
            trx.add(R.id.mContainer, targetFragment)
        }
        trx.show(targetFragment).commit()
        viewModel.currentFragment = targetFragment
    }
}