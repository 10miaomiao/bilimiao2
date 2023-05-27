package com.a10miaomiao.bilimiao.page.search

import android.os.Bundle
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
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.*
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.openSearchDrawer
import com.a10miaomiao.bilimiao.page.search.result.VideoResultFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.google.android.material.tabs.TabLayout
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent

class SearchResultFragment : Fragment(), DIAware, MyPage, ViewPager.OnPageChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "search.result"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.text) {
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
        title = "搜索\n-\n${viewModel.keyword ?: "无关键字"}"
        menus = mutableListOf(
            myMenuItem {
                key = MenuKeys.search
                title = "继续搜索"
                iconResource = R.drawable.ic_search_gray
            }
        ).apply {
            viewModel.curFragment?.let { addAll(it.menus) }
        }
        search = SearchConfigInfo(
            keyword = viewModel.keyword ?: ""
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.search -> {
//                val bsNav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
//                val args = SearchStartFragment.createArguments(viewModel.keyword ?: "")
//                bsNav.navigate(SearchStartFragment.actionId, args)
                requireActivity().getScaffoldView().openSearchDrawer()
            }
            else -> {
                viewModel.curFragment?.onMenuItemClick(view, menuItem)
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<SearchResultViewModel>(di)

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
                    return viewModel.fragments[p0]
                }
                override fun getCount() = viewModel.fragments.size
                override fun getPageTitle(position: Int) = viewModel.fragments[position].title
            }
            viewPager.adapter = mAdapter
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            tabLayout.setupWithViewPager(viewPager)
            tabLayout.addOnDoubleClickTabListener {
                viewModel.fragments[it.position].toListTop()
            }

            viewPager.addOnPageChangeListener(this)
            viewPager.post {
                viewModel.position = 0
                pageConfig.notifyConfigChanged()
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        viewModel.position = position
        pageConfig.notifyConfigChanged()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    fun changeVideoRegion(region: RegionInfo) {
        val curFragment = viewModel.curFragment
        if (curFragment is VideoResultFragment) {
            curFragment.changeVideoRegion(region)
        }
    }

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