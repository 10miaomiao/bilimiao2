package com.a10miaomiao.bilimiao.page.rank

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.NavType
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.viewpager.widget.ViewPager
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.dsl.addOnDoubleClickTabListener
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.RecyclerViewFragment
import com.a10miaomiao.bilimiao.page.search.SearchStartFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.tabs.TabLayout
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent

class RankFragment : Fragment(), DIAware, MyPage, ViewPager.OnPageChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "rank"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilibili://rank")
            deepLink("bilibili://rank?type={type}")
            argument(MainNavArgs.type) {
                type = NavType.StringType
                nullable = true
            }
        }

        fun createArguments(text: String): Bundle {
            return bundleOf(
                MainNavArgs.text to text
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "排行榜"
        menus = mutableListOf(
            myMenuItem {
                key = MenuKeys.search
                title = "搜索"
                iconResource = R.drawable.ic_search_gray
            }
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.search -> {
                val bsNav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
                bsNav.navigate(SearchStartFragment.actionId)
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<RankViewModel>(di)

    private val ID_viewPager = View.generateViewId()
    private val ID_tabLayout = View.generateViewId()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager>(ID_viewPager)
        if  (viewPager.adapter == null) {
            val mAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(p0: Int): Fragment {
                    val tid = viewModel.tids[p0]
                    return RankDetailFragment.newInstance(tid)
                }
                override fun getCount() = viewModel.tids.size
                override fun getPageTitle(position: Int) = viewModel.titles[position]
                private val registeredFragments = SparseArray<Fragment>()
                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    val obj = super.instantiateItem(container, position)
                    if (obj is Fragment) {
                        registeredFragments.put(position, obj)
                    }
                    return obj
                }
                fun getRegisteredFragment(position: Int): Fragment? {
                    return registeredFragments[position]
                }
            }
            viewPager.adapter = mAdapter
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            tabLayout.setupWithViewPager(viewPager)
            tabLayout.addOnDoubleClickTabListener {
                val fragment = mAdapter.getRegisteredFragment(it.position)
                if (fragment is RecyclerViewFragment) {
                    fragment.toListTop()
                }
            }
            viewPager.addOnPageChangeListener(this)
            viewPager.post {
                viewModel.position = 0
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        viewModel.position = position
//        pageConfig.notifyConfigChanged()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

//    fun changeVideoRegion(region: RegionInfo) {
//        val curFragment = viewModel.curFragment
//        if (curFragment is VideoResultFragment) {
//            curFragment.changeVideoRegion(region)
//        }
//    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            views {
                +tabLayout(ID_tabLayout) {
                    _topPadding = contentInsets.top
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right
                }..lParams(matchParent, wrapContent)
                +viewPager(ID_viewPager) {
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
            }
        }
    }

}