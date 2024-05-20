package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.BiliNavigation
import com.a10miaomiao.bilimiao.comm._isRefreshing
import com.a10miaomiao.bilimiao.comm.connectUi
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.currentOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.pointerOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.stopSameIdAndArgs
import com.a10miaomiao.bilimiao.comm.recycler.MiaoBindingAdapter
import com.a10miaomiao.bilimiao.comm.recycler.RecyclerViewFragment
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.ImageSaveUtil
import com.a10miaomiao.bilimiao.comm.wrapInSwipeRefreshLayout
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewInfo
import com.a10miaomiao.bilimiao.commponents.comment.videoCommentView
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.widget.gridimage.NineGridImageView
import com.a10miaomiao.bilimiao.widget.gridimage.OnImageItemClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.launch
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.NumIndicator
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class VideoCommentListFragment : RecyclerViewFragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.comment.list"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://video/{id}/comment?title={title}&cover={cover}&name={name}")
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.title) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.cover) {
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
            title: String,
            cover: String,
            name: String,
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.title to title,
                MainNavArgs.cover to cover,
                MainNavArgs.name to name,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "评论列表"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.send
                iconResource = R.drawable.ic_baseline_send_24
                title = "发布评论"
            },
            myMenuItem {
                key = MenuKeys.filter
                iconResource = R.drawable.ic_baseline_filter_list_grey_24
                title = SortOrderPopupMenu.getText(viewModel.sortOrder)
            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.filter -> {
                val pm = SortOrderPopupMenu(
                    activity = requireActivity(),
                    anchor = view,
                    checkedValue = viewModel.sortOrder
                )
                pm.setOnMenuItemClickListener(handleMenuItemClickListener)
                pm.show()
            }
            MenuKeys.send -> {
                if (!viewModel.isLogin()) {
                    PopTip.show("请先登录")
                    return
                }
                val params = SendCommentParam(
                    type = 1,
                    oid = viewModel.id,
                    title = viewModel.title,
                    image = viewModel.cover,
                    name = viewModel.name,
                    content = ""
                )
                val args = SendCommentFragment.createArguments(params)
                findNavController().pointerOrSelf()
                    .stopSameIdAndArgs(SendCommentFragment.id,args)
                    ?.navigate(SendCommentFragment.actionId, args)
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoCommentListViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val themeDelegate by instance<ThemeDelegate>()

    private var mAdapter: MiaoBindingAdapter<VideoCommentViewInfo>? = null

    private val handleMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
        it.isChecked = true
        viewModel.sortOrder = it.itemId
        pageConfig.notifyConfigChanged()
        viewModel.refreshList()
        false
    }

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

        // 页面返回回调数据接收
        findNavController().currentBackStackEntry?.let(::onNavBackStackEntry)
    }

    private fun onNavBackStackEntry(navBackStackEntry: NavBackStackEntry) {
        val savedStateHandle = navBackStackEntry.savedStateHandle
        val reply = savedStateHandle.get<Any>(MainNavArgs.reply)
        if (reply is VideoCommentViewInfo) {
            val index = savedStateHandle.get<Int>(MainNavArgs.index)
            if (index != null
                && index in viewModel.list.data.indices) {
                if (reply.isDelete) {
                    mAdapter?.removeAt(index)
                } else {
                    viewModel.list.data[index] = reply
                    mAdapter?.setData(index, reply)
                }
            }
        } else if (reply is VideoCommentReplyInfo) {
            mAdapter?.addData(
                0,
                VideoCommentViewAdapter.convertToVideoCommentViewInfo(reply)
            )
            toListTop()
        }
        savedStateHandle[MainNavArgs.reply] = null
    }

    private fun toSelfLink(view: View, url: String) {
        val urlInfo = BiliUrlMatcher.findIDByUrl(url)
        val urlType = urlInfo[0]
        var urlId = urlInfo[1]
        if (urlType == "BV") {
            urlId = "BV$urlId"
        }
        when (urlType) {
            "AV", "BV" -> {
                val args = VideoInfoFragment.createArguments(urlId)
                Navigation.findNavController(view).currentOrSelf()
                    .navigate(VideoInfoFragment.actionId, args)
            }
        }
    }

    private val handleUserClick = View.OnClickListener {
        val id = it.tag
        if (id != null && id is String) {
            val args = UserFragment.createArguments(id)
            Navigation.findNavController(it).currentOrSelf()
                .navigate(UserFragment.actionId, args)
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.getItem(position) as VideoCommentViewInfo
        val args = VideoCommentDetailFragment.createArguments(position, viewModel.upMid, item)
        Navigation.findNavController(view).currentOrSelf()
            .navigate(VideoCommentDetailFragment.actionId, args)
    }

    private val handleItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = adapter.getItem(position) as VideoCommentViewInfo
        val args = ReplyDetailFragment.createArguments(item)
        Navigation.findNavController(requireActivity(), R.id.nav_bottom_sheet_fragment)
            .navigate(ReplyDetailFragment.actionId, args)
        true
    }

    private val handleLinkClickListener =
        ExpandableTextView.OnLinkClickListener { view, linkType, content, selfContent -> //根据类型去判断
            when (linkType) {
                LinkType.LINK_TYPE -> {
                    val url = content
                    val re = BiliNavigation.navigationTo(view, url)
                    if (!re) {
                        if (url.indexOf("bilibili://") == 0) {
                            toast("不支持打开的链接：$url")
                        } else {
                            BiliUrlMatcher.toUrlLink(view, url)
                        }
                    }
                }

                LinkType.MENTION_TYPE -> {
//                toast("你点击了@用户 内容是：$content")
                }

                LinkType.SELF -> {
                    toSelfLink(view, selfContent)
                }
            }
        }


    private val handleLikeClick = View.OnClickListener {
//        requireActivity().toast("暂不支持此操作")
        val index = it.tag
        if (index is Int && index >= 0) {
            viewModel.setLike(index) { item ->
                viewModel.list.data[index] = item
                mAdapter?.setData(index, item)
            }
        }
    }

    private val handleImageItemClick = object : OnImageItemClickListener {
        override fun onClick(
            nineGridView: NineGridImageView,
            imageView: ImageView,
            url: String,
            urlList: List<String>,
            externalPosition: Int,
            position: Int
        ) {
            Mojito.start(imageView.context) {
                urls(urlList)
                position(position)
                progressLoader {
                    DefaultPercentProgress()
                }
                setIndicator(NumIndicator())
                views(nineGridView.getImageViews().toTypedArray())
                mojitoListener(
                    onLongClick = { a, _, _, _, i ->
                        val imageUrl = urlList[i]
                        val context = ContextThemeWrapper(a, themeDelegate.getThemeResId())
                        ImageSaveUtil(a!!, imageUrl).showMemu(context)
                    }
                )
            }
        }
    }

    val itemUi = miaoBindingItemUi<VideoCommentViewInfo> { item, index ->
        videoCommentView(
            index = index,
            mid = item.mid,
            uname = item.uname,
            avatar = item.avatar,
            time = item.time,
            location = item.location,
            floor = item.floor,
            content = item.content,
            like = item.like,
            count = item.count,
            isLike = item.isLike,
            upMid = viewModel.upMid,
            cardLabels = item.cardLabels,
            onUpperClick = handleUserClick,
            onLinkClick = handleLinkClickListener,
            onLikeClick = handleLikeClick,
            onImageItemClick = handleImageItemClick,
        ).apply {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            backgroundColor = config.windowBackgroundColor
            mLayoutManager = _miaoLayoutManage(
                LinearLayoutManager(requireContext())
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

            mAdapter = _miaoAdapter(
                items = viewModel.list.data,
                itemUi = itemUi,
            ) {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                setOnItemLongClickListener(handleItemLongClick)
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