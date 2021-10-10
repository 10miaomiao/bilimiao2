package com.a10miaomiao.bilimiao2.page

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao2.comm.MiaoUI
import com.a10miaomiao.bilimiao2.comm.diViewModel
import com.a10miaomiao.bilimiao2.comm.miaoBindingUi
import com.a10miaomiao.bilimiao2.comm.recycler.MiaoBindingAdapter
import com.a10miaomiao.bilimiao2.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao2.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao2.comm.views
import com.bumptech.glide.Glide
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.onClick
import splitties.views.topPadding
import splitties.views.verticalPadding

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
        return ui.root
    }

    val itemUi = miaoBindingItemUi<String> { item, index ->
        verticalLayout {

            views {
                +textView {
                    _text = item
                }
            }
        }
    }

    val regionItemUi = miaoBindingItemUi<RegionInfo> { item, index ->
        verticalLayout {
//            selectableItemBackground()
            gravity = Gravity.CENTER
            verticalPadding = dip(10)

            views {
                +imageView {
                    miaoEffect(item.icon) {
                        if (item.icon != null) {
                            Glide.with(context)
                                .load(item.icon)
                                .override(dip(24), dip(24))
                                .into(this)
                        }
                    }
                    miaoEffect(item.logo) {
                        if (item.logo != null) {
                            Glide.with(context)
                                .load(item.logo!!)
                                .override(dip(24), dip(24))
                                .into(this)
                        }
                    }
                }..lParams(dip(24), dip(24))

                +textView {
                    _text = item.name
                    gravity = Gravity.CENTER
                }
            }


        }
    }

    val ui = miaoBindingUi {
        verticalLayout {
            layoutParams = lParams(matchParent, matchParent)

            views {
                +recyclerView {
                    layoutManager = GridLayoutManager(activity, 5)
                    isNestedScrollingEnabled = false
//                    backgroundColor = config.blockBackgroundColor

                    _miaoAdapter(
                        viewModel.regions,
                        itemUi = regionItemUi
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


        }.wrapInScrollView {
            layoutParams = lParams(matchParent, matchParent)
//                backgroundColor = config.windowBackgroundColor
//            views {
//
//            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}