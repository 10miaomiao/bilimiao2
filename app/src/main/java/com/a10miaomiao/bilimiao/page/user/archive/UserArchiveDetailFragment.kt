package com.a10miaomiao.bilimiao.page.user.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm._isRefreshing
import com.a10miaomiao.bilimiao.comm.connectUi
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.pointerOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.stopSameIdAndArgs
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.wrapInSwipeRefreshLayout
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.search.result.BaseResultFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class UserArchiveDetailFragment : BaseResultFragment(), DIAware {

    companion object {
        fun newInstance(
            id: String,
            name: String,
        ): UserArchiveDetailFragment {
            val fragment = UserArchiveDetailFragment()
            val bundle = bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
            )
            fragment.arguments = bundle
            return fragment
        }
    }
    override val title get() = "全部投稿"
    override val menus get() = listOf(
        myMenuItem {
            key = MenuKeys.filter
            title = viewModel.rankOrder.title
            iconResource = R.drawable.ic_baseline_filter_list_grey_24
        },
    )

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when (menuItem.key) {
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
                    notifyConfigChanged()
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
                    notifyConfigChanged()
                }
                pm.show()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<UserArchiveDetailViewModel>(di)

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
        Navigation.findNavController(view).pointerOrSelf()
            .stopSameIdAndArgs(VideoInfoFragment.id, args)
            ?.navigate(VideoInfoFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<ArchiveInfo> { item, index ->
        videoItem(
            title = item.title,
            pic = item.cover,
            remark = NumberUtil.converCTime(item.ctime),
            playNum = item.play,
            damukuNum = item.danmaku,
            duration = NumberUtil.converDuration(item.duration),
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

//            val headerView = frameLayout {
//                _topPadding = contentInsets.top
//            }
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
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMode()
                }
//                addHeaderView(headerView)
                addFooterView(footerView)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }

}