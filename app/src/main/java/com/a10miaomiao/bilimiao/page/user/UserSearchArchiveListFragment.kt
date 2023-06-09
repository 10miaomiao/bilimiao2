package com.a10miaomiao.bilimiao.page.user

import android.content.Context
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
import bilibili.app.archive.v1.Archive
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.mypage.*
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.openSearchDrawer
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
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class UserSearchArchiveListFragment  : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.archive.search"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.name) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.text) {
                type = NavType.StringType
                nullable = false
            }
        }
        fun createArguments(
            id: String,
            name: String,
            keyword: String,
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
                MainNavArgs.text to keyword,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = if (viewModel.keyword?.isBlank() == true) {
            "${viewModel.name}\n的\n投稿列表"
        } else {
            "搜索\n-\n投稿列表\n-\n${viewModel.keyword}"
        }
        menus = listOf(
            myMenuItem {
                key = MenuKeys.search
                title = "继续搜索"
                iconResource = R.drawable.ic_search_gray
            },
        )
        search = SearchConfigInfo(
            name = "搜索投稿列表",
            keyword = viewModel.keyword,
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            MenuKeys.search -> {
                requireActivity().getScaffoldView().openSearchDrawer()
            }
        }
    }

    override fun onSearchSelfPage(context: Context, keyword: String) {
        viewModel.keyword = keyword
        pageConfig.notifyConfigChanged()
        viewModel.refreshList()
    }

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@UserSearchArchiveListFragment }
    }

    private val viewModel by diViewModel<UserSearchArchiveListViewModel>(di)

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
        val args = VideoInfoFragment.createArguments(item.aid.toString())
        Navigation.findNavController(view)
            .navigate(VideoInfoFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<Archive.Arc> { item, index ->
        videoItem (
            title = item.title,
            pic = item.pic,
            remark = NumberUtil.converCTime(item.ctime),
            playNum = item.stat.view.toString(),
            damukuNum = item.stat.danmaku.toString(),
            isHtml = true,
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