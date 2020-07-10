package com.a10miaomiao.bilimiao.ui.user

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.mediaItemView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.upper.UpperChannelVideoListFragment
import com.a10miaomiao.bilimiao.ui.upper.UpperVideoListFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.ValueManager
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.v
import com.bumptech.glide.Glide
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.*


class UserFragment : SwipeBackFragment() {

    private val vmid by lazy { arguments!!.getLong(ConstantUtil.ID) }

    companion object {
        fun newInstance(vmid: Long): UserFragment {
            val fragment = UserFragment()
            val bundle = Bundle()
            bundle.putLong(ConstantUtil.ID, vmid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var userStore: UserStore
    private lateinit var viewModel: UserViewModel
    private lateinit var filterStore: FilterStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userStore = Store.from(context!!).userStore
        filterStore = Store.from(context!!).filterStore
        viewModel = getViewModel { UserViewModel(context!!, vmid) }
        viewModel.noLike set !filterStore.filterUpper(vmid)
        return attachToSwipeBack(createUI().view)
    }

    private fun logout() {
        alert("确定退出登录？") {
            negativeButton("退出登录") {
                Bilimiao.app.deleteAuth()
                userStore.setUserInfo(null)
                pop()
                toast("已退出登陆")
            }
            positiveButton("取消") { }
        }.show()
    }

    private val favClick = View.OnClickListener {
        startFragment(FavFragment.newInstance(vmid))
    }
    private val bangumiClick = View.OnClickListener {
        if (userStore.isSelf(vmid)) {
            startFragment(MyBangumiFragment.newInstance(vmid))
        } else {
            startFragment(UserBangumiFragment.newInstance(vmid))
        }
    }
    private val followClick = View.OnClickListener {
        startFragment(UserFollowFragment.newInstance(UserFollowFragment.TYPE_FOLLOW, vmid))
    }
    private val fansClick = View.OnClickListener {
        startFragment(UserFollowFragment.newInstance(UserFollowFragment.TYPE_FANS, vmid))
    }


    private val archiveClick = View.OnClickListener {
        val info = -viewModel.dataInfo
        if (info != null) {
            startFragment(UpperVideoListFragment.newInstance(vmid, info.card.name))
        } else {
            toast("操作失败，信息未加载完成")
        }
    }


    private fun createUI() = UI {
        val observeLoading = +viewModel.loading
        verticalLayout {
            backgroundColor = config.windowBackgroundColor

            headerView {
                title("个人信息")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
                (+viewModel.dataInfo){
                    it?.let {
                        title(it.card.name)
                    }
                }


                if (userStore.isSelf(vmid)) {
                    addMenu("退出登录")
                    setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
                        logout()
                        true
                    })
                } else {
                    addMenu("屏蔽该up主").apply {
                        (+viewModel.noLike){
                            title = if (it) {
                                "取消屏蔽该up主"
                            } else {
                                "屏蔽该up主"
                            }
                        }
                    }
                    setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
                        val userInfo = -viewModel.dataInfo
                        if (-viewModel.noLike) {
                            filterStore.deleteUpper(vmid)
                            viewModel.noLike set false
                        } else if (userInfo != null) {
                            filterStore.addUpper(vmid, userInfo.card.name)
                            viewModel.noLike set true
                        } else {
                            toast("操作失败，信息未加载完成")
                        }
                        true
                    })
                }
            }
            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                observeLoading { isRefreshing = it }
                setOnRefreshListener { viewModel.loadData() }

