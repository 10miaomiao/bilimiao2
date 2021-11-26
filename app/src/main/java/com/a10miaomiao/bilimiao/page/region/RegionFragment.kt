package com.a10miaomiao.bilimiao.page.region

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.page.MainViewModel
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.a10miaomiao.bilimiao.widget.comm.getAppBarView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.topPadding

class RegionFragment : Fragment(), DIAware , MyPage {

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@RegionFragment }
    }

    private val viewModel by diViewModel<RegionViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val timeSettingStore: TimeSettingStore by instance()

    private lateinit var mViewPager: ViewPager
    private lateinit var mTabLayout: TabLayout

    override val pageConfig = myPageConfig {
        title = "时光姬-" + viewModel.region.name
        menus = listOf(
            myMenuItem {
                key = 0
                iconResource = R.drawable.ic_baseline_filter_list_grey_24
                title = "排序"
            },
            myMenuItem {
                key = 1
                title = "当前时间线"
                subTitle = viewModel.getTimeText()
            },
        )
    }

    override fun onMenuItemClick(view: MenuItemView) {
        super.onMenuItemClick(view)
        when (view.prop.key) {
            1 -> {
                val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
                nav.navigate(Uri.parse("bilimiao://time/setting"))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
            timeSettingStore.stateFlow.collect {

                pageConfig.notifyConfigChanged()
            }
        }
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
        val contentInsets = windowStore.state.contentInsets
        verticalLayout {
            views {
                +tabLayout(234) {
                    _topPadding = contentInsets.top
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