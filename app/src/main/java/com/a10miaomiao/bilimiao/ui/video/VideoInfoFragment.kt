package com.a10miaomiao.bilimiao.ui.video

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.BiliVideoPageData
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.ValueManager
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.v
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.support.v4.toast


class VideoInfoFragment : SwipeBackFragment() {
    companion object {
        fun newInstance(id: String): VideoInfoFragment {
            val fragment = VideoInfoFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.TYPE, "AV")
            bundle.putString(ConstantUtil.ID, id)
            fragment.arguments = bundle
            return fragment
        }

        fun newInstanceByBvid(bvid: String): VideoInfoFragment {
            val fragment = VideoInfoFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.TYPE, "BV")
            bundle.putString(ConstantUtil.ID, bvid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: VideoInfoViewModel

//    private val aid by lazy { arguments!!.getString(ConstantUtil.ID) }

    private val videoPlayer by lazy { MainActivity.of(context!!).videoPlayerDelegate }
    private val playerStore by lazy { Store.from(context!!).playerStore }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel {
            val id = arguments!!.getString(ConstantUtil.ID)
            val type = arguments!!.getString(ConstantUtil.TYPE)
            VideoInfoViewModel(context!!, type, id)
        }
        return attachToSwipeBack(createUI().view)
    }

    private fun playVideo(cid: String, title: String) {
        val info = -viewModel.info
        if (info != null) {
            videoPlayer.playVideo(info.aid.toString(), cid, title)
        }
    }

    private fun downloadVideo() {
        val info = -viewModel.info
        if (info != null) {
            val downloadService = MainActivity.of(context!!)
                    .downloadDelegate
                    .downloadService
            val page = info.pages[0]
            val pageData = BiliVideoPageData(
                    cid = page.cid.toLong(),
                    page = page.page,
                    from = page.from,
                    part = page.part,
                    vid = page.vid,
                    has_alias = false,
                    tid = 0,
                    width = 0,
                    height = 0,
                    rotate = 0,
                    download_title = "视频已缓存完成",
                    download_subtitle = info.title
            )
            val biliVideoEntry = BiliVideoEntry(
                    media_type = 1,
                    has_dash_audio = false,
                    is_completed = false,
                    total_bytes = 0,
                    downloaded_bytes = 0,
                    title = info.title,
                    type_tag = "64",
                    cover = info.pic,
                    prefered_video_quality = 112,
                    guessed_total_bytes = 0,
                    total_time_milli = 0,
                    danmaku_count = 1000,
                    time_update_stamp = 1589831292571L,
                    time_create_stamp = 1589831261539L,
                    can_play_in_advance = true,
                    interrupt_transform_temp_file = false,
                    avid = info.aid,
                    spid = 0,
                    seasion_id = 0,
                    bvid = info.bvid,
                    owner_id = info.owner.mid,
                    page_data = pageData
            )
            downloadService.createDownload(biliVideoEntry)
            context!!.toast("创建成功")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getBoolean("player_auto_stop", true)) {
            val info = -viewModel.info
            if (videoPlayer.isAid(info?.aid.toString())) {
                videoPlayer.haederBehavior.hide()
                videoPlayer.stopPlay()
            }
        }
    }

