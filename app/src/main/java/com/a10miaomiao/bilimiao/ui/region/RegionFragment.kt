package com.a10miaomiao.bilimiao.ui.region

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.miaoandriod.MiaoAnkoContext
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_region.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.verticalLayout

class RegionFragment : BaseFragment() {
    override fun layout() = R.layout.fragment_region

    val region by lazy { arguments!!.getParcelable(ConstantUtil.REGION) as Home.Region }
    val fragments = ArrayList<Fragment>(0)

    override fun initView() {
        initToolbar()
        val titles = region.children.map {
            fragments.add(RegionDetailsFragment.newInstance(it.tid))
            it.name
        }
        val mAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int) = fragments[p0]
            override fun getCount() = fragments.size
            override fun getPageTitle(position: Int) = titles[position]
        }
        mViewPager.adapter = mAdapter
        mTabLayout.setTabsFromPagerAdapter(mAdapter)
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        mTabLayout.setupWithViewPager(mViewPager)
    }

    private fun initToolbar() {
        app_bar_layout.setPadding(0, getStatusBarHeight(), 0, 0)
        toolbar.title = region.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { pop() }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = context!!.resources.getIdentifier("status_bar_height", "dimen",
                "android")
        if (resourceId > 0) {
            result = context!!.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    companion object {
        fun newInstance(region: Home.Region): RegionFragment {
            val fragment = RegionFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantUtil.REGION, region)
            fragment.arguments = bundle
            return fragment
        }
    }

}