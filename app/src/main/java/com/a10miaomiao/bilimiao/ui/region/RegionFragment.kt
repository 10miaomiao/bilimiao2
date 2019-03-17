package com.a10miaomiao.bilimiao.ui.region

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.widget.NestedScrollView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.fragment_region.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.support.v4.dip

class RegionFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(region: Home.Region): RegionFragment {
            val fragment = RegionFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantUtil.REGION, region)
            fragment.arguments = bundle
            return fragment
        }
    }

    val region by lazy { arguments!!.getParcelable(ConstantUtil.REGION) as Home.Region }
    val fragments = ArrayList<Fragment>(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_region, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initToolbar()
    }

    private fun initView() {
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
        app_bar_layout.setPadding(0, app_bar_layout.getStatusBarHeight(), 0, 0)
        toolbar.title = region.name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { pop() }

        app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            toolbar.alpha = 1 - Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
        })
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        mViewPager.adapter?.notifyDataSetChanged()
    }

}