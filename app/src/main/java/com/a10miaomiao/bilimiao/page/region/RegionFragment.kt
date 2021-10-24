package com.a10miaomiao.bilimiao.page.region

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.page.MainViewModel
import com.google.android.material.tabs.TabLayout
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.views.dsl.core.*

class RegionFragment : Fragment(), DIAware {

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@RegionFragment }
    }

    private val viewModel by diViewModel<RegionViewModel>(di)

    private lateinit var mViewPager: ViewPager
    private lateinit var mTabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val mAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int): Fragment {
                var fragment = viewModel.fragments[p0]
                if (fragment == null) {
                    val tid = viewModel.region.children[p0].tid
                    fragment = RegionDetailsFragment.newInstance(tid)
                }
                return fragment
            }
            override fun getCount() = viewModel.region.children.size
            override fun getPageTitle(position: Int) = viewModel.region.children[position].name
        }
        mViewPager.adapter = mAdapter
        mTabLayout.setTabsFromPagerAdapter(mAdapter)
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        mTabLayout.setupWithViewPager(mViewPager)
    }

    @SuppressLint("ResourceType")
    val ui = miaoBindingUi {
        verticalLayout {
            views {
                +tabLayout(234) {
                    mTabLayout = this
                }..lParams(matchParent, wrapContent)
                +viewPager(233) {
                    mViewPager = this
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
            }
        }
    }
}