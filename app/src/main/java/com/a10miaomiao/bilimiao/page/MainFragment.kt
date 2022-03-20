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
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import cn.a10miaomiao.miao.binding.android.view.*

import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
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
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.a10miaomiao.bilimiao.widget.wrapInLimitedFrameLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
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
                visibility = if (viewModel.userStore.isLogin()) {
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
                toast("重新装修中")
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<MainViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadRegionData()
        viewModel.randomTitle()
    }

    val handleHeaderClick = View.OnClickListener{
        val userInfo = viewModel.userStore.state.info
        if (userInfo != null) {
            val nav = findNavController(it)
            val args = bundleOf(
                MainNavGraph.args.id to userInfo.mid.toString()
            )
            nav.navigate(MainNavGraph.action.home_to_user, args)
        }
    }

    val handleHeaderLongClick = View.OnLongClickListener{
        if (viewModel.userStore.state.info == null) {
            val nav = findNavController(it)
            nav.navigate(MainNavGraph.action.home_to_h5Login)
            true
        } else {
            false
        }
    }

    val handleTimeSettingClick = View.OnClickListener {
        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
        nav.navigate(Uri.parse("bilimiao://time/setting"))
    }

    val handleAdClick = View.OnClickListener {
        viewModel.adInfo?.let {
            //普通链接 调用浏览器
            var intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it.link.url)
            requireActivity().startActivity(intent)
        }
    }

    val regionItemUi = miaoBindingItemUi<RegionInfo> { item, index ->
        verticalLayout {
            setBackgroundResource(config.selectableItemBackground)
            gravity = Gravity.CENTER
            verticalPadding = dip(10)

            views {
                +imageView {
                    miaoEffect(listOf(item.icon, item.logo)) {
                        if (item.icon != null) {
                            Glide.with(context)
                                .load(item.icon)
                                .override(dip(24), dip(24))
                                .into(this)
                        } else if (item.logo != null) {
                            Glide.with(context)
                                .loadPic(item.logo!!)
                                .override(dip(24), dip(24))
                                .into(this)
                        }
                    }
                    _network(item.logo)
                }..lParams(dip(24), dip(24))

                +textView {
                    _text = item.name
                    gravity = Gravity.CENTER
                    setTextColor(config.foregroundAlpha45Color)
                }
            }
        }
    }

    val regionItemClick = OnItemClickListener { baseQuickAdapter, view, i  ->
        val nav = findNavController(view)
        val args = bundleOf(
            MainNavGraph.args.region to viewModel.regions[i]
        )
        nav.navigate(MainNavGraph.action.home_to_region, args)
    }

    fun MiaoUI.timeView(): View {
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            apply(ViewStyle.roundRect(dip(10)))

            views {
                +textView {
                    _text = viewModel.title
                    textSize = 18f
                    setTextColor(config.foregroundColor)
                }..lParams {
                    horizontalMargin = dip(10)
                    topMargin = dip(10)
                    bottomMargin = dip(5)
                }
                +horizontalLayout {
                    views {
                        +textView {
                            _text = "当前时间线：" + viewModel.getTimeText()
                            setTextColor(config.foregroundAlpha45Color)
                        }
                        +textView {
                            setTextColor(config.themeColor)
                            setBackgroundResource(config.selectableItemBackgroundBorderless)
                            setOnClickListener(handleTimeSettingClick)
                            text = "去设置>"
                        }..lParams {
                            leftMargin = dip(5)
                        }
                    }
                }..lParams {
                    horizontalMargin = dip(10)
                    bottomMargin = dip(5)
                }

                +recyclerView {
                    layoutManager = GridAutofitLayoutManager(requireContext(), dip(80))
                    isNestedScrollingEnabled = false

                    _miaoAdapter(
                        viewModel.regions,
                        regionItemUi,
                    ) {
                        setOnItemClickListener(regionItemClick)
                    }
                }..lParams {
                    width = matchParent
                }
            }
        }
    }

    fun MiaoUI.adView(): View {
        return horizontalLayout {
            backgroundColor = config.blockBackgroundColor
            apply(ViewStyle.roundRect(dip(10)))
            padding = config.dividerSize

            val adInfo = viewModel.adInfo
            _show = adInfo?.isShow == true

            views {
                +textView {
                    _text = adInfo?.title ?: ""
                    setTextColor(config.foregroundAlpha45Color)
                }..lParams {
                    width = matchParent
                    weight = 1f
                }

                +textView {
                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                    textColorResource = config.themeColorResource
                    _text = adInfo?.link?.text ?: ""
                    setOnClickListener(handleAdClick)
                }
            }
        }
    }

    fun MiaoUI.headerView(): View {
        return frameLayout {
            backgroundColor = config.blockBackgroundColor
            apply(ViewStyle.roundRect(dip(10)))
            setOnClickListener(handleHeaderClick)
            setOnLongClickListener(handleHeaderLongClick)

            views {
                // 背景图片
                +imageView {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    imageResource = R.drawable.home_header_img
                }..lParams(matchParent, matchParent)

                val userInfo = viewModel.userStore.state.info
                // 应用信息
                +horizontalLayout {
                    padding = dip(10)

                    views {
                        +imageView {
                            miaoEffect(userInfo) {
                                if (it == null) {
                                    Glide.with(context)
                                        .load(R.mipmap.ic_launcher)
                                        .circleCrop()
                                        .into(this)
                                } else {
                                    Glide.with(context)
                                        .loadImageUrl(it.face)
                                        .circleCrop()
                                        .into(this)
                                }
                            }
                        }..lParams {
                            height = dip(50)
                            width = dip(50)
                            rightMargin = dip(10)
                        }

                        +verticalLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            views {
                                +textView {
                                    _text = userInfo?.name ?: "bilimiao"
                                    setTextColor(config.foregroundColor)
                                    textSize = 16f
                                }..lParams {
                                    width = matchParent
                                    bottomMargin = dip(5)
                                }

                                +textView {
                                    _text = userInfo?.run {
                                        "B币:${bcoin}     硬币:${coin}     UID:${mid}"
                                    } ?: "不知道写什么好，长按试试(o゜▽゜)o☆"
                                    setTextColor(config.foregroundAlpha45Color)
                                }..lParams {
                                    width = matchParent
                                }
                            }
                        }..lParams {
                            height = matchParent
                            width = matchParent
                        }
                    }
                }..lParams {
                    width = matchParent
                    height = wrapContent
                    gravity = Gravity.BOTTOM
                }
            }

        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, viewModel.timeSettingStore)
        connectStore(viewLifecycleOwner, viewModel.userStore)
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        miaoEffect(listOf(viewModel.userStore.state.info)) {
            pageConfig.notifyConfigChanged()
        }

        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom

            views {
                +headerView()..lParams {
                    width = matchParent
                    height = dip(150)
                    bottomMargin = config.dividerSize
                }
                +adView()..lParams {
                    width = matchParent
                    bottomMargin = config.dividerSize
                }
                +timeView()..lParams {
                    width = matchParent
                    bottomMargin = config.dividerSize
                }

//                +button {
//                    text = "测试"
//                    setOnClickListener {
//                        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
//                        nav.navigate(Uri.parse("bilimiao://time/setting"))
//                    }
//                }
            }

        }.wrapInLimitedFrameLayout {
            maxWidth = config.containerWidth
        }.wrapInNestedScrollView (
            height = ViewGroup.LayoutParams.MATCH_PARENT,
            gravity = Gravity.CENTER_HORIZONTAL,
        )
    }


}