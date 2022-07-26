package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.comment.videoCommentView
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class VideoCommentDetailFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "评论详情"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoCommentDetailViewModel>(di)

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

    private fun toSelfLink (view: View, url: String) {
        val urlInfo = BiliUrlMatcher.findIDByUrl(url)
        val urlType = urlInfo[0]
        var urlId = urlInfo[1]
        if (urlType == "BV") {
            urlId = "BV$urlId"
        }
        val args = bundleOf(
            MainNavGraph.args.id to urlId
        )
        when(urlType){
            "AV", "BV" -> {
                args.putString(MainNavGraph.args.type, urlType)
                Navigation.findNavController(view)
                    .navigate(MainNavGraph.action.videoCommentDetail_to_videoInfo, args)
            }
        }
    }

    private val handleUserClick = View.OnClickListener {
        val id = it.tag
        if (id != null) {
            val args = bundleOf(
                MainNavGraph.args.id to id
            )
            Navigation.findNavController(it)
                .navigate(MainNavGraph.action.videoCommentDetail_to_user, args)
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
    }

    private val handleLinkClickListener = ExpandableTextView.OnLinkClickListener { view, linkType, content, selfContent -> //根据类型去判断
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

    val itemUi = miaoBindingItemUi<VideoCommentReplyInfo> { item, index ->
        videoCommentView(
            mid = item.mid,
            uname = item.member.uname,
            avatar = item.member.avatar,
            time = NumberUtil.converCTime(item.ctime),
            floor = item.floor,
            content =item.content,
            like = item.like,
            count = item.count,
            onUpperClick = handleUserClick,
            onLinkClick = handleLinkClickListener,
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
            _miaoLayoutManage(
                LinearLayoutManager(requireContext())
            )

            val mAdapter = _miaoAdapter(
                items = viewModel.list.data,
                itemUi = itemUi,
            ) {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMode()
                }
            }

            headerViews(mAdapter) {
                val reply = viewModel.reply
                +videoCommentView(
                    mid = reply.member.mid,
                    uname = reply.member.uname,
                    avatar = reply.member.avatar,
                    time = NumberUtil.converCTime(reply.ctime),
                    floor = reply.floor,
                    content = reply.content,
                    like = reply.like,
                    count = reply.count,
                    onUpperClick = handleUserClick,
                    onLinkClick = handleLinkClickListener,
                ).apply {
                    _topPadding = contentInsets.top + config.dividerSize
                    backgroundColor = config.blockBackgroundColor
                }..lParams(matchParent, matchParent)
                +textView {
                    text = "全部回复"
                }..lParams {
                    margin = config.dividerSize
                }
            }

            footerViews(mAdapter) {
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