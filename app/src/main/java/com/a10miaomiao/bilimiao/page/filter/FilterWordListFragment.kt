package com.a10miaomiao.bilimiao.page.filter

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.rightPadding
import splitties.views.topPadding

// 屏蔽标题关键字设置
class FilterWordListFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "filter.word.list"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://filter/word/list")
        }
    }

    override val pageConfig = myPageConfig {
        title = "屏蔽管理\n-\n关键字"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.add
                iconResource = R.drawable.ic_add_white_24dp
                title = "添加"
            },
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
            MenuKeys.add -> {
                val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
                nav.navigate(Uri.parse("bilimiao://filter/word/add"))
            }
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

    private val viewModel by diViewModel<FilterWordListViewModel>(di)

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

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list[position]
        val nav = requireActivity().findNavController(R.id.nav_bottom_sheet_fragment)
        nav.navigate(Uri.parse("bilimiao://filter/word/edit/" + item))
    }

    val handleCheckedChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val tag = buttonView.tag
        if (tag is String) {
            viewModel.selectedChange(tag, isChecked)
            pageConfig.notifyConfigChanged()
        }
    }

    val itemUi = miaoBindingItemUi<String> { item, index ->
        horizontalLayout {
            horizontalPadding = config.pagePadding
            topPadding = config.dividerSize
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(config.selectableItemBackground)
            layoutParams = lParams(matchParent, wrapContent)

            views {
                +checkBox {
                    rightPadding = config.dividerSize

                    val isSelected = viewModel.selectedList.contains(item)
                    miaoEffect(listOf(item, isSelected)) {
                        tag = item
                        isChecked = isSelected
                    }

                    setOnCheckedChangeListener(handleCheckedChange)
                }
                +textView {
                    textSize = 20f
                    _text = item
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
            ) {
                setOnItemClickListener(handleItemClick)
            }
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
                            text = "去添加关键字"
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