    private fun createUI() = UI {
        val observeInfo = viewModel.info.observeNotNull()
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
                title("视频详情")
                observeInfo {
                    title(it!!.bvid)
                }

                inflateMenu(R.menu.video_info)
                onMenuItemClick(Toolbar.OnMenuItemClickListener {
                    if (it.itemId == R.id.download) {
                        val info = -viewModel.info
                        if (info == null) {
                            toast("数据未加载完成喵")
                        } else {
                            startFragment(CreateDownloadFragment.newInstance(
                                    info.aid.toString(),
                                    info.bvid,
                                    info.title,
                                    info.pic,
                                    info.owner.mid,
                                    viewModel.pages,
                                    0
                            ))
                        }
                    } else if (it.itemId == R.id.open){
                        val id = viewModel.id
                        try {
                            var intent = Intent(Intent.ACTION_VIEW)
                            var url = "bilibili://video/$id"
                            intent.data = Uri.parse(url)
                            startActivity(intent)
                        } catch (e: Exception) {
                            var intent = Intent(Intent.ACTION_VIEW)
                            var url = "http://www.bilibili.com/video/av$id"
                            intent.data = Uri.parse(url)
                            startActivity(intent)
                        }
                    }
                    true
                })
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (viewModel.loading.observe()){ isRefreshing = it }
                setOnRefreshListener { viewModel.loadData() }

                recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    setHasFixedSize(true)
                }.miao(viewModel.relates) {
                    addHeaderView {
                        headerView()
                    }
                    itemView { b ->
                        videoItemView(
                                b.itemValue { title },
                                b.itemValue { pic },
                                b.itemValue { owner?.name ?: "" },
                                b.itemValue { stat.view },
                                b.itemValue { stat.danmaku }
                        )
                    }
                    onItemClick { item, position ->
                        startFragment(VideoInfoFragment.newInstance(item.aid.toString()))
                    }
                }
            }.lparams {
                width = matchParent
                height = matchParent
                weight = 1f
            }

            view {
                backgroundColor = config.lineColor
            }.lparams {
                width = matchParent
                height = dip(1)
            }
            bottomView().lparams {
                width = matchParent
                height = wrapContent
            }
        }
    }

    fun ViewManager.headerView() {
        val observeInfo = viewModel.info.observeNotNull()
        verticalLayout {
            lparams {
                width = matchParent
                verticalPadding = dip(10)
            }

            linearLayout {
                rcImageView {
                    radius = dip(5)
                    observeInfo { network(it!!.pic) }
                    setOnClickListener {
                        val info = viewModel.info.value!!
                        playVideo(
                                info.cid.toString(),
                                info.title
                        )
                    }
                }.lparams {
                    width = dip(150)
                    height = dip(100)
                    rightMargin = dip(10)
                }
                verticalLayout {
                    // 标题
                    textView {
                        textSize = 16f
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textColor = config.foregroundColor
                        observeInfo {
                            text = it!!.title
                        }
                    }.lparams(weight = 1f)
                    //
                    linearLayout {
                        imageView {
                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                            setImageResource(R.drawable.ic_info_views)
                        }.lparams(dip(14), dip(14)) {
                            gravity = Gravity.CENTER
                        }
                        textView {
                            textSize = 12f
                            textColor = config.foregroundAlpha45Color
                            observeInfo {
                                text = NumberUtil.converString(it!!.stat.view)
                            }
                        }.lparams {
                            leftMargin = dip(3)
                            rightMargin = dip(16)
                        }

                        imageView {
                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                            setImageResource(R.drawable.ic_info_danmakus)
                        }.lparams(dip(14), dip(14)) {
                            gravity = Gravity.CENTER
                        }
                        textView {
                            textSize = 12f
                            textColor = config.foregroundAlpha45Color
                            observeInfo {
                                text = NumberUtil.converString(it!!.stat.danmaku)
                            }
                        }.lparams {
                            leftMargin = dip(3)
                            rightMargin = dip(16)
                        }
                    }.lparams {
                        width = matchParent
                        height = wrapContent
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }.lparams(matchParent, matchParent)

            }.lparams {
                horizontalMargin = dip(10)
            }

            upperView()

            // 视频简介
//            mySpannableTextView {
//                observeInfo {
//                    setLimitText(it!!.desc)
//                }
//                setOnAvTextClickListener { view, avId ->
//                    startFragment(VideoInfoFragment.newInstance(avId))
//                }
//            }.lparams {
//                horizontalMargin = dip(10)
//            }
            include<ExpandableTextView>(R.layout.layout_expandable) {
                observeInfo {
                    setContent(it!!.desc)
                }
                linkClickListener = ExpandableTextView.OnLinkClickListener { linkType, content, selfContent -> //根据类型去判断
                    when (linkType) {
                        LinkType.LINK_TYPE -> {
                            viewModel.toLink(content)
                        }
                        LinkType.MENTION_TYPE -> {
                            toast("你点击了@用户 内容是：$content")
                        }
                        LinkType.SELF -> {
                            viewModel.toLink(selfContent)
                        }
                    }
                }
            }.lparams {
                horizontalMargin = dip(10)
            }

            // 分P列表
            pageView().lparams {
                width = matchParent
                height = dip(48)
                margin = dip(10)
            }
        }
    }

    fun ViewManager.upperView() {
        val observeInfo = viewModel.info.observeNotNull()
        verticalLayout {
            // up主信息
            linearLayout {
                selectableItemBackground()
                lparams {
                    margin = dip(10)
                    width = matchParent
                }
                setOnClickListener {
                    val owner = viewModel.info.value!!.owner
                    startFragment(UserFragment.newInstance(owner.mid))
                }
                observeInfo {
                    visibility = if (it?.staff?.isEmpty() == false) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                rcImageView {
                    isCircle = true
                    observeInfo {
                        network(it!!.owner.face)
                    }
                }.lparams {
                    height = dip(40)
                    width = dip(40)
                }


                verticalLayout {
                    lparams {
                        leftMargin = dip(8)
                    }
                    textView {
                        textColor = config.foregroundColor
                        observeInfo {
                            text = it!!.owner.name
                        }
                    }
                    textView {
                        textSize = 12f
                        textColor = config.foregroundAlpha45Color
                        observeInfo {
                            text = "发表于 " + NumberUtil.converCTime(it!!.pubdate)
                        }
                    }
                }
            }

            recyclerView {
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
            }.miao(viewModel.info.miaoList { it?.staff }()) {
                itemView { b ->
                    verticalLayout {
                        lparams(dip(64), wrapContent)
                        verticalPadding = dip(10)
                        gravity = Gravity.CENTER
                        selectableItemBackground()

                        rcImageView {
                            isCircle = true
                            b.bind {
                                network(it.face)
                            }
                        }.lparams {
                            height = dip(40)
                            width = dip(40)
                        }

                        textView {
                            textColor = config.foregroundColor
                            gravity = Gravity.CENTER
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            b.bind {
                                text = it.name
                            }
                        }
                        textView {
                            textSize = 12f
                            textColor = config.themeColor
                            gravity = Gravity.CENTER
                            maxLines = 1
                            b.bind {
                                text = it.title
                            }
                        }
                    }
                }
                onItemClick { item, position ->
                    startFragment(UserFragment.newInstance(item.mid))
                }
            }
        }
    }

    fun ViewManager.pageView() = linearLayout {
        val observeInfo = viewModel.info.observeNotNull()
        val observePlayer = playerStore.observe()
        frameLayout {
            recyclerView {
                val lm = LinearLayoutManager(context)
                lm.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = lm
            }.lparams {
                width = matchParent
                height = matchParent
            }.miao(viewModel.pages) {
                itemView { b ->
                    pageItemView(
                            b.itemValue { cid },
                            b.itemValue { part }
                    )
                }
                onItemClick { item, position ->
                    playVideo(item.cid, item.part)
                }
                observePlayer {
                    notifyDataSetChanged()
                }
            }
            imageView {
                scaleType = ImageView.ScaleType.FIT_XY
                imageResource = R.drawable.shape_gradient
            }.lparams {
                gravity = Gravity.RIGHT
                width = dip(10)
                height = matchParent
            }
        }.lparams {
            width = matchParent
            height = matchParent
            weight = 1f
        }

        imageView {
            setImageResource(R.drawable.ic_navigate_next_black_24dp)
            selectableItemBackgroundBorderless()
            setOnClickListener {
                val fragment = PagesFragment.newInstance(viewModel.id, viewModel.pages, 0)
                MainActivity.of(context)
                        .showBottomSheet(fragment)
            }
            observeInfo {
                visibility = if (it!!.pages.size > 2) View.VISIBLE
                else View.GONE
            }
        }.lparams(dip(24), dip(24)) {
            gravity = Gravity.CENTER
        }
    }

    fun ViewManager.pageItemView(
            cidBind: ValueManager<String>,
            partBind: ValueManager<String>
    ) {
        frameLayout {
            backgroundResource = R.drawable.shape_corner
            lparams(wrapContent, matchParent) {
                rightMargin = dip(5)
            }
            textView {
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
                cidBind {
                    if (playerStore.info.cid == it) {
                        this@frameLayout.isEnabled = false
                        textColorResource = config.themeColorResource
                    } else {
                        this@frameLayout.isEnabled = true
                        textColorResource = R.color.text_black
                    }
                }
                partBind { text = it }
            }.lparams {
                gravity = Gravity.CENTER
            }
        }
    }

    fun ViewManager.videoItemView(
            titleBind: ValueManager<String>,
            picBind: ValueManager<String?>,
            uperNameBind: ValueManager<String>,
            viewNumBind: ValueManager<String>,
            danmakuNumBind: ValueManager<String>
    ) {
        linearLayout {
            lparams(matchParent, wrapContent)
            selectableItemBackground()
            padding = dip(5)

            rcImageView {
                radius = dip(5)
                picBind { if (it != null) network(it) }
            }.lparams {
                width = dip(140)
                height = dip(85)
                rightMargin = dip(5)
            }

            verticalLayout {
                textView {
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 2
                    textColor = config.foregroundColor
                    titleBind { text = it }
                }.lparams(matchParent, matchParent) {
                    weight = 1f
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL
                    imageView {
                        imageResource = R.drawable.icon_up
                    }.lparams {
                        width = dip(16)
                        rightMargin = dip(3)
                    }
                    textView {
                        textSize = 12f
                        textColor = config.foregroundAlpha45Color
                        uperNameBind { text = it }
                    }
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL
                    imageView {
                        imageResource = R.drawable.ic_play_circle_outline_black_24dp
                    }.lparams {
                        width = dip(16)
                        rightMargin = dip(3)
                    }
                    textView {
                        textSize = 12f
                        textColor = config.foregroundAlpha45Color
                        viewNumBind { text = NumberUtil.converString(it) }
                    }
                    space().lparams(width = dip(10))
                    imageView {
                        imageResource = R.drawable.ic_subtitles_black_24dp
                    }.lparams {
                        width = dip(16)
                        rightMargin = dip(3)
                    }
                    textView {
                        textSize = 12f
                        textColor = config.foregroundAlpha45Color
                        danmakuNumBind { text = NumberUtil.converString(it) }
                    }
                }

                linearLayout {

                }
            }.lparams(width = matchParent, height = matchParent)
        }
    }

    fun ViewManager.bottomView() = linearLayout {
        backgroundColor = config.blockBackgroundColor
        gravity = Gravity.END
        val itemWidth = dip(60)
        val click = View.OnClickListener {
            toast("施工中")
        }
        val info = viewModel.info
        bottomItemView(
                info.v {
                    if (it?.req_user?.like == null) {
                        R.drawable.ic_column_unlike
                    } else {
                        R.drawable.ic_column_like
                    }
                }(),
                info.v { NumberUtil.converString(it?.stat?.like?.toString() ?: "点赞") }(),
                viewModel.columnLikeClick
        ).lparams(weight = 1f)
        bottomItemView(
                info.v {
                    if (it?.req_user?.coin == null) {
                        R.drawable.ic_column_uncoin
                    } else {
                        R.drawable.ic_column_coin
                    }
                }(),
                info.v { NumberUtil.converString(it?.stat?.coin?.toString() ?: "投币") }(),
                viewModel.columnCoinClick
        ).lparams(weight = 1f)
        bottomItemView(
                R.drawable.ic_column_share.v(),
                info.v { NumberUtil.converString(it?.stat?.coin?.toString() ?: "分享") }(),
                viewModel.columnShareClick
        ).lparams(weight = 1f)
        bottomItemView(
                info.v {
                    if (it?.req_user?.favorite == null) {
                        R.drawable.ic_column_unstar
                    } else {
                        R.drawable.ic_column_star
                    }
                }(),
                info.v { NumberUtil.converString(it?.stat?.favorite?.toString() ?: "收藏") }(),
                click
        ).lparams(weight = 1f)
        bottomItemView(
                R.drawable.ic_column_comm.v(),
                info.v { NumberUtil.converString(it?.stat?.reply?.toString() ?: "评论") }(),
                viewModel.columnCommClick
        ).lparams(weight = 1f)
    }

    fun ViewManager.bottomItemView(
            iconBind: ValueManager<Int>,
            textBind: ValueManager<String>,
            onClick: View.OnClickListener
    ) = verticalLayout {
        gravity = Gravity.CENTER
        topPadding = dip(5)
        bottomPadding = dip(3)
        selectableItemBackgroundBorderless()
        setOnClickListener(onClick)

        imageView {
            iconBind {
                imageResource = it
            }
        }.lparams {
            height = dip(20)
            bottomMargin = dip(2)
        }
        textView {
            textSize = 10f
            textColor = config.foregroundAlpha45Color
            textBind {
                text = it
            }
        }.lparams(wrapContent, wrapContent)
    }

}


