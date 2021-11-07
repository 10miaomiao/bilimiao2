package com.a10miaomiao.bilimiao.page.video

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._isEnabled
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoRelateInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoStaffInfo
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import com.a10miaomiao.bilimiao.widget._setContent
import com.a10miaomiao.bilimiao.widget.comm.getAppBarView
import com.a10miaomiao.bilimiao.widget.expandableTextView
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import kotlin.properties.Delegates

class VideoInfoFragment: Fragment(), DIAware {

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoInfoViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val playerStore by instance<PlayerStore>()

    private val playerDelegate by instance<PlayerDelegate>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        lifecycle.coroutineScope.launch {
//            windowStore.connectUi(ui)
//            playerStore.connectUi(ui)
//
//            withContext(Dispatchers.IO){
//                while (isActive) {
//                    DebugMiao.log("isActive", isActive)
//                    delay(1000)
//                }
//            }
//        }
    }

    private fun playVideo(cid: String, title: String) {
        val info = viewModel.info
        if (info != null) {
            playerDelegate.playVideo(info.aid.toString(), cid, title)
        }
    }

    private val handlePageItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.pages[position]
        playVideo(item.cid, item.part)
    }

    private val handleRelateItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.relates[position]
        val args = bundleOf(
            MainNavGraph.args.id to item.aid
        )
        Navigation.findNavController(view)
            .navigate(MainNavGraph.action.videoInfo_to_videoInfo, args)
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.loadData()
    }

    val pageItemUi = miaoBindingItemUi<VideoPageInfo> { item, index ->
        frameLayout {
            layoutParams = lParams {
                width = wrapContent
                height = matchParent
                rightMargin = dip(5)
            }
            val enabled = playerStore.state.info.cid != item.cid
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
                            _miaoLayoutManage(lm)

                            _miaoAdapter(
                                items = viewModel.pages,
                                itemUi = pageItemUi,
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
                    _network(item.face)
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
                textView {
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
                    setBackgroundColor(config.selectableItemBackground)
                    setOnClickListener {
                    }
                    _show = !isMutableUpper

                    views {
                        +rcImageView {
                            isCircle = true
                            _network(viewModel.info?.owner?.face)
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
                    _miaoLayoutManage(
                        LinearLayoutManager(context).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                    )
                    _miaoAdapter(
                        itemUi = upperItemUi,
                        items = viewModel.staffs
                    )
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

    fun MiaoUI.headerView(): View {
        val videoInfo = viewModel.info
        val contentInsets = windowStore.state.contentInsets
        return verticalLayout {
            padding = dip(10)
            _topPadding = dip(10) + contentInsets.top

            views {
                +horizontalLayout {
                    views {
                        +rcImageView {
                            radius = dip(5)
                            _network(videoInfo?.pic)
//                            setOnClickListener {
//                                val info = viewModel.info.value!!
//                                playVideo(
//                                    info.cid.toString(),
//                                    info.title
//                                )
//                            }
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
                                    maxLines = 2
                                    setTextColor(config.foregroundColor)
                                    _text = videoInfo?.title ?: ""
                                }..lParams(weight = 1f)

                                // 播放量
                                +horizontalLayout {

                                    views {

                                        +imageView {
                                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_views)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
                                            _text = NumberUtil.converString(videoInfo?.stat?.view ?: "")
                                        }..lParams {
                                            leftMargin = dip(3)
                                            rightMargin = dip(16)
                                        }

                                        +imageView {
                                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_danmakus)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
                                            _text = NumberUtil.converString(videoInfo?.stat?.danmaku ?: "")
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
                    _setContent(viewModel.info?.desc ?: "")
//                    linkClickListener = ExpandableTextView.OnLinkClickListener { linkType, content, selfContent -> //根据类型去判断
//                        when (linkType) {
//                            LinkType.LINK_TYPE -> {
//                                viewModel.toLink(content)
//                            }
//                            LinkType.MENTION_TYPE -> {
//                                toast("你点击了@用户 内容是：$content")
//                            }
//                            LinkType.SELF -> {
//                                viewModel.toLink(selfContent)
//                            }
//                        }
//                    }
                }..lParams {
                    width = matchParent
                    height = wrapContent
                    topMargin = dip(10)
                }
            }

        }
    }

    val relateItemUi = miaoBindingItemUi <VideoRelateInfo> { item, index ->
        videoItem(
            title = item.title,
            pic = item.pic,
            upperName = item.owner.name,
            playNum = item.stat.view,
            damukuNum = item.stat.danmaku
        )
    }

    val ui = miaoBindingUi {
        // 监听info改变，修改页面标题
        miaoEffect(viewModel.info) {
            getAppBarView().setProp {
                title = it?.let {
                    "${it.bvid} /\nAV${it.aid}"
                } ?: "视频详情"
            }
        }
        verticalLayout {
            views {
                +recyclerView {
                    backgroundColor = config.windowBackgroundColor
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
                    )

                    val haderView = headerView()
                    haderView..lParams(matchParent, wrapContent)

                    _miaoAdapter(
                        items = viewModel.relates,
                        itemUi = relateItemUi,
                    ) {
                        setOnItemClickListener(handleRelateItemClick)
//                        loadMoreModule.setOnLoadMoreListener {
//                            viewModel.loadMode()
//                        }
                        addHeaderView(haderView)
                    }
                }.wrapInSwipeRefreshLayout {
                    setColorSchemeResources(config.themeColorResource)
                    setOnRefreshListener(handleRefresh)
                    _isRefreshing = viewModel.loading
                }..lParams(matchParent, matchParent)
            }
        }
    }

}