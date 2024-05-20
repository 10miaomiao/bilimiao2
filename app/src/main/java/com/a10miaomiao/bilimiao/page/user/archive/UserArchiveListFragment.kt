package com.a10miaomiao.bilimiao.page.user.archive

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.connectStore
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.currentOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.openSearch
import com.a10miaomiao.bilimiao.comm.tabLayout
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.wrapInViewPager2Container
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent

class UserArchiveListFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.archive"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.name) {
                type = NavType.StringType
                nullable = false
            }
        }
        fun createArguments(
            id: String,
            name: String
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
            )
        }

        private val ID_viewPager = View.generateViewId()
        private val ID_tabLayout = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        var searchTitle = "搜索"
        title = "${viewModel.name}\n的\n投稿列表"
        menus = mutableListOf(
            myMenuItem {
                key = MenuKeys.search
                title = searchTitle
                iconResource = R.drawable.ic_search_gray
            }
        ).apply {
            viewModel.curFragment?.let { addAll(it.menus) }
        }
        search = SearchConfigInfo(
            name = "搜索投稿列表",
            keyword = "",
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            MenuKeys.search -> {
                requireActivity().openSearch(view)
            }
            else -> {
                viewModel.curFragment?.onMenuItemClick(view, menuItem)
            }
        }
    }

    override fun onSearchSelfPage(context: Context, keyword: String) {
        findNavController().currentOrSelf()
            .navigate(
                UserSearchArchiveListFragment.actionId,
                UserSearchArchiveListFragment.createArguments(
                    viewModel.id,
                    viewModel.name ?: "",
                    keyword,
                )
            )
    }

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@UserArchiveListFragment }
    }

    private val viewModel by diViewModel<UserArchiveListViewModel>(di)

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager2>(ID_viewPager)
        if (viewPager.adapter == null) {
            val mAdapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun getItemCount() = viewModel.fragments.size
                override fun createFragment(position: Int): Fragment {
                    return viewModel.fragments[position]
                }
            }
            viewPager.adapter = mAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = viewModel.fragments[position].title
            }.attach()
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.position = position
                    pageConfig.notifyConfigChanged()
                }
            })
        }

    }

    @OptIn(InternalSplittiesApi::class)
    @SuppressLint("ResourceType")
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout {
            _bottomPadding = contentInsets.bottom

            views {
                +verticalLayout {
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
                        }.wrapInViewPager2Container {
                        }..lParams(matchParent, matchParent) {
                            weight = 1f
                        }
                    }
                }
            }
        }

    }

}