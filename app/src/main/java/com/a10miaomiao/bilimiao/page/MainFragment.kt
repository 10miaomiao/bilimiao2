package com.a10miaomiao.bilimiao.page

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import cn.a10miaomiao.miao.binding.android.view._topMargin
import cn.a10miaomiao.miao.binding.android.view._topPadding

import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.a10miaomiao.bilimiao.widget.comm.getAppBarView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.wrapInLimitedFrameLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityCenter
import splitties.views.padding
import splitties.views.verticalPadding


class MainFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "bilimiao"
        menus = listOf(
            myMenuItem {
                title = "更多"
            }
        )
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<MainViewModel>(di)

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
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    val handleTimeSettingClick = View.OnClickListener {
        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
        nav.navigate(Uri.parse("bilimiao://time/setting"))
    }

    val regionItemUi = miaoBindingItemUi<RegionInfo> { item, index ->
        verticalLayout {
            setBackgroundResource(config.selectableItemBackground)
            gravity = Gravity.CENTER
            verticalPadding = dip(10)

            views {
                +imageView {
                    _network(item.logo)
                }..lParams(dip(24), dip(24))

                +textView {
                    _text = item.name
                    gravity = Gravity.CENTER
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

    val ui = miaoBindingUi {
        val contentInsets = windowStore.state.contentInsets

        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor
            padding = config.pagePadding

            views {
                +timeView()..lParams {
                    width = matchParent
                    _topMargin = contentInsets.top
                }

//                +button {
//                    text = "测试"
//                    setOnClickListener {
//                        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
//                        nav.navigate(Uri.parse("bilimiao://time/setting"))
//                    }
//                }
            }


                // 广告通知
//                linearLayout {
//                    visibility = View.GONE
//                    backgroundColor = config.blockBackgroundColor
//                    padding = config.dividerSize
//
//                    val observeAdInfo = viewModel.adInfo.observeNotNull()
//
//                    observeAdInfo {
//                        visibility = if (it?.isShow == true) View.VISIBLE else View.GONE
//                    }
//
//                    textView {
//                        observeAdInfo { text = it!!.title }
//                    }.lparams {
//                        width = matchParent
//                        weight = 1f
//                    }
//
//                    textView {
//                        selectableItemBackgroundBorderless()
//                        textColorResource = attr(android.R.attr.colorAccent)
//                        observeAdInfo { text = it!!.link.text }
//                    }
//
//                    setOnClickListener { viewModel.openAd() }
//                }.lparams {
//                    width = matchParent
//                    topMargin = config.dividerSize
//                }


        }.wrapInLimitedFrameLayout {
            maxWidth = config.containerWidth
        }.wrapInNestedScrollView (
            height = ViewGroup.LayoutParams.MATCH_PARENT,
            gravity = Gravity.CENTER_HORIZONTAL,
        )
    }


}