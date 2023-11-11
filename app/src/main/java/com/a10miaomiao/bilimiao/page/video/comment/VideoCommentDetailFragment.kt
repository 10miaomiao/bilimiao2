package com.a10miaomiao.bilimiao.page.video.comment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
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
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.comm.utils.*
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.NumIndicator
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class VideoCommentDetailFragment : RecyclerViewFragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.comment.detail"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.index) {
                type = NavType.IntType
                nullable = false
            }
            argument(MainNavArgs.reply) {
                type = NavType.ParcelableType(VideoCommentViewInfo::class.java)
                nullable = false
            }
        }

        fun createArguments(
            index: Int,
            reply: VideoCommentViewInfo
        ): Bundle {
            return bundleOf(
                MainNavArgs.index to index,
                MainNavArgs.reply to reply,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "评论详情"

        menus = listOf(
            myMenuItem {
                key = MenuKeys.send
                iconResource = R.drawable.ic_baseline_send_24
                title = "回复评论"
            },
            myMenuItem {
                key = MenuKeys.delete
                iconResource = R.drawable.ic_baseline_delete_outline_24
                title = "删除评论"
                visibility = if (viewModel.isSelfReply()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.send -> {
                val replyParams = viewModel.reply
                val content = replyParams.content.message.split("\n")[0]
                val params = SendCommentParam(
                    type = 2,
                    oid = replyParams.oid.toString(),
                    root = replyParams.id.toString(),
                    parent = replyParams.id.toString(),
                    content = content,
                    image = replyParams.avatar,
                    name = replyParams.uname,
                    title = ""
                )
                val args = SendCommentFragment.createArguments(params)
                findNavController().navigate(SendCommentFragment.actionId, args)
            }
            MenuKeys.delete -> {
                viewModel.delete()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoCommentDetailViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val themeDelegate by instance<ThemeDelegate>()

    private var mAdapter: MiaoBindingAdapter<VideoCommentViewInfo>? = null

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
        if (reply is VideoCommentReplyInfo) {
            val parentId = reply.parent
            var position = 0
            if (parentId != viewModel.reply.id) {
                val index = viewModel.list.data.indexOfFirst { it.id == parentId }
                if (index in viewModel.list.data.indices) {
                    position = index + 1
                }
            }
            mAdapter?.addData(
                position,
                VideoCommentViewAdapter.convertToVideoCommentViewInfo(reply)
            )
            if (position == 0) {
                toListTop()
            }
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
                Navigation.findNavController(view)
                    .navigate(VideoInfoFragment.actionId, args)
            }
        }
    }

    private fun toSendCommentPage(reply: VideoCommentViewInfo) {
        val content = reply.content.message.split("\n")[0]
        val params = SendCommentParam(
            type = 3,
            oid = reply.oid.toString(),
            root = viewModel.reply.id.toString(),
            parent = reply.id.toString(),
            content = content,
            image = reply.avatar,
            name = reply.uname,
            title = ""
        )
        val args = SendCommentFragment.createArguments(params)
        findNavController().navigate(SendCommentFragment.actionId, args)
    }

    private val handleUserClick = View.OnClickListener {
        val id = it.tag
        if (id != null && id is String) {
            val args = UserFragment.createArguments(id)
            Navigation.findNavController(it)
                .navigate(UserFragment.actionId, args)
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.getItem(position) as VideoCommentViewInfo
        MaterialAlertDialogBuilder(requireContext()).apply {
//            setTitle("评论操作")
            val menuItems = if (viewModel.isSelfReply(item)) {
                arrayOf<CharSequence>("回复评论", "删除评论",)
            } else {
                arrayOf<CharSequence>("回复评论",)
            }
            setItems(
                menuItems
            ) { _, i ->
                when (i) {
                    0 -> toSendCommentPage(item)
                    1 -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (viewModel.delete(item)) {
                                mAdapter?.removeAt(position)
                            }
                        }
                    }
                }
            }
        }.show()
    }

    private val handleHeaderLongClick = View.OnLongClickListener {
        val reply = viewModel.reply
        val args = ReplyDetailFragment.createArguments(reply)
        Navigation.findNavController(requireActivity(), R.id.nav_bottom_sheet_fragment)
            .navigate(ReplyDetailFragment.actionId, args)
        true
    }

    private val handleItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = adapter.getItem(position) as VideoCommentViewInfo
        val args = ReplyDetailFragment.createArguments(item)
        Navigation.findNavController(requireActivity(), R.id.nav_bottom_sheet_fragment)
            .navigate(ReplyDetailFragment.actionId, args)
        true
    }

    private val handleRootLikeClick = View.OnClickListener {
        viewModel.setRootLike()
    }

    private val handleLikeClick = View.OnClickListener {
        val index = it.tag
        if (index is Int && index >= 0) {
            viewModel.setLike(index) { item ->
                viewModel.list.data[index] = item
                mAdapter?.setData(index, item)
            }
        }
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
            viewInfo = item,
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
            }

            headerViews(mAdapter!!) {
                val reply = viewModel.reply
                +videoCommentView(
                    viewInfo = reply,
                    textIsSelectable = true,
                    onUpperClick = handleUserClick,
                    onLinkClick = handleLinkClickListener,
                    onLikeClick = handleRootLikeClick,
                    onImageItemClick = handleImageItemClick,
                ).apply {
                    _topPadding = contentInsets.top + config.dividerSize
                    backgroundColor = config.blockBackgroundColor
                    setOnLongClickListener(handleHeaderLongClick)
                }..lParams(matchParent, matchParent)
                +textView {
                    text = "全部回复"
                }..lParams {
                    margin = config.dividerSize
                }
            }

            footerViews(mAdapter!!) {
                +listStateView(
                    when {
                        viewModel.triggered -> ListState.NORMAL
                        viewModel.list.loading -> ListState.LOADING
                        viewModel.list.fail -> ListState.FAIL
                        viewModel.list.finished -> ListState.NOMORE
                        else -> ListState.NORMAL
                    }
                ) {
                    _bottomPadding = contentInsets.bottom
                }..lParams(matchParent, wrapContent)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }

}