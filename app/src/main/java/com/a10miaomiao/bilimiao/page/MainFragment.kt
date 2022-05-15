package com.a10miaomiao.bilimiao.page

import android.R.id.tabs
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.page.home.HomeFragment
import com.a10miaomiao.bilimiao.page.home.PopularFragment
import com.a10miaomiao.bilimiao.page.home.RecommendFragment
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateFragment
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.kodein.di.*
import splitties.experimental.InternalSplittiesApi
import splitties.views.*
import splitties.views.dsl.core.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


class MainFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "bilimiao"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.setting
                title = "设置"
                iconResource = R.drawable.ic_baseline_settings_grey_24
            },
            myMenuItem {
                key = MenuKeys.history
                title = "历史"
                iconResource = R.drawable.ic_history_gray_24dp
                visibility = if (userStore.isLogin()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            },
            myMenuItem {
                key = MenuKeys.download
                title = "下载"
                iconResource = R.drawable.ic_arrow_downward_gray_24dp
            },
            myMenuItem {
                key = MenuKeys.search
                title = "搜索"
                iconResource = R.drawable.ic_search_gray
            },
        )
    }

    override fun onMenuItemClick(view: MenuItemView) {
        super.onMenuItemClick(view)
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        when (view.prop.key) {
            MenuKeys.setting -> {
                nav.navigate(MainNavGraph.action.home_to_setting)
            }
            MenuKeys.history -> {
                nav.navigate(MainNavGraph.action.home_to_history)
            }
            MenuKeys.download -> {
                nav.navigate(MainNavGraph.action.home_to_download)
            }
            MenuKeys.search -> {
                val bsNav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
                bsNav.navigate(MainNavGraph.action.global_to_searchStart)
            }
        }
    }
    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<MainViewModel>(di)

    private val themeDelegate by instance<ThemeDelegate>()

    private var themeId = 0

    private val userStore by instance<UserStore>()

    private val ID_viewPager = View.generateViewId()
    private val ID_tabLayout = View.generateViewId()

    private val fragmentMap: Map<KClass<out Fragment>, () -> Fragment> = mapOf(
        HomeFragment::class to HomeFragment::newFragmentInstance,
        RecommendFragment::class to RecommendFragment::newFragmentInstance,
        PopularFragment::class to PopularFragment::newFragmentInstance,
    )
    private val titleMap: Map<KClass<out Fragment>, String> = mapOf(
        HomeFragment::class to "首页",
        RecommendFragment::class to "推荐",
        PopularFragment::class to "热门",
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (themeDelegate.getThemeResId() != themeId) {
            ui.cleanCacheView()
            themeId = themeDelegate.getThemeResId()
        }
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager2>(ID_viewPager)
        val newNavList = viewModel.readNavList()
        if  (viewModel.navList.isEmpty()) {
            viewModel.navList = newNavList
            val mAdapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun getItemCount() = viewModel.navList.size

                override fun createFragment(position: Int): Fragment {
                    return fragmentMap[viewModel.navList[position]]?.invoke() ?: TemplateFragment()
                }
            }
            viewPager.adapter = mAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titleMap[viewModel.navList[position]] ?: ""
            }.attach()
        } else {
            if (!viewModel.equalsNavList(viewModel.navList, newNavList)) {
                viewModel.navList = newNavList
                viewPager.adapter?.notifyDataSetChanged()
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)
        miaoEffect(userStore.isLogin()) {
            pageConfig.notifyConfigChanged()
        }
        verticalLayout {
            views {
                +tabLayout(ID_tabLayout) {
                    _topPadding = contentInsets.top
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right
                    tabMode = TabLayout.MODE_SCROLLABLE
                }..lParams(matchParent, wrapContent)
                +view<ViewPager2>(ID_viewPager) {
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right
                    offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                    isSaveEnabled = false
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
//                +viewPager(ID_viewPager) {
//                    _leftPadding = contentInsets.left
//                    _rightPadding = contentInsets.right
//                }..lParams(matchParent, matchParent) {
//                    weight = 1f
//                }
            }
        }
    }


}