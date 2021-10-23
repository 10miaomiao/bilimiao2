package com.a10miaomiao.bilimiao.page

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager

import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.padding
import splitties.views.verticalPadding
import kotlin.contracts.ExperimentalContracts


class MainFragment : Fragment(), DIAware {

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@MainFragment }
    }

    private val viewModel by diViewModel<MainViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        DebugMiao.log("MainFragment.onCreateView")
        return ui.root
    }

    val regionItemUi = miaoBindingItemUi<RegionInfo> { item, index ->
        verticalLayout {
            setBackgroundResource(config.selectableItemBackground)
            gravity = Gravity.CENTER
            verticalPadding = dip(10)

            views {
                +imageView {
                    _netword(item.logo)
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
        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor
            padding = config.pagePadding

            views {
                +recyclerView {
                    layoutManager = GridLayoutManager(activity, 5)
                    isNestedScrollingEnabled = false
                    backgroundColor = config.blockBackgroundColor
                    apply(ViewStyle.roundRect(dip(10)))

                    _miaoAdapter(
                        viewModel.regions,
                        regionItemUi,
                        onItemClick = regionItemClick
                    )
                }..lParams {
                    width = matchParent
//                    topMargin = config.dividerSize
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}