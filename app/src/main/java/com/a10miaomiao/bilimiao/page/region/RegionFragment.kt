package com.a10miaomiao.bilimiao.page.region

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.*
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.tabs.TabLayout
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.views.dsl.core.*

class RegionFragment : Fragment(), DIAware , MyPage {

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@RegionFragment }
    }

    private val viewModel by diViewModel<RegionViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val timeSettingStore: TimeSettingStore by instance()

    private val ID_viewPager = 233
    private val ID_tabLayout = 234

    override val pageConfig = myPageConfig {
        title = "时光姬-" + viewModel.region.name
        menus = listOf(
            myMenuItem {
                key = MenuKeys.filter
                iconResource = R.drawable.ic_baseline_filter_list_grey_24
                title = timeSettingStore.getRankOrderText()
            },
            myMenuItem {
                key = MenuKeys.time
                title = "当前时间线"
                subTitle = viewModel.getTimeText()
            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.filter -> {
                val pm = RankOrderPopupMenu(
                    activity = requireActivity(),
                    anchor = view,
                    checkedValue = timeSettingStore.state.rankOrder
                )
                pm.setOnMenuItemClickListener(handleMenuItemClickListener)
                pm.show()
            }
            MenuKeys.time -> {
                val nav = requireActivity().findNavController(com.a10miaomiao.bilimiao.R.id.nav_bottom_sheet_fragment)
                val url = "bilimiao://time/setting"
                nav.navigate(
                    MainNavGraph.action.global_to_compose, bundleOf(
                        MainNavGraph.args.url to url
                    )
                )
            }
        }
    }

    private val handleMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
        it.isChecked = true
        timeSettingStore.setState {
            rankOrder = arrayOf("click", "scores", "stow", "coin", "dm")[it.itemId]
        }
        false
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
        initView(view)
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager>(ID_viewPager)
        if  (viewPager.adapter == null) {
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
            viewPager.adapter = mAdapter
            tabLayout.setTabsFromPagerAdapter(mAdapter)
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    @SuppressLint("ResourceType")
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.state.contentInsets
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