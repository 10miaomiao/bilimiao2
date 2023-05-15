package com.a10miaomiao.bilimiao.page.filter

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.db.FilterUpperDB
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class FilterUpperListFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "filter.upper.list"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://filter/upper/list")
        }
    }

    override val pageConfig = myPageConfig {
        title = "屏蔽管理\n-\nUP主"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.delete
                title = "删除已选"
            },
            myMenuItem {
                key = MenuKeys.select
                title = if (viewModel.isSelectAll) {
                    "全不选"
                } else {
                    "全选"
                }
            }
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when(menuItem.key) {
            MenuKeys.delete -> {
                viewModel.deleteSelected()
            }
            MenuKeys.select -> {
                if (viewModel.isSelectAll) {
                    viewModel.unSelectAll()
                } else {
                    viewModel.selectAll()
                }
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<FilterUpperListViewModel>(di)

    private val windowStore by instance<WindowStore>()
    private val filterStore by instance<FilterStore>()

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

    val handleCheckedChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val mid = buttonView.tag
        if (mid is Long) {
            viewModel.selectedChange(mid, isChecked)
        }
    }

    val itemUi = miaoBindingItemUi<FilterUpperDB.Upper> { item, index ->
        horizontalLayout {
            horizontalPadding = config.pagePadding
            topPadding = config.dividerSize
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(config.selectableItemBackground)
            layoutParams = lParams(matchParent, wrapContent)

            views {
                +checkBox {
                    rightPadding = config.dividerSize
                    val isSelected = viewModel.selectedList.contains(item.mid)
                    miaoEffect(listOf(item.mid, isSelected)) {
                        tag = item.mid
                        isChecked = isSelected
                    }
                    setOnCheckedChangeListener(handleCheckedChange)
                }
                +textView {
                    textSize = 20f
                    _text = "${item.name}(uid:${item.mid})"
                }..lParams { weight = 1f }
            }

        }
    }

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        miaoEffect(viewModel.isSelectAll) {
            pageConfig.notifyConfigChanged()
        }
        recyclerView {
            _leftPadding = insets.left
            _rightPadding = insets.right

//            val divide = RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 2, Color.BLUE)
//            addItemDecoration(divide)
            _miaoLayoutManage(LinearLayoutManager(requireContext()))

            val mAdapter = _miaoAdapter(
                itemUi = itemUi,
                items = viewModel.list,
                isForceUpdate = true,
            )
            headerViews(mAdapter) {
                +frameLayout {
                    _topPadding = insets.top
                }
            }
            footerViews(mAdapter) {
                +verticalLayout {
                    gravity = Gravity.CENTER

                    _show = viewModel.count == 0
                    views {
                        +textView {
                            text = "空空如也"
                            gravity = Gravity.CENTER
                        }
                        +textView {
                            text = "似乎没有讨厌的人"
                            gravity = Gravity.CENTER
                        }
                    }
                }..lParams(matchParent, dip(400))
                +frameLayout {
                    _topPadding = insets.bottom
                }
            }
        }
    }

}