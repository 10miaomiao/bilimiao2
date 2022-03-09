package com.a10miaomiao.bilimiao.page.filter

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.comm.wrapInNestedScrollView
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

    override val pageConfig = myPageConfig {
        title = "时光姬-屏蔽管理"
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
        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
        nav.navigate(Uri.parse("bilimiao://filter/word/add"))
    }

    val itemUi = miaoBindingItemUi<String> { item, index ->
        frameLayout {
            apply(ViewStyle.roundRect(dip(5)))
            backgroundColor = config.windowBackgroundColor
            layoutParams = lParams {
                rightMargin =  dip(8)
                bottomMargin = dip(5)
            }

            views {
                +textView {
                    padding = dip(5)

                    _text = item
                }
            }
        }
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
                                itemUi = itemUi
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
            }
        }.wrapInNestedScrollView()
    }

}