package com.a10miaomiao.bilimiao.ui.rank

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.ui.region.RegionDetailsFragment
import com.a10miaomiao.bilimiao.ui.region.RegionFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.fragment_region.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

class RankCategoryFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(info: BiliMiaoRank): RankCategoryFragment {
            val fragment = RankCategoryFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantUtil.INFO, info)
            fragment.arguments = bundle
            return fragment
        }
    }

    val rankInfo by lazy { arguments!!.getParcelable<BiliMiaoRank>(ConstantUtil.INFO) }
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
        val titles = arrayListOf<String>()
        rankInfo.category.forEach {
            fragments += RankCategoryDetailsFragment.newInstance(rankInfo, it.id)
            titles += it.name
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
        toolbar.title = rankInfo.rank_name
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { pop() }
    }

    override fun onSupportVisible(){
        super.onSupportVisible()
        mViewPager.adapter?.notifyDataSetChanged()
    }
}