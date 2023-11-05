package com.a10miaomiao.bilimiao.page.start

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.flexboxLayout
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.closeSearchDrawer
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.comm.wrapInNestedScrollView
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.bangumi.BangumiDetailFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehaviorDelegate
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import com.a10miaomiao.bilimiao.widget.layout.SideSlideLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
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
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.radioButton
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.verticalMargin
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.padding

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

        private val ID_editText = View.generateViewId()
        private val ID_radioButton_all = View.generateViewId()
        private val ID_radioButton_self = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        title = "搜索"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private lateinit var mEditText: EditText
    private lateinit var mAllRadioButton: RadioButton
    private lateinit var mSelfRadioButton: RadioButton

    private val viewModel by diViewModel<StartViewModel>(di)
    private val supportHelper by instance<SupportHelper>()
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
        mEditText = view.findViewById(ID_editText)
        mAllRadioButton = view.findViewById(ID_radioButton_all)
        mSelfRadioButton = view.findViewById(ID_radioButton_self)
        mEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                viewModel.loadSuggestData(text, mEditText)
            }
        })
        mAllRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        mSelfRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        val text = arguments?.getString(MainNavArgs.text)
        if (text != null) {
            mEditText.setText(text)
        }
    }

    fun setConfig(config: SearchConfigInfo?) {
        viewModel.config = config
        if (config == null) {
            mEditText.setText("")
            mAllRadioButton.isChecked = true
            mSelfRadioButton.visibility = View.GONE
            viewModel.searchMode = 0
        } else {
            mEditText.setText(config.keyword)
            mEditText.setSelection(config.keyword.length)
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

    fun showSoftInput() {
        supportHelper.showSoftInput(mEditText)
    }

    fun hideSoftInput() {
        supportHelper.hideSoftInput(mEditText)
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

    fun onDrawerStateChanged(state: Int) {
        if (state == AppBarBehaviorDelegate.STATE_COLLAPSED) {
            hideSoftInput()
            mEditText.isEnabled = false
        } else {
            mEditText.isEnabled = true
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
        val keyword = mEditText.text.toString()
        viewModel.startSearch(keyword, it)
    }
    private val handleCloseClick = View.OnClickListener {
        val text = mEditText.text.toString()
        if (text.isEmpty()) {
            scaffoldApp.closeSearchDrawer()
        } else {
            mEditText.setText("")
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
//                    Navigation.findNavController(view).popBackStack()
                    val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = VideoInfoFragment.createArguments(item.value)
                    nav.navigate(actionId, args)
                }

                "SS" -> {
                    scaffoldApp.closeSearchDrawer()
//                    Navigation.findNavController(view).popBackStack()
                    val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = BangumiDetailFragment.createArguments(item.value)
                    nav.navigate(actionId, args)
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
        tagItem(item.text)
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

    fun MiaoUI.secrchBoxView(): View {
        return frameLayout {
            backgroundColor = config.blockBackgroundColor
            bottomPadding = config.dividerSize
            horizontalPadding = config.pagePadding

            views {
                val iconSize = dip(24)
                +editText(ID_editText) {
                    hint = "请输入ID或关键字"
                    horizontalPadding = iconSize + dip(15)

                    imeOptions = EditorInfo.IME_ACTION_SEARCH
                    isSingleLine = true
                    inputType = EditorInfo.TYPE_CLASS_TEXT

                    setOnEditorActionListener(handleEditorAction)
                }..lParams(matchParent, dip(48))
                +imageView {
                    setImageResource(R.drawable.ic_search_24dp)
                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                }..lParams(iconSize, iconSize) {
                    leftMargin = config.dividerSize
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                    setOnClickListener(handleSearchClick)
                }
                +imageView {
                    setImageResource(R.drawable.ic_close_grey_24dp)
                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                    setOnClickListener(handleCloseClick)
                }..lParams(iconSize, iconSize) {
                    rightMargin = config.dividerSize
                    gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                }
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        verticalLayout {
            views {
                +flexboxLayout {
                    horizontalPadding = config.pagePadding
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    backgroundColor = config.blockBackgroundColor

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
                }..lParams(width = matchParent)
                +secrchBoxView()..lParams(width = matchParent)

                +verticalLayout {

                    views {
                        +recyclerView {
                            isNestedScrollingEnabled = false
                            _miaoLayoutManage(
                                FlexboxLayoutManager(requireActivity()).apply {
                                    flexDirection = FlexDirection.ROW
                                    flexWrap = FlexWrap.WRAP
                                }
                            )
                            _miaoAdapter(
                                items = viewModel.suggestList,
                                itemUi = itemSuggestTagUi,
                            ) {
                                setOnItemClickListener(handleSuggestTagItemClick)
                            }
                        }..lParams(matchParent, wrapContent) {
                            horizontalMargin = config.pagePadding
                            verticalMargin = config.dividerSize
                        }

                        +horizontalLayout {
                            gravity = Gravity.CENTER_HORIZONTAL
                            _show = viewModel.historyList.isNotEmpty()

                            views {
                                +textView {
                                    textSize = 16f
                                    text = "最近搜索"
                                }..lParams(matchParent, wrapContent) {
                                    weight = 1f
                                }

                                +textView {
                                    text = "删除全部"
                                    alpha = 0.75f
                                    setBackgroundResource(config.selectableItemBackgroundBorderless)
                                    setOnClickListener(handleDeleteHistoryClick)
                                }..lParams(wrapContent, wrapContent)
                            }
                        }..lParams(matchParent, wrapContent) {
                            horizontalMargin = config.pagePadding
                            bottomMargin = config.dividerSize
                        }

                        +recyclerView {
                            isNestedScrollingEnabled = false
                            _miaoLayoutManage(
                                FlexboxLayoutManager(requireActivity()).apply {
                                    flexDirection = FlexDirection.ROW
                                    flexWrap = FlexWrap.WRAP
                                }
                            )
                            _miaoAdapter(
                                items = viewModel.historyList,
                                itemUi = itemHistoryTagUi,
                            ) {
                                setOnItemClickListener(handleHistoryTagItemClick)
                                setOnItemLongClickListener(handleHistoryTagItemLongClick)
                            }
                        }..lParams(matchParent, wrapContent) {
                            horizontalMargin = config.pagePadding
                            verticalMargin = config.dividerSize
                        }
                    }
                }.wrapInNestedScrollView {
                    backgroundColor = config.windowBackgroundColor
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
            }
        }

    }

}