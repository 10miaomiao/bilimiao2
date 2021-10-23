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

    private val viewModel by diViewModel<MainViewModel>(di)

    val region by lazy { requireArguments().getParcelable<RegionInfo>(MainNavGraph.args.region)!! }
    val fragments = ArrayList<Fragment>(0)

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
//        initToolbar()
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