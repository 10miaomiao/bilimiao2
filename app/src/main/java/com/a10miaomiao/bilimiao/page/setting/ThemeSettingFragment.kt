package com.a10miaomiao.bilimiao.page.setting

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class ThemeSettingFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting.theme"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/theme")
        }
    }

    override val pageConfig = myPageConfig {
        title = "主题设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val statusBarHelper by instance<StatusBarHelper>()
    private val themeDelegate by instance<ThemeDelegate>()
//    private val viewModel by diViewModel<TemplateViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    private val handleItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            ThemeDelegate.setNightMode(requireContext(), p2)
            statusBarHelper.update()
        }
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.getItemOrNull(position)
        if (item is ThemeDelegate.ThemeInfo) {
            themeDelegate.setTheme(item.theme)
            adapter.notifyDataSetChanged()
        }
    }

    val itemUi = miaoBindingItemUi<ThemeDelegate.ThemeInfo> { item, index ->
        shadowLayout {
            layoutParams = ViewGroup.MarginLayoutParams(matchParent, dip(150)).apply {
                margin = config.dividerSize
            }
            padding = config.pagePadding
            setLayoutBackground(config.blockBackgroundColor)
            setCornerRadius(dip(10))
            miaoEffect(item) {
                if (item.theme == themeDelegate.getTheme()) {
                    setStrokeColor(item.color)
                    isEnabled = false
                } else {
                    setStrokeColor(config.blockBackgroundColor)
                    isEnabled = true
                }
            }

            views {
                +frameLayout {
                    apply(ViewStyle.circle)
                    _backgroundColor = item.color
                }..lParams(dip(30), dip(30)) {
                    bottomMargin = dip(10)
                    gravity = gravityCenter
                }
                +textView {
                    _text = item.name
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

            val mAdapter = _miaoAdapter(
                items = themeDelegate.themeList,
                itemUi = itemUi,
            ) {
                setOnItemClickListener(handleItemClick)
            }

            headerViews(mAdapter) {
                +horizontalLayout {
                    horizontalPadding = config.pagePadding
                    verticalPadding = config.dividerSize

                    views {
                        +textView{
                            _text = "夜间模式："
                        }
                        +spinner {
                            miaoEffect(null) {
                                val mAdapter = ArrayAdapter<String>(context, R.layout.simple_spinner_item
                                    , arrayOf("跟随系统", "关闭", "打开"))
                                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                adapter = mAdapter
                                setSelection(ThemeDelegate.getNightMode(requireContext()))
                                onItemSelectedListener = handleItemSelectedListener
                            }
                        }
                    }

                }
            }

        }
    }

}