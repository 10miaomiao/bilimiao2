package com.a10miaomiao.bilimiao.page.start


import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.activity.SearchActivity
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.connectStore
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.flexboxLayout
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.loadImageUrl
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.footerViews
import com.a10miaomiao.bilimiao.comm.recycler.headerViews
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.badgeTextView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.bottomPadding
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.margin
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.radioButton
import splitties.views.dsl.core.space
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInHorizontalScrollView
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.imageResource
import splitties.views.padding
import splitties.views.textColorResource

class StartFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "start"

        fun createArguments(): Bundle {
            return bundleOf()
        }

        fun createArguments(text: String): Bundle {
            return bundleOf(
                MainNavArgs.text to text
            )
        }

        private val ID_searchView = View.generateViewId()
        private val ID_searchTextView = View.generateViewId()
        private val ID_radioButton_all = View.generateViewId()
        private val ID_radioButton_self = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        title = "搜索"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private lateinit var mAllRadioButton: RadioButton
    private lateinit var mSelfRadioButton: RadioButton
    private lateinit var mSearchView: MaterialCardView
    private lateinit var mSearchTextView: TextView

    private val viewModel by lazy { StartViewModel(di) }
    private val supportHelper by instance<SupportHelper>()
    private val themeDelegate by instance<ThemeDelegate>()
    private val playerDelegate by instance<BasePlayerDelegate>()
    private val windowStore by instance<WindowStore>()
    private val scaffoldApp by lazy { requireActivity().getScaffoldView() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSearchView = view.findViewById(ID_searchView)
        mSearchTextView = view.findViewById(ID_searchTextView)
        mAllRadioButton = view.findViewById(ID_radioButton_all)
        mSelfRadioButton = view.findViewById(ID_radioButton_self)

        mAllRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        mSelfRadioButton.setOnCheckedChangeListener(handleCheckedChange)

        themeDelegate.observeTheme(this, Observer {
            // 切换主题时，颜色随之改变
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ),
                intArrayOf(config.foregroundAlpha45Color, config.themeColor)
            )
            mAllRadioButton.buttonTintList = colorStateList
            mSelfRadioButton.buttonTintList = colorStateList
        })
    }

    fun setConfig(config: SearchConfigInfo?) {
        viewModel.config = config
        if (config == null) {
            mAllRadioButton.isChecked = true
            mSelfRadioButton.visibility = View.GONE
            viewModel.searchMode = 0
        } else {
            mSearchTextView.text = config.keyword.ifBlank {
                "请输入ID或关键字"
            }
            if (config.name.isNotBlank()) {
                viewModel.searchMode = 1
                mSelfRadioButton.visibility = View.VISIBLE
                mSelfRadioButton.text = config.name
                mSelfRadioButton.isChecked = true
            } else {
                mAllRadioButton.isChecked = true
                mSelfRadioButton.visibility = View.GONE
                viewModel.searchMode = 0
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setConfig(viewModel.config)
    }

    override fun onPause() {
        super.onPause()
    }

    fun onBackPressed(): Boolean {
        return false
    }

    private val handlePlayerCardDetailClick = View.OnClickListener {
        val playerState = viewModel.playerStore.state
        val scaffoldView = requireActivity().getScaffoldView()
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        if (playerState.sid.isNotBlank()) {
            nav.navigateToCompose(BangumiDetailPage()) {
                id set playerState.sid
                epId set playerState.epid
            }
            scaffoldView.closeDrawer()
        } else if (playerState.aid.isNotBlank()) {
            val args = VideoInfoFragment.createArguments(playerState.aid)
            nav.navigate(VideoInfoFragment.actionId, args)
            scaffoldView.closeDrawer()
        }
    }

    private val handlePlayerCardCloseClick = View.OnClickListener {
        playerDelegate.closePlayer()
    }

    private val handleUserClick = View.OnClickListener {
        val scaffoldView = requireActivity().getScaffoldView()
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        val userStore = viewModel.userStore
        if (userStore.isLogin()) {
            val mid = userStore.state.info?.mid ?: return@OnClickListener
            val args = UserFragment.createArguments(mid.toString())
            nav.navigate(UserFragment.actionId, args)
        } else {
            nav.navigateToCompose(LoginPage())
        }
        scaffoldView.closeDrawer()
    }

    private val handleMessageClick = View.OnClickListener {
        val scaffoldView = requireActivity().getScaffoldView()
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        nav.navigateToCompose(MessagePage())
        scaffoldView.closeDrawer()
    }

    private val handleStartSearchClick = View.OnClickListener {
        val activity = requireActivity()
        val scaffoldView = activity.getScaffoldView()
        SearchActivity.launch(
            activity,
            viewModel.config?.keyword ?: "",
            if (mSelfRadioButton.isChecked) 1 else 0,
            viewModel.config?.name,
            mSearchView,
        )
        scaffoldView.closeDrawer()
    }

    private val handleNavItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.navList[position]
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.miao_fragment_open_enter)
            .setExitAnim(R.anim.miao_fragment_open_exit)
            .setPopEnterAnim(R.anim.miao_fragment_close_enter)
            .setPopExitAnim(R.anim.miao_fragment_close_exit)
            .build()
        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
        val scaffoldView = requireActivity().getScaffoldView()
        if (item.isNeedAuth) {
            val userInfo = viewModel.userStore.state.info
            if (userInfo == null) {
                PopTip.show("请先登录")
                return@OnItemClickListener
            }
            val pageUrl = item.pageUrl
                .replace("{mid}", userInfo.mid.toString())
                .replace("{name}", userInfo.name)
            nav.navigate(Uri.parse(pageUrl), navOptions)
            scaffoldView.closeDrawer()
        } else {
            val pageUrl = item.pageUrl
            nav.navigate(Uri.parse(pageUrl), navOptions)
            scaffoldView.closeDrawer()
        }
    }

    private val handleCheckedChange = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
        if (b) {
            if (compoundButton.id == ID_radioButton_all) {
                mSelfRadioButton.isChecked = false
                viewModel.searchMode = 0
            } else {
                mAllRadioButton.isChecked = false
                viewModel.searchMode = 1
            }
        }
    }

    @InternalSplittiesApi
    fun MiaoUI.playerStateCard(): View {
        val playerState = viewModel.playerStore.state
        return horizontalLayout {
            apply(ViewStyle.roundRect(dip(10)))
            padding = dip(10)
            backgroundColor = config.blockBackgroundColor

            _show = playerState.cid.isNotBlank()

            views {
                +rcImageView {
                    radius = dip(10)
                    _network(playerState.cover, "@300w_300h_1c_")
                }..lParams {
                    width = dip(60)
                    height = dip(60)
                    rightMargin = dip(10)
                }
                +verticalLayout {
                    views {
                        +horizontalLayout {
                            views {
                                +textView {
                                    text = "正在播放："
                                    setTextColor(config.foregroundColor)
                                    textSize = 16f
                                    gravity = Gravity.CENTER_VERTICAL
                                }..lParams(wrapContent,  dip(20))
                                +textView {
                                    _text = if (playerState.sid.isNotBlank()) {
                                        "SS${playerState.sid} / EP${playerState.epid}"
                                    } else {
                                        "AV${playerState.aid}"
                                    }
                                    ellipsize = TextUtils.TruncateAt.END
                                    maxLines = 1
                                    setTextColor(config.foregroundAlpha45Color)
                                    textSize = 14f
                                    gravity = Gravity.BOTTOM
                                }..lParams(wrapContent,  dip(20))
                            }
                        }..lParams(matchParent, wrapContent)
                        +textView {
                            _text = playerState.title
                            setTextColor(config.foregroundAlpha45Color)
                            textSize = 16f
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 2
//                            gravity = Gravity.CENTER_VERTICAL
                        }..lParams(matchParent, wrapContent) {
                            topMargin = dip(2)
                        }

                        +horizontalLayout {
                            views {
                                +view<MaterialButton> {
                                    text = "关闭播放"
                                    cornerRadius = dip(10)
                                    backgroundColor = config.blockBackgroundColor
                                    textColorResource = config.themeColorResource
                                    strokeColor = ColorStateList.valueOf(config.themeColor)
                                    strokeWidth = dip(1.5f).toInt()
                                    setOnClickListener(handlePlayerCardCloseClick)
                                    textSize = 14f
                                    padding = 0
                                }..lParams(dip(100), dip(40)) {
                                    rightMargin = dip(5)
                                }
                                +view<MaterialButton> {
                                    text = "查看详情"
                                    cornerRadius = dip(10)
                                    setTextColor(0xFFFFFFFF.toInt())
                                    setOnClickListener(handlePlayerCardDetailClick)
                                    textSize = 14f
                                    padding = 0
                                }..lParams(dip(100), dip(40))
                            }
                        }
                    }
                }..lParams {
                    weight = 1f
                }
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    fun MiaoUI.searchBoxView(): View {
        return view<MaterialCardView>(ID_searchView) {
            setCardBackgroundColor(config.blockBackgroundColor)
            strokeWidth = 0
            val iconSize = dip(30)
            radius = dip(10f)

            views {

                +verticalLayout {

                    views {

                        +horizontalLayout {
                            horizontalPadding = config.pagePadding
                            gravity = Gravity.CENTER_VERTICAL

                            views {
                                +radioButton(ID_radioButton_all) {
                                    text = "搜索全站"
                                    isChecked = true
                                }..lParams { rightMargin = config.dividerSize }
                                +radioButton(ID_radioButton_self) {
                                    text = ""
                                    visibility = View.GONE
                                }
                            }
                        }.wrapInHorizontalScrollView(
                            height = dip(40)
                        ) {

                        }..lParams(width = matchParent)

                        +frameLayout {

                            views {
                                +textView(ID_searchTextView) {
                                    text = "请输入ID或关键字"
                                    setTextColor(config.foregroundAlpha45Color)
                                    horizontalPadding = iconSize + dip(15)
                                    textSize = 18f
                                    gravity = Gravity.CENTER_VERTICAL
                                    setOnClickListener(handleStartSearchClick)
                                }..lParams(matchParent, dip(60)) {
                                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                }

                                +imageView {
                                    setImageResource(R.drawable.ic_search_24dp)
                                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                                    setOnClickListener(handleStartSearchClick)
                                }..lParams(iconSize, iconSize) {
                                    leftMargin = config.pagePadding
                                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                }
                            }
                        }..lParams(matchParent, dip(60))
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun MiaoUI.userView(): View {
        val contentInsets = windowStore.state.windowInsets
        val userInfo = viewModel.userStore.state.info
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = config.largePadding
            _topPadding = config.largePadding + contentInsets.top

            views {
                +horizontalLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    views {
                        +imageView {
                            setOnClickListener(handleUserClick)
                            miaoEffect(userInfo) {
                                if (it == null) {
                                    Glide.with(context)
                                        .load(com.a10miaomiao.bilimiao.R.mipmap.ic_launcher)
                                        .circleCrop()
                                        .into(this)
                                } else {
                                    Glide.with(context)
                                        .loadImageUrl(it.face)
                                        .circleCrop()
                                        .into(this)
                                }
                            }
                        }..lParams {
                            height = dip(60)
                            width = dip(60)
                        }

                        +space()..lParams {
                            weight = 1f
                        }

                        +frameLayout {
                            _show = viewModel.userStore.isLogin()
                            setOnClickListener(handleMessageClick)

                            views {
                                +imageView {
                                    imageResource = R.drawable.ic_message
                                }..lParams(dip(40), dip(40)) {
                                    gravity = Gravity.CENTER
                                }
                                +badgeTextView {
                                    _show = viewModel.showUnreadBadge()
                                    _text = viewModel.getUnreadCountText()
                                    setTextColor(0xFFFFFFFF.toInt())
                                }..lParams {
                                    gravity = Gravity.TOP or Gravity.RIGHT
                                }
                            }
                        }..lParams(dip(60), dip(60))

                    }
                }..lParams(matchParent, wrapContent) {
                    bottomMargin = config.pagePadding
                }


                +textView {
                    _text = userInfo?.name ?: "bilimiao"
                    setTextColor(config.foregroundColor)
                    textSize = 20f
                }..lParams {
                    width = matchParent
                    bottomMargin = config.smallPadding
                }

                +flexboxLayout {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    _show = userInfo != null

                    views {
                        +textView {
                            _text = "${userInfo?.following} 关注"
                            setTextColor(config.foregroundAlpha45Color)
                            textSize = 18f
                        }
                        +space()..lParams(dip(20))
                        +textView {
                            _text = "${userInfo?.follower} 粉丝"
                            setTextColor(config.foregroundAlpha45Color)
                            textSize = 18f
                        }
                    }
                }..lParams {
                    width = matchParent
                }
            }
        }
    }

    val itemUi = miaoBindingItemUi<StartViewModel.StartNavInfo> { item, index ->
        frameLayout {
            layoutParams = MarginLayoutParams(matchParent, dip(120)).apply {
                margin = config.smallPadding
            }
            apply(ViewStyle.roundRect(dip(10)))
            backgroundColor = config.blockBackgroundColor

            views {

                +verticalLayout {
                    padding = config.pagePadding
                    gravity = Gravity.CENTER
                    setBackgroundResource(config.selectableItemBackground)

                    views {
                        +imageView {
                            miaoEffect(listOf(item.iconRes, item.iconUrl)) {
                                if (item.iconRes != null) {
                                    Glide.with(context)
                                        .load(item.iconRes)
                                        .override(dip(40), dip(40))
                                        .into(this)
                                } else if (item.iconUrl != null) {
                                    Glide.with(context)
                                        .loadImageUrl(item.iconUrl!!)
                                        .override(dip(40), dip(40))
                                        .into(this)
                                }
                            }
                        }..lParams(dip(40), dip(40)) {
                            bottomMargin = config.pagePadding
                            gravity = Gravity.CENTER
                        }
                        +textView {
                            _text = item.title
                            setTextColor(config.foregroundColor)
                            gravity = Gravity.CENTER
                        }..lParams(wrapContent, wrapContent)
                    }
                }
            }
        }

    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        connectStore(viewLifecycleOwner, viewModel.userStore)
        connectStore(viewLifecycleOwner, viewModel.messageStore)
        connectStore(viewLifecycleOwner, viewModel.playerStore)
        val contentInsets = windowStore.state.windowInsets

        frameLayout {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            _bottomPadding = contentInsets.bottom

            views {

                +recyclerView {
                    bottomPadding = dip(150)
                    clipToPadding = false
                    backgroundColor = config.windowBackgroundColor
                    scrollBarSize = 0

                    _miaoLayoutManage(
                        GridAutofitLayoutManager(
                            requireContext(),
                            dip(90)
                        )
                    )

                    val mAdapter = _miaoAdapter(
                        items = viewModel.navList,
                        itemUi = itemUi,
                    ) {
                        setOnItemClickListener(handleNavItemClick)
                    }
                    headerViews(mAdapter) {
                        +userView()..lParams(width = matchParent) {
                            bottomMargin = config.smallPadding
                        }
                        +playerStateCard()..lParams(width = matchParent) {
                            margin = config.smallPadding
                        }
                    }
                    footerViews(mAdapter) {

                    }
                }..lParams(matchParent, matchParent)

                +searchBoxView()..lParams {
                    height = wrapContent
                    width = matchParent
                    gravity = Gravity.BOTTOM
                    margin = config.largePadding
                }

            }
        }


    }

}