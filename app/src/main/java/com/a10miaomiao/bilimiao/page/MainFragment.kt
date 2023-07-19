package com.a10miaomiao.bilimiao.page

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.dsl.addOnDoubleClickTabListener
import com.a10miaomiao.bilimiao.comm.mypage.*
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.navigation.openSearchDrawer
import com.a10miaomiao.bilimiao.comm.recycler.RecyclerViewFragment
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.page.home.*
import com.a10miaomiao.bilimiao.page.user.HistoryFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.SettingFragment
import com.a10miaomiao.bilimiao.template.TemplateFragment
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.wrapInViewPager2Container
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.*
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.*
import kotlin.reflect.KClass


class MainFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "main"
    }

    private var pageTitle = "bilimiao\n-\n首页"

    override val pageConfig = myPageConfig {
        title = pageTitle
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

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        when (menuItem.key) {
            MenuKeys.setting -> {
                nav.navigate(SettingFragment.actionId)
            }
            MenuKeys.history -> {
                nav.navigate(HistoryFragment.actionId)
            }
            MenuKeys.download -> {
                nav.navigateToCompose("bilimiao://download/list")
            }
            MenuKeys.search -> {
//                val bsNav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
//                bsNav.navigate(SearchStartFragment.actionId)
                scaffoldApp.openSearchDrawer()
            }
        }
    }
    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<MainViewModel>(di)

    private val themeDelegate by instance<ThemeDelegate>()
    private val playerDelegate by instance<BasePlayerDelegate>()
    private val scaffoldApp by lazy { requireActivity().getScaffoldView() }

    private var themeId = 0

    private val userStore by instance<UserStore>()

    private val ID_viewPager = View.generateViewId()
    private val ID_tabLayout = View.generateViewId()
    private val ID_space = View.generateViewId()

    private var backKeyPressedTimes = 0L

    private val fragmentMap: Map<KClass<out Fragment>, () -> Fragment> = mapOf(
        HomeFragment::class to HomeFragment::newFragmentInstance,
        RecommendFragment::class to RecommendFragment::newFragmentInstance,
        PopularFragment::class to PopularFragment::newFragmentInstance,
        DynamicFragment::class to DynamicFragment::newFragmentInstance,
    )
    private val titleMap: Map<KClass<out Fragment>, String> = mapOf(
        HomeFragment::class to "首页",
        RecommendFragment::class to "推荐",
        PopularFragment::class to "热门",
        DynamicFragment::class to "动态"
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
//        showPrivacyDialog()
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val now = System.currentTimeMillis()
                    if (scaffoldApp.showPlayer) {
                        if (now - backKeyPressedTimes > 2000) {
                            PopTip.show("再按一次退出播放")
                            backKeyPressedTimes = now
                        } else {
                            playerDelegate.closePlayer()
                            backKeyPressedTimes = 0
                        }
                    } else {
                        if (now - backKeyPressedTimes > 2000) {
                            PopTip.show("再按一次退出bilimiao")
                            backKeyPressedTimes = now
                        } else {
                            requireActivity().finish()
                        }
                    }
                }
            })
    }

    private fun showPrivacyDialog() {
        val appInfo = requireActivity().packageManager.getApplicationInfo(
            requireActivity().application.packageName,
            PackageManager.GET_META_DATA
        )
        if (appInfo.metaData.getString("BaiduMobAd_CHANNEL") != "Coolapk") {
            return
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        if (sp.getBoolean("is_approve_privacy", false)) {
            return
        }
        val dialog = MaterialAlertDialogBuilder(requireActivity()).apply {
            setTitle("温馨提示")
            setMessage("根据相关政策法规，你需要先阅读并同意《隐私协议》才能使用本软件")
            setCancelable(false)
            setNeutralButton("阅读《隐私协议》", null)
            setPositiveButton("同意"){ dialog, _ ->
                sp.edit()
                    .putBoolean("is_approve_privacy", true)
                    .apply()
                dialog.dismiss()
            }
            setNegativeButton("拒绝") { _, _ ->
                requireActivity().finish()
            }
        }.show()
        // 手动设置按钮点击事件，可阻止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://10miaomiao.cn/bilimiao/privacy.html")
            requireActivity().startActivity(intent)
        }
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager2>(ID_viewPager)
        val space = view.findViewById<Space>(ID_space)
        val newNavList = viewModel.readNavList()
        if (newNavList.size > 1) {
            space.visibility = View.GONE
            tabLayout.visibility = View.VISIBLE
        } else {
            space.visibility = View.VISIBLE
            tabLayout.visibility = View.GONE
        }
        if  (viewPager.adapter == null) {
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
            tabLayout.addOnDoubleClickTabListener {
                val itemId = mAdapter.getItemId(it.position)
                childFragmentManager.findFragmentByTag("f$itemId")?.let { currentFragment ->
                    if (currentFragment is RecyclerViewFragment) {
                        currentFragment.toListTop()
                    }
                }
            }
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val title = titleMap[viewModel.navList[position]] ?: ""
                    pageTitle = "bilimiao\n-\n$title"
                    pageConfig.notifyConfigChanged()
                }
            })
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
                +space(ID_space) {
                    visibility = View.GONE
                }..lParams {
                    _height = contentInsets.top
                }
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