package com.a10miaomiao.bilimiao.page.video

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore.Video
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoRelateInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoStaffInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoTagInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.page.search.SearchResultFragment
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.video.comment.VideoCommentListFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget._setContent
import com.a10miaomiao.bilimiao.widget.expandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.core.lParams
import splitties.views.dsl.recyclerview.recyclerView

class VideoInfoFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        const val TYPE_AV = "AV"
        const val TYPE_BV = "BV"
        override val name = "main"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://video/{id}")
            deepLink("bilibili://video/{id}")
            argument(MainNavArgs.type) {
                type = NavType.StringType
                defaultValue = TYPE_AV
            }
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
        }

        fun createArguments(
            id: String
        ): Bundle {
            val type = if (id.indexOf("BV") == 0) {
                VideoInfoFragment.TYPE_BV
            } else {
                VideoInfoFragment.TYPE_AV
            }
            return bundleOf(
                MainNavArgs.type to type,
                MainNavArgs.id to id,
            )
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoInfoViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val userStore by instance<UserStore>()

    private val playerStore by instance<PlayerStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    override val pageConfig = myPageConfig {
        val info = viewModel.info
        title = info?.let {
            "${it.bvid} /\nAV${it.aid}"
        } ?: "视频详情"
        menus = listOf(
            myMenuItem {
                key = 0
                iconResource = R.drawable.ic_more_vert_grey_24dp
                title = "更多"
            },
            myMenuItem {
                key = 1
                iconResource = R.drawable.ic_column_comm
                title = NumberUtil.converString(info?.stat?.reply?.toString() ?: "评论")
            },
//            myMenuItem {
//                key = 2
//                iconResource = R.drawable.ic_column_share
//                title = NumberUtil.converString(info?.stat?.share?.toString() ?: "分享")
//            },
            myMenuItem {
                key = 3
                iconResource = if (info?.req_user?.favorite == null) {
                    R.drawable.ic_column_unstar
                } else {
                    R.drawable.ic_column_star
                }
                title = NumberUtil.converString(info?.stat?.favorite?.toString() ?: "收藏")
            },
            myMenuItem {
                key = 4
                iconResource = if (info?.req_user?.coin == null) {
                    R.drawable.ic_column_uncoin
                } else {
                    R.drawable.ic_column_coin
                }
                title = NumberUtil.converString(info?.stat?.coin?.toString() ?: "投币")
            },
            myMenuItem {
                key = 5
                iconResource = if (info?.req_user?.like == null) {
                    R.drawable.ic_column_unlike
                } else {
                    R.drawable.ic_column_like
                }
                title = NumberUtil.converString(info?.stat?.like?.toString() ?: "点赞")
            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        val info = viewModel.info
        if (!userStore.isLogin() && (menuItem.key ?: 0) >= 3) {
            toast("请先登录")
            return
        }
        when (menuItem.key) {
            0 -> {
                // 更多
                val pm = VideoMorePopupMenu(
                    activity = requireActivity(),
                    anchor = view,
                    viewModel = viewModel
                )
                pm.show()
            }
            1 -> {
                // 评论
                if (info != null) {
                    val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = VideoCommentListFragment.createArguments(info.aid)
                    nav.navigate(VideoCommentListFragment.actionId, args)
                }
            }
            2 -> {
                // 分享
                if (info == null) {
                    toast("视频信息未加载完成，请稍后再试")
                    return
                }
                var shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "bilibili视频分享")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${info.title} https://www.bilibili.com/video/${info.bvid}"
                    )
                }
                requireActivity().startActivity(Intent.createChooser(shareIntent, "分享"))
            }
            3 -> {
                // 收藏
                if (info != null) {
                    val nav = Navigation.findNavController(
                        requireActivity(),
                        R.id.nav_bottom_sheet_fragment
                    )
                    val args = VideoAddFavoriteFragment.createArguments(info.aid)
                    nav.navigate(VideoAddFavoriteFragment.actionId, args)
                }
            }
            4 -> {
                // 投币
                if (info != null) {
                    val nav = Navigation.findNavController(
                        requireActivity(),
                        R.id.nav_bottom_sheet_fragment
                    )
                    val num = if (info.copyright == 2) {
                        1
                    } else {
                        2
                    }
                    val args = VideoCoinFragment.createArguments(num)
                    nav.navigate(VideoCoinFragment.actionId, args)
                }
            }
            5 -> {
                // 点赞
                viewModel.requestLike()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.info?.let {
            if (it.season?.is_jump == 1) {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getBoolean("player_auto_stop", false)
            && playerStore.state.aid == viewModel.id
        ) {
            basePlayerDelegate.closePlayer()
        }
    }

    fun confirmCoin(num: Int) {
        viewModel.requestCoin(num)
    }

    fun confirmFavorite(favIds: List<String>, addIds: List<String>, delIds: List<String>) {
        viewModel.requestFavorite(favIds, addIds, delIds)
    }

    private fun playVideo(cid: String, title: String) {
        val info = viewModel.info
        if (info != null) {
            basePlayerDelegate.openPlayer(
                VideoPlayerSource(
                    title = title,
                    coverUrl = info.pic,
                    aid = info.aid,
                    id = cid,
                    ownerId = info.owner.mid,
                    ownerName = info.owner.name,
                )
            )
//            basePlayerDelegate.playVideo(info.aid.toString(), cid, title)
        }
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
                val args = createArguments(urlId)
                Navigation.findNavController(view)
                    .navigate(actionId, args)
            }
            else -> {
                BiliUrlMatcher.toUrlLink(view, url)
            }
        }
    }

    private fun toUser(view: View, mid: String) {
        val args = UserFragment.createArguments(mid)
        Navigation.findNavController(view)
            .navigate(UserFragment.actionId, args)
    }

    private val handleUpperClick = View.OnClickListener {
        viewModel.info?.let { info ->
            toUser(it, info.owner.mid)
        }
    }

    private val handleMorePageClick = View.OnClickListener {
        viewModel.info?.let { info ->
            val nav = Navigation.findNavController(requireActivity(), R.id.nav_bottom_sheet_fragment)
            val args = VideoPagesFragment.createArguments(
                video = VideoPagesParam(
                    aid = info.aid,
                    title = info.title,
                    pic = info.pic,
                    ownerId = info.owner.mid,
                    ownerName = info.owner.name,
                    pages = info.pages.map {
                        VideoPagesParam.Page(it.cid, it.part)
                    }
                )
            )
            nav.navigate(VideoPagesFragment.actionId, args)
        }
    }

    private val handleUpperItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.staffs[position]
        toUser(view, item.mid)
    }

    private val handlePageItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.pages[position]
        playVideo(item.cid, item.part)
    }

    private val handleTagsItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.tags[position]
        val nav = findNavController()
        val args = SearchResultFragment.createArguments(item.tag_name)
        nav.navigate(SearchResultFragment.actionId, args)
    }

    private val handleRelateItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.getItem(position)
        if (item is VideoRelateInfo) {
            if (item.goto == "av") {
                val args = createArguments(item.aid!!)
                Navigation.findNavController(view)
                    .navigate(actionId, args)
            } else {
                val url = item.uri
                val re = BiliNavigation.navigationTo(view, url)
                if (!re) {
                    if (url.indexOf("bilibili://") == 0) {
                        toast("不支持打开的链接：$url")
                    } else {
                        BiliUrlMatcher.toUrlLink(view, url)
                    }
                }
            }
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.loadData()
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

    val pageItemUi = miaoBindingItemUi<VideoPageInfo> { item, index ->
        frameLayout {
            layoutParams = lParams {
                width = wrapContent
                height = matchParent
                rightMargin = dip(5)
            }
            val enabled = playerStore.state.cid != item.cid
            setBackgroundResource(R.drawable.shape_corner)
            _isEnabled = enabled

            views {
                +textView {
                    horizontalPadding = dip(10)
                    verticalPadding = dip(5)
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    maxWidth = dip(120)
                    minWidth = dip(60)
                    maxLines = 1
                    gravity = Gravity.LEFT
                    ellipsize = TextUtils.TruncateAt.END
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    _textColorResource = if (enabled) {
                        R.color.text_black
                    } else {
                        config.themeColorResource
                    }
                    _text = item.part
                }..lParams {
                    gravity = Gravity.CENTER
                }
            }
        }
    }

    fun MiaoUI.pageView(): View {
        return horizontalLayout {
            views {
                +frameLayout {

                    views {
                        +recyclerView {
                            val lm = LinearLayoutManager(context)
                            lm.orientation = LinearLayoutManager.HORIZONTAL
                            scrollBarSize = 0
                            _miaoLayoutManage(lm)

                            _miaoAdapter(
                                items = viewModel.pages,
                                itemUi = pageItemUi,
                                depsAry = arrayOf(playerStore.state.cid),
                            ) {
                                setOnItemClickListener(handlePageItemClick)
                            }
                        }..lParams {
                            width = matchParent
                            height = matchParent
                        }

                        +imageView {
                            scaleType = ImageView.ScaleType.FIT_XY
                            imageResource = R.drawable.shape_gradient
                        }..lParams {
                            gravity = Gravity.RIGHT
                            width = dip(10)
                            height = matchParent
                        }
                    }
                }..lParams {
                    width = matchParent
                    height = matchParent
                    weight = 1f
                }

                +imageView {
                    setImageResource(R.drawable.ic_navigate_next_black_24dp)
                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                    setOnClickListener(handleMorePageClick)
//                    setOnClickListener {
//                        val fragment = PagesFragment.newInstance(viewModel.id, viewModel.pages, 0)
//                        MainActivity.of(context)
//                            .showBottomSheet(fragment)
//                    }
                    _show = viewModel.pages.size > 2
                }..lParams(dip(24), dip(24)) {
                    gravity = Gravity.CENTER
                }
            }
        }
    }

    val upperItemUi = miaoBindingItemUi<VideoStaffInfo> { item, index ->
        verticalLayout {
            layoutParams = ViewGroup.LayoutParams(dip(64), wrapContent)
            topPadding = dip(10)
            bottomPadding = dip(5)
            gravity = Gravity.CENTER
            setBackgroundResource(config.selectableItemBackground)

            views {
                +rcImageView {
                    isCircle = true
                    _network(item.face, "@200w_200h")
                }..lParams {
                    height = dip(40)
                    width = dip(40)
                }

                +textView {
                    setTextColor(config.foregroundColor)
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    _text = item.name
                }
                +textView {
                    setTextColor(config.themeColor)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    _text = item.title
                }
            }
        }
    }

    fun MiaoUI.upperView(): View {
        return verticalLayout {

            val isMutableUpper = viewModel.info?.staff?.isEmpty() == false

            views {
                // 单个up主信息
                +horizontalLayout {
                    setBackgroundResource(config.selectableItemBackground)
                    setOnClickListener(handleUpperClick)
                    _show = !isMutableUpper

                    views {
                        +rcImageView {
                            isCircle = true
                            _network(viewModel.info?.owner?.face, "@200w_200h")
                        }..lParams {
                            height = dip(40)
                            width = dip(40)
                        }

                        +verticalLayout {

                            views {
                                +textView {
                                    setTextColor(config.foregroundColor)
                                    _text = viewModel.info?.owner?.name ?: ""
                                }
                                +textView {
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                    _text = "发表于 " + NumberUtil.converCTime(viewModel.info?.pubdate)
                                }
                            }

                        }..lParams {
                            leftMargin = dip(8)
                        }
                    }
                }..lParams {
                    width = matchParent
                }

                // 多个up主信息
                +recyclerView {
                    _show = isMutableUpper
                    scrollBarSize = 0
                    _miaoLayoutManage(
                        LinearLayoutManager(context).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                    )
                    _miaoAdapter(
                        itemUi = upperItemUi,
                        items = viewModel.staffs
                    ) {
                        setOnItemClickListener(handleUpperItemClick)
                    }
                }

                +textView {
                    _show = isMutableUpper
                    textSize = 12f
                    setTextColor(config.foregroundAlpha45Color)
                    _text = "发表于 " + NumberUtil.converCTime(viewModel.info?.pubdate)
                }..lParams {
                    topMargin = dip(10)
                }
            }

        }
    }

    val itemTagUi = miaoBindingItemUi<VideoTagInfo> { item, index ->
        frameLayout {
            views {
                +frameLayout {
                    apply(ViewStyle.roundRect(dip(5)))
                    setBackgroundResource(config.selectableItemBackground)

                    views {
                        +textView {
                            backgroundColor = config.blockBackgroundColor
                            padding = dip(5)
                            _text = item.tag_name
                        }
                    }
                }..lParams {
                    rightMargin = dip(8)
                    bottomMargin = dip(5)
                }
            }
        }
    }

    fun MiaoUI.tagsView(): View {
        return recyclerView {
            isNestedScrollingEnabled = false
            _miaoLayoutManage(
                FlexboxLayoutManager(requireActivity()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
            )
            _miaoAdapter(
                itemUi = itemTagUi,
                items = viewModel.tags
            ) {
                setOnItemClickListener(handleTagsItemClick)
            }
        }
    }

    fun MiaoUI.headerView(): View {
        val videoInfo = viewModel.info
        return verticalLayout {
            views {
                +horizontalLayout {
                    views {
                        +rcImageView {
                            radius = dip(5)
                            _network(videoInfo?.pic, "@672w_378h_1c_")
                        }..lParams {
                            width = dip(150)
                            height = dip(100)
                            rightMargin = dip(10)
                        }

                        +verticalLayout {

                            views {
                                // 标题
                                +textView {
                                    textSize = 16f
                                    ellipsize = TextUtils.TruncateAt.END
                                    maxLines = 4
                                    setTextColor(config.foregroundColor)
                                    _text = videoInfo?.title ?: ""
                                }..lParams(weight = 1f)

                                // 播放量
                                +horizontalLayout {

                                    views {

                                        +imageView {
                                            imageTintList =
                                                ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_views)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
                                            _text =
                                                NumberUtil.converString(videoInfo?.stat?.view ?: "")
                                        }..lParams {
                                            leftMargin = dip(3)
                                            rightMargin = dip(16)
                                        }

                                        +imageView {
                                            imageTintList =
                                                ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_danmakus)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
                                            _text = NumberUtil.converString(
                                                videoInfo?.stat?.danmaku ?: ""
                                            )
                                        }..lParams {
                                            leftMargin = dip(3)
                                            rightMargin = dip(16)
                                        }
                                    }

                                }..lParams {
                                    width = matchParent
                                    height = wrapContent
                                    gravity = Gravity.CENTER_VERTICAL
                                }
                            }
                        }..lParams(matchParent, matchParent)


                    }
                }

                +upperView()..lParams {
                    width = matchParent
                    height = wrapContent
                    topMargin = dip(10)
                }
                +pageView()..lParams {
                    width = matchParent
                    height = dip(48)
                    topMargin = dip(10)
                }

                +expandableTextView {
                    setLineSpacing(dip(4).toFloat(), 1.0f)
                    textSize = 14f
                    setMaxLine(2)
                    isNeedContract = true
                    isNeedExpend = true
                    setNeedMention(false)
                    isNeedSelf = true
                    setNeedConvertUrl(false)
                    _setContent(viewModel.info?.desc ?: "")
                    linkClickListener = handleLinkClickListener
                }..lParams {
                    width = matchParent
                    height = wrapContent
                    topMargin = dip(10)
                }

                +tagsView()..lParams {
                    width = matchParent
                    height = wrapContent
                    topMargin = dip(10)
                }
            }

        }
    }

    val relateItemUi = miaoBindingItemUi<VideoRelateInfo> { item, index ->
        videoItem(
            title = item.title,
            pic = item.pic,
            upperName = item.owner?.name,
            playNum = item.stat?.view,
            damukuNum = item.stat?.danmaku
        )
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        connectStore(viewLifecycleOwner, playerStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        DebugMiao.log("VideoInfoFragmentContentInsets", contentInsets)
        val info = viewModel.info
        // 监听info改变，修改页面标题
        miaoEffect(listOf(info, info?.req_user, info?.staff)) {
            pageConfig.notifyConfigChanged()
        }
        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            backgroundColor = config.windowBackgroundColor
            _miaoLayoutManage(
                GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
            )

            val mAdapter = _miaoAdapter(
                items = viewModel.relates,
                itemUi = relateItemUi,
            ) {
                setOnItemClickListener(handleRelateItemClick)
            }

            headerViews(mAdapter) {
                +headerView().apply {
                    horizontalPadding = config.pagePadding
                    _topPadding = contentInsets.top + config.pagePadding
                }..lParams(matchParent, wrapContent)
            }
            footerViews(mAdapter) {
                +frameLayout {
                    _topPadding = contentInsets.bottom
                }
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.loading
        }
    }

}