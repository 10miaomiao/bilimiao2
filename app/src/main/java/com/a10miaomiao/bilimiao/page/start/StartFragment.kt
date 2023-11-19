package com.a10miaomiao.bilimiao.page.start

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.android.view._backgroundColor
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._imageResource
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.flexboxLayout
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.loadImageUrl
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.closeSearchDrawer
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.footerViews
import com.a10miaomiao.bilimiao.comm.recycler.headerViews
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.shadowLayout
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.comm.wrapInNestedScrollView
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.bangumi.BangumiDetailFragment
import com.a10miaomiao.bilimiao.page.search.SearchStartFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehaviorDelegate
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.layout.SideSlideLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.bottomPadding
import splitties.views.dsl.core.editText
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.horizontalMargin
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.margin
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.radioButton
import splitties.views.dsl.core.space
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.verticalMargin
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInHorizontalScrollView
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityBottomCenter
import splitties.views.gravityCenter
import splitties.views.horizontalPadding
import splitties.views.imageResource
import splitties.views.padding
import splitties.views.verticalPadding

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
        private val ID_searchEditText = View.generateViewId()
        private val ID_searchCloseIcon = View.generateViewId()
        private val ID_suggestRecycler = View.generateViewId()

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
    private lateinit var mSearchEditText: EditText
    private lateinit var mSearchTextView: TextView
    private lateinit var mSearchCloseIconView: ImageView
    private lateinit var mSuggestRecycler: RecyclerView

    private val viewModel by diViewModel<StartViewModel>(di)
    private val supportHelper by instance<SupportHelper>()
    private val themeDelegate by instance<ThemeDelegate>()
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
        mSearchEditText = view.findViewById(ID_searchEditText)
        mSearchTextView = view.findViewById(ID_searchTextView)
        mSearchCloseIconView = view.findViewById(ID_searchCloseIcon)
        mSuggestRecycler = view.findViewById(ID_suggestRecycler)

        mAllRadioButton = view.findViewById(ID_radioButton_all)
        mSelfRadioButton = view.findViewById(ID_radioButton_self)
        mSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                viewModel.loadSuggestData(text, mSearchEditText)
            }
        })
        mAllRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        mSelfRadioButton.setOnCheckedChangeListener(handleCheckedChange)