                nestedScrollView {
                    createBody()
                }
            }.lparams {
                height = matchParent
                width = matchParent
            }
        }
    }

    private fun ViewManager.createBody() = verticalLayout {
        verticalLayout {
            backgroundColor = config.blockBackgroundColor
            applyRecursively(ViewStyle.roundRect(dip(5)))

            frameLayout {
                imageView {
                    (+viewModel.dataInfo){
                        Glide.with(context)
                                .run {
                                    if (it == null || it.images.imgUrl.isEmpty())
                                        load(R.drawable.top_bg1)
                                    else
                                        loadPic(it.images.imgUrl)
                                }
                                .centerCrop()
                                .dontAnimate()
                                .into(this)
                    }
                }.lparams(matchParent, dip(80))
                linearLayout {
                    val observerUser = viewModel.dataInfo.v { it?.card }()

                    padding = dip(10)

                    rcImageView {
                        isCircle = true
                        observerUser {
                            network(it?.face ?: "")
                        }
                    }.lparams(dip(64), dip(64)) {
                        rightMargin = dip(10)
                    }
                    verticalLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        textView {
                            observerUser {
                                text = it?.name ?: ""
                            }
                            textSize = 20f
                            textColor = config.foregroundColor
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1
                        }.lparams {
                            topMargin = dip(24)
                            bottomMargin = dip(2)
                        }
                        textView {
                            observerUser {
                                text = it?.sign ?: ""
                            }
//                            ellipsize = TextUtils.TruncateAt.END
//                            maxLines = 1
                        }
                    }.lparams(matchParent, wrapContent)
                }.lparams(matchParent, wrapContent) {
                    topMargin = dip(48)
                }
            }.lparams(matchParent, wrapContent)


            linearLayout {
                createNavItem(
                        "投稿".v(),
                        viewModel.dataInfo.v { it?.archive?.count ?: 0 }(),
                        onClick = archiveClick,
                        isShow = viewModel.dataInfo.v { it?.tab?.archive ?: false }()
                ).lparams { weight = 1f }
                createNavItem(
                        "追番".v(),
                        viewModel.dataInfo.v { it?.season?.count ?: 0 }(),
                        onClick = bangumiClick,
                        isShow = viewModel.dataInfo.v { it?.tab?.bangumi ?: false }()
                ).lparams { weight = 1f }
                createNavItem(
                        "收藏".v(),
                        viewModel.dataInfo.v { it?.favourite?.count ?: 0 }(),
                        onClick = favClick,
                        isShow = viewModel.dataInfo.v { it?.tab?.favorite ?: false }()
                ).lparams { weight = 1f }
                createNavItem(
                        "关注".v(),
                        viewModel.dataInfo.v { it?.card?.attention ?: 0 }(),
                        onClick = followClick
                ).lparams { weight = 1f }
                createNavItem(
                        "粉丝".v(),
                        viewModel.dataInfo.v { it?.card?.fans ?: 0 }(),
                        onClick = fansClick
                ).lparams { weight = 1f }
            }.lparams(matchParent, wrapContent)
        }.lparams(matchParent, wrapContent) {
            margin = dip(10)
        }

        val subject = if (userStore.isSelf(vmid)) "我" else "Ta"

        createMediaList(
                "${subject}的投稿",
                archiveClick,
                isShow = viewModel.dataInfo.v { it?.tab?.archive ?: false }()
        ) {
            miao(viewModel.dataInfo.miaoList { it?.archive?.item }()) {
                itemView {
                    createArchiveItem(
                            it.itemValue { cover },
                            it.itemValue { title }
                    )
                }
                onItemClick { item, position ->
                    startFragment(VideoInfoFragment.newInstance(item.param))
                }
            }
        }

        createMediaList(
                "${subject}的频道",
                isShow = viewModel.channelList.v { it.isNotEmpty() }()
        ) {
            miao(viewModel.channelList.miaoList { it }()) {
                itemView {
                    frameLayout {
                        lparams(dip(180), wrapContent)
                        mediaItemView(
                                it.itemValue { cover },
                                it.itemValue { name },
                                it.itemValue { "共${count}个视频" }
                        )
                    }
                }
                onItemClick { item, position ->
                    startFragment(UpperChannelVideoListFragment.newInstance(item))
                }
            }
        }

        createMediaList(
                "${subject}的追番",
                bangumiClick,
                isShow = viewModel.dataInfo.v { it?.tab?.bangumi ?: false }()) {
            miao(viewModel.dataInfo.miaoList { it?.season?.item }()) {
                itemView {
                    createSeasonItem(
                            it.itemValue { cover },
                            it.itemValue { title }
                    )
                }
                onItemClick { item, position ->
                    startFragment(BangumiFragment.newInstance(item.param))
                }
            }
        }

        createMediaList(
                "${subject}的收藏",
                favClick,
                isShow = viewModel.dataInfo.v { it?.tab?.favorite ?: false }()) {
            miao(viewModel.dataInfo.miaoList { it?.favourite?.item }()) {
                itemView {
                    frameLayout {
                        lparams(dip(180), wrapContent)
                        mediaItemView(
                                it.itemValue { if (cover != null && cover.isNotEmpty()) cover[0].pic else null },
                                it.itemValue { name },
                                it.itemValue { "共${cur_count}个视频" }
                        )
                    }

                }
                onItemClick { item, position ->
                    startFragment(FavDetailsFragment.newInstance(item.media_id, item.name))
                }
            }
        }


        createMediaList(
                "${subject}推荐的",
                isShow = viewModel.dataInfo.v { it?.tab?.like ?: false }()) {
            miao(viewModel.dataInfo.miaoList { it?.like_archive?.item }()) {
                itemView {
                    createArchiveItem(
                            it.itemValue { cover },
                            it.itemValue { title }
                    )
                }
                onItemClick { item, position ->
                    startFragment(VideoInfoFragment.newInstance(item.param))
                }
            }
        }
    }

    private fun ViewManager.createNavItem(
            title: ValueManager<String>,
            number: ValueManager<Int>,
            onClick: View.OnClickListener? = null,
            isShow: ValueManager<Boolean> = true.v()
    ) = verticalLayout {
        gravity = Gravity.CENTER
        padding = dip(10)
        selectableItemBackground()
        isShow { visibility = if (it) View.VISIBLE else View.GONE }
        onClick?.let { setOnClickListener(it) }

        textView {
            title { text = it }
            textSize = 18f
        }.lparams {
            bottomMargin = dip(3)
        }
        textView {
            textSize = 12f
            number { text = NumberUtil.converString(it.toString()) }
        }.lparams(wrapContent, wrapContent)
    }

    private fun ViewManager.createMediaList(
            title: String,
            moreClick: View.OnClickListener? = null,
            isShow: ValueManager<Boolean> = true.v(),
            recyclerInit: RecyclerView.() -> Unit
    ) {
        linearLayout {
            horizontalPadding = dip(10)
            verticalPadding = dip(5)
            gravity = Gravity.CENTER_VERTICAL
            isShow { visibility = if (it) View.VISIBLE else View.GONE }

            textView(title) {
                textSize = 20f
            }.lparams {
                weight = 1f
                height = wrapContent
                width = matchParent
            }

            if (moreClick != null) {
                textView("更多 >") {
                    textColor = config.themeColor
                    textSize = 14f
                    selectableItemBackgroundBorderless()
                    setOnClickListener(moreClick)
                }
            }
        }

        recyclerView {
            isShow { visibility = if (it) View.VISIBLE else View.GONE }
            layoutManager = LinearLayoutManager(context!!).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            recyclerInit()
        }
    }

    private fun ViewManager.createSeasonItem(
            cover: ValueManager<String>,
            title: ValueManager<String>
    ) {
        verticalLayout {
            padding = dip(5)
            selectableItemBackground()
            lparams(dip(120), wrapContent)

            // 固定宽高比例 11:14
            constraintLayout {
                rcImageView {
                    radius = dip(5)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    cover { network(it) }
                }.lparams {
                    width = 0
                    height = 0
                    leftToLeft = PARENT_ID
                    rightToRight = PARENT_ID
                    dimensionRatio = "11:14"
                }
            }.lparams(matchParent, wrapContent)


            textView {
                textSize = 16f
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
                textColor = config.foregroundColor
                title { text = it }
            }.lparams {
                verticalMargin = dip(5)
            }
        }
    }

    private fun ViewManager.createArchiveItem(
            cover: ValueManager<String>,
            title: ValueManager<String>
    ) {
        verticalLayout {
            padding = dip(5)
            selectableItemBackground()
            lparams(dip(180), wrapContent)

            rcImageView {
                radius = dip(5)
                scaleType = ImageView.ScaleType.FIT_CENTER
                cover { network(it) }
            }.lparams(matchParent, dip(100))

            textView {
                textSize = 16f
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
                textColor = config.foregroundColor
                title { text = it }
            }.lparams {
                verticalMargin = dip(5)
            }
        }
    }

}