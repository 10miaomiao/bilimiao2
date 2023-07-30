package com.a10miaomiao.bilimiao.page.home

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
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.PageRoute
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.MiaoEncryptDecrypt
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewContent
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.region.RegionFragment
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.video.comment.ReplyDetailParam
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.wrapInLimitedFrameLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import java.nio.charset.Charset

class HomeFragment : Fragment(), DIAware {

    companion object {
        fun newFragmentInstance(): HomeFragment {
            val fragment = HomeFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<HomeViewModel>(di)
    private val userStore by instance<UserStore>()

    private val themeDelegate by instance<ThemeDelegate>()

    private var themeId = 0

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
        viewModel.randomTitle()
    }

    val handleHeaderClick = View.OnClickListener{
        val userInfo = viewModel.userStore.state.info
        if (userInfo != null) {
            // 跳转个人中心
            val nav = Navigation.findNavController(it)
            val args = UserFragment.createArguments(userInfo.mid.toString())
            nav.navigate(UserFragment.actionId, args)
        } else {
            // 跳转登录
            val nav = Navigation.findNavController(it)
            val url = "bilimiao://auth/login"
            nav.navigateToCompose(url)
        }
    }

    val handleHeaderLongClick = View.OnLongClickListener{
        if (viewModel.userStore.state.info == null) {
            // 跳转登录
            val nav = Navigation.findNavController(it)
            val url = "bilimiao://auth/login"
            nav.navigateToCompose(url)
            true
        } else {
            false
        }
    }

    val handleTimeSettingClick = View.OnClickListener {
        val nav = requireActivity().findNavController(com.a10miaomiao.bilimiao.R.id.nav_bottom_sheet_fragment)
        val url = "bilimiao://time/setting"
        nav.navigateToCompose(url)
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
                                .loadImageUrl(item.logo!!)
                                .override(dip(24), dip(24))
                                .into(this)
                        }
                    }
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
        val regions = viewModel.regionStore.state.regions
        val nav = Navigation.findNavController(view)
        val args = RegionFragment.createArguments(regions[i])
        nav.navigate(RegionFragment.actionId, args)
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
                +flexboxLayout {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
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
                    val regions = viewModel.regionStore.state.regions
                    _miaoAdapter(
                        regions,
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
                    imageResource = com.a10miaomiao.bilimiao.R.drawable.home_header_img
                }..lParams(matchParent, dip(120))

                val userInfo = viewModel.userStore.state.info
                // 应用信息
                +horizontalLayout {
                    padding = dip(10)

                    views {
                        +imageView {
                            miaoEffect(userInfo) {
                                if (it == null) {
                                    Glide.with(context)
                                        .load(com.a10miaomiao.bilimiao.R.mipmap.ic_launcher)
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
                                    text = "不知道写什么好，长按试试(o゜▽゜)o☆"
                                    setTextColor(config.foregroundAlpha45Color)
                                    _show = userInfo == null
                                }

                                +flexboxLayout {
                                    flexDirection = FlexDirection.ROW
                                    flexWrap = FlexWrap.WRAP
                                    _show = userInfo != null

                                    views {
                                        +textView {
                                            _text = "B币:${userInfo?.bcoin}"
                                            setTextColor(config.foregroundAlpha45Color)
                                        }
                                        +space()..lParams(dip(20))
                                        +textView {
                                            _text = "硬币:${userInfo?.coin}"
                                            setTextColor(config.foregroundAlpha45Color)
                                        }
                                        +space()..lParams(dip(20))
                                        +textView {
                                            _text = "UID:${userInfo?.mid}"
                                            setTextColor(config.foregroundAlpha45Color)
                                        }
                                    }
                                }..lParams {
                                    width = matchParent
                                }
                            }
                        }..lParams {
                            height = wrapContent
                            width = matchParent
                        }
                    }
                }..lParams {
                    topMargin = dip(60)
                    width = matchParent
                    height = wrapContent
                }
            }

        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, viewModel.timeSettingStore)
        connectStore(viewLifecycleOwner, viewModel.userStore)
        connectStore(viewLifecycleOwner, viewModel.regionStore)
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = config.pagePadding
            _bottomPadding = contentInsets.bottom

            views {
                +headerView()..lParams {
                    width = matchParent
                    height = wrapContent
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
//                        val nav = it.findNavController()
//                        nav.navigateToCompose(PageRoute.Auth.qr_login.url())
//                        nav.navigate(MainNavGraph.action.global_to_compose, bundleOf(
//                            MainNavArgs.url to PageRoute.Auth.login.url()
//                        ))
//                        nav.navigate(MainNavGraph.action.global_to_compose, bundleOf(
//                            MainNavArgs.url to "bilimiao://setting/proxy"
//                        ))
//
//                        【不當哥哥了！（僅限港澳台地區）】https://www.bilibili.com/bangumi/play/ep719017?vd_source=2bcb4ee461719ac7def0c91f553096a3
//                         https://www.bilibili.com/bangumi/play/ss44493
//                        nav.navigate(MainNavGraph.action.global_to_bangumiDetail, bundleOf(
//                            MainNavArgs.id to "44493"
//                        ))
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