//        val text = arguments?.getString(MainNavArgs.text)
//        if (text != null) {
//            mSearchTextView.setText(text)
//        }
    }

    fun setConfig(config: SearchConfigInfo?) {
        viewModel.config = config
        if (config == null) {
            mSearchEditText.setText("")
            mAllRadioButton.isChecked = true
            mSelfRadioButton.visibility = View.GONE
            viewModel.searchMode = 0
        } else {
            mSearchEditText.setText(config.keyword)
            mSearchEditText.setSelection(config.keyword.length)
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

    private fun showSoftInput() {
        supportHelper.showSoftInput(mSearchEditText)
    }

    private fun hideSoftInput() {
        supportHelper.hideSoftInput(mSearchEditText)
    }


    override fun onResume() {
        super.onResume()
        setConfig(viewModel.config)
//        supportHelper.showSoftInput(mEditText)
    }

    override fun onPause() {
        super.onPause()
//        supportHelper.hideSoftInput(mEditText)
    }

    fun onBackPressed(): Boolean {
        if (viewModel.searchFocus) {
            closeSearchView()
            return true
        }
        return false
    }

    fun onDrawerStateChanged(state: Int) {
        if (state == AppBarBehaviorDelegate.STATE_COLLAPSED) {
            closeSearchView()
        } else if (state == AppBarBehaviorDelegate.STATE_EXPANDED) {
            if (viewModel.searchFocus) {
                showSoftInput()
            }
        }
    }

    fun openSearchView() {
        viewModel.searchFocus = true
        mSuggestRecycler.visibility = View.VISIBLE
        mSearchEditText.visibility = View.VISIBLE
        mSearchCloseIconView.visibility = View.VISIBLE
        mSearchTextView.visibility = View.GONE
        (mSearchView.layoutParams as MarginLayoutParams).let {
            it.setMargins(0, 0, 0, 0)
        }
        mSearchView.radius = 0f
    }

    private fun closeSearchView() {
        hideSoftInput()
        viewModel.searchFocus = false
        mSuggestRecycler.visibility = View.GONE
        mSearchEditText.visibility = View.GONE
        mSearchCloseIconView.visibility = View.GONE
        mSearchTextView.visibility = View.VISIBLE
        mSearchView.radius = requireContext().dip(10f)
        (mSearchView.layoutParams as MarginLayoutParams).let {
            it.margin = config.largePadding
        }

    }

    private val handleEditorAction = TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
            && event.action == KeyEvent.ACTION_DOWN
        ) {
            handleSearchClick.onClick(v)
            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    }
    private val handleSearchClick = View.OnClickListener {
        val keyword = mSearchEditText.text.toString()
        viewModel.startSearch(keyword, it)
    }

    private val handleCloseClick = View.OnClickListener {
        val text = mSearchEditText.text.toString()
        if (text.isEmpty()) {
            closeSearchView()
//            scaffoldApp.closeSearchDrawer()
        } else {
            mSearchEditText.setText("")
        }
    }

    private val handleStartSearchClick = View.OnClickListener {
        openSearchView()
        showSoftInput()
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
            scaffoldApp.closeSearchDrawer()
        } else {
            val pageUrl = item.pageUrl
            nav.navigate(Uri.parse(pageUrl), navOptions)
            scaffoldApp.closeSearchDrawer()
        }
    }

    private val handleSuggestTagItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.data[position]
        if (item is StartViewModel.SuggestInfo) {
            when (item.type) {
                "SEARCH" -> {
                    val keyword = item.value
                    viewModel.startSearch(keyword, view)
                }

                "AV" -> {
                    scaffoldApp.closeSearchDrawer()
                    val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = VideoInfoFragment.createArguments(item.value)
                    nav.navigate(VideoInfoFragment.actionId, args)
                }

                "SS" -> {
                    scaffoldApp.closeSearchDrawer()
                    val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = BangumiDetailFragment.createArguments(item.value)
                    nav.navigate(BangumiDetailFragment.actionId, args)
                }

                else -> {
                    val keyword = item.text
                    viewModel.startSearch(keyword, view)
                }
            }
        }
    }

    private val handleHistoryTagItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.data[position]
        if (item is String) {
            viewModel.startSearch(item, view)
        }
    }

    private val handleHistoryTagItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = adapter.data[position]
        if (item is String) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("确认删除，喵？")
                setMessage("将删除搜索历史关键字“${item}”")
                setNegativeButton("确定") { dialog, which ->
                    viewModel.deleteSearchHistory(item)
                    PopTip.show("已删除”${item}“")
                }
                setPositiveButton("取消", null)
            }.show()
        }
        true
    }

    private val handleDeleteHistoryClick = View.OnClickListener {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("确认清空，喵？")
            setMessage("将清空搜索历史关键字")
            setNegativeButton("确定清空") { dialog, which ->
                viewModel.deleteAllSearchHistory()
                PopTip.show("已清空了喵")
            }
            setPositiveButton("取消", null)
        }.show()

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

    val itemHistoryTagUi = miaoBindingItemUi<String> { item, index ->
        tagItem(item)
    }

    val itemSuggestTagUi = miaoBindingItemUi<StartViewModel.SuggestInfo> { item, index ->
        horizontalLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            setBackgroundResource(config.selectableItemBackground)
            horizontalPadding = config.pagePadding
            verticalPadding = config.largePadding

            views {
                +textView {
                    _text = item.text
                    textSize = 16f
                }
            }
        }
    }

    fun MiaoUI.tagItem(
        text: String,
    ): View {
        return frameLayout {
            views {
                +frameLayout {
                    apply(ViewStyle.roundRect(dip(5)))
                    setBackgroundResource(config.selectableItemBackground)

                    views {
                        +textView {
                            backgroundColor = config.blockBackgroundColor
                            padding = dip(5)
                            _text = text
                        }
                    }
                }..lParams {
                    rightMargin = dip(8)
                    bottomMargin = dip(5)
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
                                +editText(ID_searchEditText) {
                                    _show = viewModel.searchFocus
                                    textSize = 18f
                                    hint = "请输入ID或关键字"
                                    horizontalPadding = iconSize + dip(15)
                                    setBackgroundResource(0)
                                    imeOptions = EditorInfo.IME_ACTION_SEARCH
                                    isSingleLine = true
                                    inputType = EditorInfo.TYPE_CLASS_TEXT
                                    setOnEditorActionListener(handleEditorAction)
                                }..lParams(matchParent, dip(60))
                                +textView(ID_searchTextView) {
                                    _show = !viewModel.searchFocus
                                    text = "请输入ID或关键字"
                                    setTextColor(config.foregroundAlpha45Color)
                                    horizontalPadding = iconSize + dip(15)
                                    textSize = 18f
                                    gravity = Gravity.CENTER_VERTICAL
                                    setOnClickListener(handleStartSearchClick)
                                }..lParams(matchParent, dip(60)) {
                                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                }

                                +imageView(ID_searchCloseIcon) {
                                    setImageResource(R.drawable.ic_close_grey_24dp)
                                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                                    setOnClickListener(handleCloseClick)
                                }..lParams(iconSize, iconSize) {
                                    rightMargin = config.pagePadding
                                    gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                                }

                                +imageView {
//                            imageTintList = ColorStateList.valueOf(config.white80)
                                    setImageResource(R.drawable.ic_search_24dp)
                                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                                    setOnClickListener(handleStartSearchClick)
                                }..lParams(iconSize, iconSize) {
                                    leftMargin = config.pagePadding
                                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
//                            setOnClickListener(handleSearchClick)
                                }
                            }
                        }..lParams(matchParent, dip(60))
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun MiaoUI.userView(): View {
        val userInfo = viewModel.userStore.state.info
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = config.largePadding

            views {
                +horizontalLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    views {
                        +imageView {
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
        frameLayout {
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
                    }
                    footerViews(mAdapter) {

                    }
                }..lParams(matchParent, matchParent)

                +recyclerView(ID_suggestRecycler) {
                    backgroundColor = config.windowBackgroundColor
                    visibility = View.GONE
                    bottomPadding = dip(100)

                    _miaoLayoutManage(
                        LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true)
                    )
                    _miaoAdapter(
                        items = viewModel.suggestList,
                        itemUi = itemSuggestTagUi,
                        depsAry = viewModel.suggestList.toTypedArray(),
                    ) {
                        setOnItemClickListener(handleSuggestTagItemClick)
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