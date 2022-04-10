package com.a10miaomiao.bilimiao.page.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class ThemeSettingFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "主题设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val a = 0;
//    private val viewModel by diViewModel<TemplateViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    val itemUi = miaoBindingItemUi<String> { item, index ->
        shadowLayout {
            layoutParams = ViewGroup.MarginLayoutParams(matchParent, dip(150)).apply {
                margin = config.dividerSize
            }
            padding = config.pagePadding
            backgroundColor = config.blockBackgroundColor
            setCornerRadius(dip(10))
            miaoEffect(index == 0) {
                if (it) {
                    setStrokeColor(config.themeColor)
                    isEnabled = false
                } else {
                    setStrokeColor(config.blockBackgroundColor)
                    isEnabled = true
                }
            }

            views {
                +frameLayout {
                    apply(ViewStyle.circle)
                    backgroundColor = config.themeColor
                }..lParams(dip(30), dip(30)) {
                    bottomMargin = dip(10)
                    gravity = gravityCenter
                }
                +textView {
                    _text = item
                }..lParams(wrapContent, wrapContent) {
                    bottomMargin = dip(25)
                    gravity = gravityBottomCenter
                }
            }
        }
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom

            _miaoLayoutManage(GridAutofitLayoutManager(
                requireContext(),
                dip(120)
            ))

            _miaoAdapter(
                items = mutableListOf("少女粉", "胖次蓝"),
                itemUi = itemUi,
            ) {
//                setOnItemClickListener(handleEpisodeItemClick)
            }
        }
    }

}