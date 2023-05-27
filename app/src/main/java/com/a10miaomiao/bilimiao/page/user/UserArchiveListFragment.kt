package com.a10miaomiao.bilimiao.page.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.space.v1.SpaceOuterClass
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.video.SubmitVideosInfo
import com.a10miaomiao.bilimiao.comm.mypage.*
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class UserArchiveListFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.archive"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.name) {
                type = NavType.StringType
                nullable = false
            }
        }
        fun createArguments(
            id: String,
            name: String
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "${viewModel.name}\n的\n投稿列表"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.filter
                title = viewModel.rankOrder.title
                iconResource = R.drawable.ic_baseline_filter_list_grey_24
            },
//            myMenuItem {
//                key = MenuKeys.region
//                title = viewModel.region.title
//                iconResource = R.drawable.ic_baseline_grid_on_gray_24
//            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            MenuKeys.region -> {
                val pm = CheckPopupMenu(
                    context = requireActivity(),
                    anchor = view,
                    menus = viewModel.regionList,
                    value = viewModel.region.value,
                )
                pm.onMenuItemClick = {
                    viewModel.region = it
                    viewModel.refreshList()
                    pageConfig.notifyConfigChanged()
                }
                pm.show()
            }
            MenuKeys.filter -> {
                val pm = CheckPopupMenu(
                    context = requireActivity(),
                    anchor = view,
                    menus = viewModel.rankOrderList,
                    value = viewModel.rankOrder.value,
                )
                pm.onMenuItemClick = {
                    viewModel.rankOrder = it
                    viewModel.refreshList()
                    pageConfig.notifyConfigChanged()
                }
                pm.show()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@UserArchiveListFragment }
    }

    private val viewModel by diViewModel<UserArchiveListViewModel>(di)

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

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = VideoInfoFragment.createArguments(item.param)
        Navigation.findNavController(view)
            .navigate(VideoInfoFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<ArchiveInfo> { item, index ->
        videoItem (
            title = item.title,
            pic = item.cover,
            remark = NumberUtil.converCTime(item.ctime),
            playNum = item.play,
            damukuNum = item.danmaku,
        )
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            backgroundColor = config.windowBackgroundColor
            _miaoLayoutManage(
                GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
            )

            val headerView = frameLayout {
                _topPadding = contentInsets.top
            }
            val footerView = listStateView(
                when {
                    viewModel.triggered -> ListState.NORMAL
                    viewModel.list.loading -> ListState.LOADING
                    viewModel.list.fail -> ListState.FAIL
                    viewModel.list.finished -> ListState.NOMORE
                    else -> ListState.NORMAL
                }
            ) {
                _bottomPadding = contentInsets.bottom
            }
            footerView.layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

            _miaoAdapter(
                items = viewModel.list.data,
                itemUi = itemUi,
            ) {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMode()
                }
                addHeaderView(headerView)
                addFooterView(footerView)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }

}