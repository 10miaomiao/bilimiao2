package com.a10miaomiao.bilimiao.page.filter

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.db.FilterUpperDB
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.flow.FlowLayoutManager
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.padding
import splitties.views.textColorResource

class FilterListFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "filter.list"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://filter/list")
        }
    }

    override val pageConfig = myPageConfig {
        title = "时光姬\n-\n屏蔽管理"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()
    private val filterStore by instance<FilterStore>()

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
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
            filterStore.connectUi(ui)
        }
    }

    val handleToWordClick = View.OnClickListener {
        val nav = Navigation.findNavController(it)
        nav.navigate(FilterWordListFragment.actionId)
    }

    val handleToUpperClick = View.OnClickListener {
        val nav = Navigation.findNavController(it)
        nav.navigate(FilterUpperListFragment.actionId)
    }

    fun MiaoUI.tagView(
        text: String,
    ): View {
        return frameLayout {
            apply(ViewStyle.roundRect(dip(5)))
            backgroundColor = config.windowBackgroundColor
            layoutParams = lParams {
                rightMargin =  dip(8)
                bottomMargin = dip(5)
            }

            views {
                +textView {
                    padding = dip(5)

                    _text = text
                }
            }
        }
    }

    val wordItemUi = miaoBindingItemUi<String> { item, index ->
        tagView(item)
    }

    val upperItemUi = miaoBindingItemUi<FilterUpperDB.Upper> { item, index ->
        tagView(item.name)
    }

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        verticalLayout {
            _leftPadding = insets.left
            _topPadding = insets.top
            _rightPadding = insets.right
            _bottomPadding = insets.bottom
            layoutParams = lParams(matchParent, matchParent)

            views {
                +verticalLayout {
                    apply(ViewStyle.block)

                    views {
                        +textView {
                            text = "屏蔽标题关键字："
                        }

                        +recyclerView {
                            isNestedScrollingEnabled = false
                            _show = filterStore.filterWordCount != 0
                            _miaoLayoutManage(
                                FlowLayoutManager()
                            )
                            _miaoAdapter(
                                items = filterStore.state.filterWordList,
                                itemUi = wordItemUi
                            ) {
                            }
                        }..lParams {
                            verticalMargin = dip(10)
                        }

                        +textView {
                            text = "空空如也"
                            _show = filterStore.filterWordCount == 0
                        }..lParams {
                            gravity = Gravity.CENTER
                            verticalMargin = dip(10)
                        }

                        +textView {
                            text = "去设置"
                            textColorResource = config.themeColorResource
                            setBackgroundResource(config.selectableItemBackground)
                            setOnClickListener(handleToWordClick)
                        }..lParams {
                            gravity = Gravity.RIGHT
                        }
                    }

                }..lParams {
                    width = matchParent
                    height = wrapContent
                    verticalMargin = config.dividerSize
                }

                +verticalLayout {
                    apply(ViewStyle.block)

                    views {
                        +textView {
                            text = "屏蔽up主："
                        }

                        +recyclerView {
                            isNestedScrollingEnabled = false
                            _show = filterStore.filterWordCount != 0
                            _miaoLayoutManage(
                                FlowLayoutManager()
                            )
                            _miaoAdapter(
                                items = filterStore.state.filterUpperList,
                                itemUi = upperItemUi
                            ) {
                            }
                        }..lParams {
                            verticalMargin = dip(10)
                        }

                        +textView {
                            text = "空空如也"
                            _show = filterStore.filterUpperCount == 0
                        }..lParams {
                            gravity = Gravity.CENTER
                            verticalMargin = dip(10)
                        }

                        +textView {
                            text = "去设置"
                            textColorResource = config.themeColorResource
                            setBackgroundResource(config.selectableItemBackground)
                            setOnClickListener(handleToUpperClick)
                        }..lParams {
                            gravity = Gravity.RIGHT
                        }
                    }

                }..lParams {
                    width = matchParent
                    height = wrapContent
                    verticalMargin = config.dividerSize
                }
            }
        }.wrapInNestedScrollView()
    }

}