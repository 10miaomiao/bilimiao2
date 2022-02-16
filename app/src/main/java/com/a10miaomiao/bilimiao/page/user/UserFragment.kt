package com.a10miaomiao.bilimiao.page.user

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UpperChannelInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.season.miniSeasonItemView
import com.a10miaomiao.bilimiao.commponents.video.mediaItemView
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.a10miaomiao.bilimiao.widget.wrapInLimitedFrameLayout
import com.bumptech.glide.Glide
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
import splitties.views.horizontalPadding
import splitties.views.padding
import splitties.views.verticalPadding

class UserFragment  : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        val info = viewModel.dataInfo
        title = info?.card?.name ?: "个人信息"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<UserViewModel>(di)

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

    private val handleMoreClick = View.OnClickListener {
        val info = viewModel.dataInfo
        if (info == null) {
            toast("操作失败，信息未加载完成")
        } else {
            when (it.tag) {
                "archive" -> {
                    val args = bundleOf(
                        MainNavGraph.args.id to viewModel.id,
                        MainNavGraph.args.name to info.card.name
                    )
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.user_to_userArchiveList, args)
                }
                "season" -> {

                }
                "favourite" -> {
                    val args = bundleOf(
                        MainNavGraph.args.id to viewModel.id,
                        MainNavGraph.args.name to info.card.name
                    )
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.user_to_userFavouriteList, args)
                }
                "attention" -> {

                }
                "fans" -> {

                }
            }
        }
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.data[position]
        if (item != null) {
            when (item) {
                is SpaceInfo.ArchiveItem -> {
                    val args = bundleOf(
                        MainNavGraph.args.id to item.param
                    )
                    Navigation.findNavController(view)
                        .navigate(MainNavGraph.action.user_to_videoInfo, args)
                }
                is SpaceInfo.FavouriteItem -> {
                    val args = bundleOf(
                        MainNavGraph.args.id to item.media_id,
                        MainNavGraph.args.id to item.name
                    )
                    Navigation.findNavController(view)
                        .navigate(MainNavGraph.action.user_to_userFavouriteDetail, args)
                }
            }
        }
    }

    fun MiaoUI.userNavView(
        title: String,
        number: Int,
        tag: String? = null,
        isShow: Boolean = true
    ): View {
        return verticalLayout {
            gravity = Gravity.CENTER
            padding = dip(10)
            setBackgroundResource(config.selectableItemBackground)
            setTag(tag)
            _show = isShow
            setOnClickListener(handleMoreClick)

            views {
                +textView {
                    _text = title
                    textSize = 18f
                }..lParams {
                    bottomMargin = dip(3)
                }
                +textView {
                    _text = NumberUtil.converString(number.toString())
                    textSize = 12f
                }..lParams(wrapContent, wrapContent)
            }

        }
    }

    fun MiaoUI.userInfoView(): View {
        val userCardInfo = viewModel.dataInfo?.card
        return horizontalLayout {
            padding = dip(10)

            views {
                +rcImageView {
                    isCircle = true
                    _network(userCardInfo?.face)
                }..lParams(dip(64), dip(64)) {
                    rightMargin = dip(10)
                }

                +verticalLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    views {
                        +textView {
                            textSize = 20f
                            setTextColor(config.foregroundColor)
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1

                            _text = userCardInfo?.name ?: ""
                        }..lParams {
                            topMargin = dip(24)
                            bottomMargin = dip(2)
                        }
                        +textView {
//                            ellipsize = TextUtils.TruncateAt.END
//                            maxLines = 1

                            _text = userCardInfo?.sign ?: ""
                        }
                    }
                }..lParams(matchParent, wrapContent)
            }
        }
    }

    fun MiaoUI.userCardView(): View{
        val userInfo = viewModel.dataInfo
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            apply(ViewStyle.roundRect(dip(5)))

            views {
                +frameLayout {
                    views {
                        +imageView {
                            miaoEffect(viewModel.dataInfo) {
                                Glide.with(context)
                                    .run {
                                        if (it == null || it.images.imgUrl.isEmpty())
                                            load(R.drawable.top_bg1)
                                        else
                                            loadImageUrl(it.images.imgUrl)
                                    }
                                    .centerCrop()
                                    .dontAnimate()
                                    .into(this)
                            }
                        }..lParams(matchParent, dip(80))

                        +userInfoView()..lParams(matchParent, wrapContent) {
                            topMargin = dip(48)
                        }
                    }
                }..lParams(matchParent, wrapContent)

                +horizontalLayout {

                    views {
                        +userNavView(
                            title = "投稿",
                            number = userInfo?.archive?.count ?: 0,
                            tag = "archive",
                            isShow = userInfo?.tab?.archive == true
                        )..lParams { weight = 1f }
                        +userNavView(
                            title = "追番",
                            tag = "season",
                            number = userInfo?.season?.count ?: 0,
                            isShow = userInfo?.tab?.bangumi == true
                        )..lParams { weight = 1f }
                        +userNavView(
                            title = "收藏",
                            tag = "favourite",
                            number = userInfo?.favourite?.count ?: 0,
                            isShow = userInfo?.tab?.favorite == true
                        )..lParams { weight = 1f }
                        +userNavView(
                            title = "关注",
                            number = userInfo?.card?.attention ?: 0,
                            tag = "attention",
                        )..lParams { weight = 1f }
                        +userNavView(
                            title = "粉丝",
                            number = userInfo?.card?.fans ?: 0,
                            tag = "fans",
                        )..lParams { weight = 1f }
                    }
                }..lParams(matchParent, wrapContent)
            }
        }
    }

    private fun MiaoUI.mediaTitleView(
        title: String,
        tag: String? = null,
        isShow: Boolean = true,
    ): View {
        return horizontalLayout {
            padding = dip(5)
            gravity = Gravity.CENTER_VERTICAL
            _show = isShow

            views {
                +textView {
                    _text = title
                    textSize = 20f
                }..lParams {
                    weight = 1f
                    height = wrapContent
                    width = matchParent
                }

                +textView {
                    text = "更多 >"
                    setTextColor(config.themeColor)
                    textSize = 14f
                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                    setTag(tag)
                    setOnClickListener(handleMoreClick)

                    _show = tag != null
                }

            }
        }
    }

    private fun MiaoUI.archiveItemView(
        cover: String,
        title: String
    ) : View{
        return verticalLayout {
            padding = dip(5)
            setBackgroundResource(config.selectableItemBackground)

            views {
                +rcImageView {
                    radius = dip(5)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    _network(cover)
                }..lParams(matchParent, dip(100))

                +textView {
                    textSize = 16f
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 2
                    setTextColor(config.foregroundColor)
                    _text = title
                }..lParams {
                    verticalMargin = dip(5)
                }
            }
        }
    }

    val ui = miaoBindingUi {
        // 监听info改变，修改页面标题
        miaoEffect(viewModel.dataInfo) {
            pageConfig.notifyConfigChanged()
        }
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom + config.pagePadding
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding

            views {
                +userCardView()

                val subject = if(viewModel.isSelf) "我" else "Ta"

                // 投稿
                var isShow = viewModel.dataInfo?.archive?.count?: 0 > 0
                +mediaTitleView(
                    title = "${subject}的投稿",
                    isShow = isShow,
                    tag = "archive",
                )..lParams(matchParent, wrapContent) {
                    _topMargin = config.dividerSize
                }
                +recyclerView {
                    _show = isShow
                    isNestedScrollingEnabled = false
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(150))
                    )
                    val itemUi = miaoMemo(null) {
                        miaoBindingItemUi<SpaceInfo.ArchiveItem> { item, _ ->
                            archiveItemView(item.cover, item.title)
                        }
                    }
                    _miaoAdapter(
                        items = viewModel.dataInfo?.archive?.item?.toMutableList(),
                        itemUi = itemUi,
                    ){
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams(matchParent, wrapContent)

                // 频道
                isShow = viewModel.channelList.isNotEmpty()
                +mediaTitleView(
                    title = "${subject}的频道",
                    isShow = isShow,
                )..lParams(matchParent, wrapContent) {
                    _topMargin = config.dividerSize
                }
                +recyclerView {
                    _show = isShow
                    isNestedScrollingEnabled = false
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(150))
                    )
                    val itemUi = miaoMemo(null) {
                        miaoBindingItemUi<UpperChannelInfo> { item, _ ->
                            mediaItemView(
                                title = item.name,
                                subtitle = "共${item.count}个视频",
                                cover = item.cover,
                            )
                        }
                    }
                    _miaoAdapter(
                        items = viewModel.channelList.toMutableList(),
                        itemUi = itemUi,
                    ){
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams(matchParent, wrapContent)

                // 追番
                isShow = viewModel.dataInfo?.season?.count?: 0 > 0
                +mediaTitleView(
                    title = "${subject}的追番",
                    isShow = isShow,
                    tag = "season",
                )..lParams(matchParent, wrapContent) {
                    _topMargin = config.dividerSize
                }
                +recyclerView {
                    _show = isShow
                    isNestedScrollingEnabled = false
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(120))
                    )
                    val itemUi = miaoMemo(null) {
                        miaoBindingItemUi<SpaceInfo.SeasonItem> { item, _ ->
                            miniSeasonItemView(
                                title = item.title,
                                cover = item.cover,
                            )
                        }
                    }
                    _miaoAdapter(
                        items = viewModel.dataInfo?.season?.item?.toMutableList(),
                        itemUi = itemUi,
                    ){
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams(matchParent, wrapContent)

                // 收藏
                isShow = viewModel.dataInfo?.favourite?.count?: 0 > 0
                +mediaTitleView(
                    title = "${subject}的收藏",
                    isShow = isShow,
                    tag = "favourite",
                )..lParams(matchParent, wrapContent) {
                    _topMargin = config.dividerSize
                }
                +recyclerView {
                    _show = isShow
                    isNestedScrollingEnabled = false
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(150))
                    )
                    val itemUi = miaoMemo(null) {
                        miaoBindingItemUi<SpaceInfo.FavouriteItem> { item, _ ->
                            mediaItemView(
                                title = item.name,
                                subtitle = "共${item.cur_count}个视频",
                                cover = if (item.cover != null && item.cover.isNotEmpty()) {
                                    item.cover[0].pic
                                } else { "" },
                            )
                        }
                    }
                    _miaoAdapter(
                        items = viewModel.dataInfo?.favourite?.item?.toMutableList(),
                        itemUi = itemUi,
                    ){
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams(matchParent, wrapContent)

                // 推荐
                isShow = viewModel.dataInfo?.like_archive?.count?: 0 > 0
                +mediaTitleView(
                    title = "${subject}推荐的",
                    isShow = isShow,
                )..lParams(matchParent, wrapContent) {
                    _topMargin = config.dividerSize
                }
                +recyclerView {
                    _show = isShow
                    isNestedScrollingEnabled = false
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(150))
                    )
                    val itemUi = miaoMemo(null) {
                        miaoBindingItemUi<SpaceInfo.ArchiveItem> { item, _ ->
                            archiveItemView(item.cover, item.title)
                        }
                    }
                    _miaoAdapter(
                        items = viewModel.dataInfo?.like_archive?.item?.toMutableList(),
                        itemUi = itemUi,
                    ){
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams(matchParent, wrapContent)

            }
        }.wrapInLimitedFrameLayout {
            maxWidth = config.containerWidth
        }.wrapInNestedScrollView(
            height = ViewGroup.LayoutParams.MATCH_PARENT,
            gravity = Gravity.CENTER_HORIZONTAL,
        ) {
//            miaoEffect(null) {
//                DebugMiao.log("viewModel.scrollY", viewModel.scrollY)
//                scrollTo(0, viewModel.scrollY)
//                setOnScrollChangeListener(viewModel.handleScrollChange)
//            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener { viewModel.loadData() }
            _isRefreshing = viewModel.loading
        }
    }

}




