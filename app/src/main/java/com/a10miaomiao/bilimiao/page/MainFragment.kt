package com.a10miaomiao.bilimiao.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import cn.a10miaomiao.miao.binding.android.view.*

import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.view.loadPic
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.search.SearchResultViewModel
import com.a10miaomiao.bilimiao.page.search.result.VideoResultFragment
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.a10miaomiao.bilimiao.widget.wrapInLimitedFrameLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView


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

    private val userStore by instance<UserStore>()

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
                override fun getPageTitle(position: Int) = viewModel.titles[position]
            }
            viewPager.adapter = mAdapter
            tabLayout.setTabsFromPagerAdapter(mAdapter)
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            tabLayout.setupWithViewPager(viewPager)
        }
    }

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