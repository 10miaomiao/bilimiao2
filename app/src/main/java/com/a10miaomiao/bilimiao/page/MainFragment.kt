package com.a10miaomiao.bilimiao.page

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation.findNavController
import cn.a10miaomiao.miao.binding.android.view._topMargin
import cn.a10miaomiao.miao.binding.android.view._topPadding

import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.getAppBarView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
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
import splitties.views.padding
import splitties.views.verticalPadding


class MainFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "bilimiao2"
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

    val ui = miaoBindingUi {
        val contentInsets = windowStore.state.contentInsets
        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor
            padding = config.pagePadding

            views {
                +recyclerView {
                    layoutManager = GridAutofitLayoutManager(requireContext(), dip(100))
                    isNestedScrollingEnabled = false
                    backgroundColor = config.blockBackgroundColor
                    apply(ViewStyle.roundRect(dip(10)))

                    _miaoAdapter(
                        viewModel.regions,
                        regionItemUi,
                    ) {
                        setOnItemClickListener(regionItemClick)
                    }
                }..lParams {
                    width = matchParent
                    _topMargin = contentInsets.top
                }

                +button {
                    text = "测试"
                    setOnClickListener {
                        val app = requireActivity().getScaffoldView()
                        val bottomSheetBehavior = app.bottomSheetBehavior
//                        behavior.peekHeight = peekHeight
//                        supportFragmentManager.beginTransaction()
//                            .replace(R.id.bottomSheettContainer, fragment)
//                            .commit()
//                        bottomSheetFragment = fragment
                        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED //设置为展开状态
                        bottomSheetBehavior?.skipCollapsed = true
                        DebugMiao.log(bottomSheetBehavior?.state)
                    }
                }
            }

                // 时间线时间显示
//                linearLayout {
//                    selectableItemBackground()
//                    backgroundColor = config.blockBackgroundColor
//                    padding = config.dividerSize
//                    textView {
//                        observeTime {
//                            text = "当前时间线：" + timeSettingStore.value
//                        }
//                    }
//                    setOnClickListener {
//                        startFragment(TimeSettingFragment())
//                    }
//                }.lparams {
//                    width = matchParent
//                    topMargin = config.dividerSize
//                }

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


        }.wrapInNestedScrollView (height = ViewGroup.LayoutParams.MATCH_PARENT)
    }

}