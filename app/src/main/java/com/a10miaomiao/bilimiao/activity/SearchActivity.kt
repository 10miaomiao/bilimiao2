package com.a10miaomiao.bilimiao.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.newViewModelFactory
import com.a10miaomiao.bilimiao.comm.recycler.MiaoBindingAdapter
import com.a10miaomiao.bilimiao.comm.recycler.MiaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.launch
import org.kodein.di.DI
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent
import splitties.views.horizontalPadding
import splitties.views.imageResource
import splitties.views.rightPadding
import splitties.views.verticalPadding

class SearchActivity : AppCompatActivity() {

    companion object {
        const val KEY_KEYWORD = "keyword"
        const val KEY_MODE = "mode"
        const val KEY_NAME = "name"
        const val KEY_URL = "url"
        const val KEY_COMPOSE_ENTRY = "entry"
        const val KEY_COMPOSE_PARAM = "param"
        const val KEY_IS_COMPOSE_PAGE = "is_compose_page"
        const val REQUEST_CODE = 1234

        fun launch(
            activity: Activity,
            keyword: String,
            mode: Int,
            selfSearchName: String?,
            shareElement: View?,
        ) {
            val intent = Intent(activity, SearchActivity::class.java)
            val options = shareElement?.let {
                ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    android.util.Pair(it, "shareElement"),
                ).toBundle()
            }
            intent.putExtra(KEY_KEYWORD, keyword)
            intent.putExtra(KEY_MODE, mode)
            intent.putExtra(KEY_NAME, selfSearchName)
            activity.startActivityForResult(intent, REQUEST_CODE, options)
        }
    }

    private val di: DI = DI.lazy {}

    private val ui: SearchActivityUi by lazy {
        val arguments = intent.extras ?: Bundle()
        val keyword = arguments.getString(KEY_KEYWORD, "")
        val mode = arguments.getInt(KEY_MODE)
        val selfSearchName = arguments.getString(KEY_NAME)
        SearchActivityUi(this, themeDelegate, keyword, mode, selfSearchName)
    }

    private val viewModel by viewModels<SearchViewModel> {
        newViewModelFactory {
            SearchViewModel(this@SearchActivity)
        }
    }

    private val themeDelegate by lazy {
        ThemeDelegate(this@SearchActivity, di)
    }

    private val supportHelper by lazy {
        SupportHelper(this@SearchActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawableResource(R.drawable.gradient_reverse)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.enterTransition = Slide()
        window.exitTransition = Slide()
        super.onCreate(savedInstanceState)
        themeDelegate.onCreate(savedInstanceState)
        setContentView(ui.root)
        initSearchBoxView()
        initSuggestRecycler()
    }

    override fun onStart() {
        super.onStart()
        supportHelper.showSoftInput(ui.searchEditText)
    }

    override fun onStop() {
        super.onStop()
        supportHelper.hideSoftInput(ui.searchEditText)
    }

    private fun startSearch(keyword: String) {
        if (keyword.isEmpty()) {
            PopTip.show("请输入ID或关键字")
            return
        }
        viewModel.addSearchHistory(keyword)
        val intent = Intent()
        intent.putExtra(KEY_MODE, if (ui.allRadioButton.isChecked) 0 else 1)
        intent.putExtra(KEY_KEYWORD, keyword)
        setResult(REQUEST_CODE, intent)
        finishAfterTransition()
    }

    private val handleCheckedChange = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
        if (b) {
            if (compoundButton == ui.allRadioButton) {
                ui.selfRadioButton.isChecked = false
                viewModel.searchMode = 0
            } else {
                ui.allRadioButton.isChecked = false
                viewModel.searchMode = 1
            }
        }
    }


    private fun initSearchBoxView() {
        ui.allRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        ui.selfRadioButton.setOnCheckedChangeListener(handleCheckedChange)
        ui.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                viewModel.loadSuggestData(text, ui.searchEditText)
            }
        })
        ui.searchEditText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (event?.action == KeyEvent.ACTION_DOWN
                && actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val keyword = ui.searchEditText.text.toString()
                startSearch(keyword)
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
        ui.searchIcon.setOnClickListener {
            val keyword = ui.searchEditText.text.toString()
            startSearch(keyword)
        }
        ui.searchCloseIcon.setOnClickListener {
            val text = ui.searchEditText.text.toString()
            if (text.isEmpty()) {
                finishAfterTransition()
            } else {
                ui.searchEditText.setText("")
            }
        }
    }

    private fun showDeleteAllHistoryDialog() {
        MaterialAlertDialogBuilder(this@SearchActivity).apply {
            setTitle("确认清空，喵？")
            setMessage("将清空搜索历史关键字")
            setPositiveButton("确定清空") { _, _ ->
                viewModel.deleteAllSearchHistory()
                PopTip.show("已清空了喵")
            }
            setNegativeButton("取消", null)
        }.show()
    }

    private val handleHistoryDeleteItemClick = View.OnClickListener { view ->
        val text = view.tag as? String ?: return@OnClickListener
        MaterialAlertDialogBuilder(this@SearchActivity).apply {
            setTitle("确认删除，喵？")
            setMessage("将删除搜索历史关键字“${text}”")
            setPositiveButton("确定") { _, _ ->
                viewModel.deleteSearchHistory(text)
                PopTip.show("已删除”${text}“")
            }
            setNegativeButton("取消", null)
            setNeutralButton("清空全部") { _, _ ->
                showDeleteAllHistoryDialog()
            }
        }.show()
    }

    private fun createAdapter(
        itemUi: MiaoBindingItemUi<SearchViewModel.SuggestInfo>
    ): MiaoBindingAdapter<SearchViewModel.SuggestInfo> {
        return object : MiaoBindingAdapter<SearchViewModel.SuggestInfo>(
            viewModel.suggestList,
            itemUi,
        ) {}
    }

    private fun initSuggestRecycler() = ui.suggestRecycler.apply {
        val mAdapter = createAdapter(miaoBindingItemUi { item, index ->
            horizontalLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                setBackgroundResource(config.selectableItemBackground)
                horizontalPadding = config.pagePadding
                verticalPadding = config.largePadding
                gravity = Gravity.CENTER_VERTICAL

                views {
                    +imageView {
                        _show = item.type == SearchViewModel.SuggestType.HISTORY
                        imageResource = R.drawable.ic_history_gray_24dp
                        rightPadding = config.pagePadding
                    }
                    +textView {
                        _text = item.text
                        textSize = 16f
                        rightPadding = config.pagePadding
                    }..lParams(matchParent, wrapContent) {
                        weight = 1f
                    }
                    +imageView {
                        _show = item.type == SearchViewModel.SuggestType.HISTORY
                        _tag = item.value
                        imageResource = R.drawable.ic_baseline_delete_outline_24
                        setBackgroundResource(config.selectableItemBackgroundBorderless)
                        setOnClickListener(handleHistoryDeleteItemClick)
                    }
                }
            }
        })
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item = adapter.data[position]
            if (item is SearchViewModel.SuggestInfo) {
                when (item.type) {
                    SearchViewModel.SuggestType.AV -> {
                        val intent = Intent()
                        intent.putExtra(KEY_URL, "bilimiao://video/${item.value}")
                        setResult(REQUEST_CODE, intent)
                        finishAfterTransition()
                    }
                    SearchViewModel.SuggestType.SS -> {
                        val intent = Intent()
                        intent.putExtra(KEY_URL, "bilimiao://bangumi/${item.value}")
                        setResult(REQUEST_CODE, intent)
                        finishAfterTransition()
                    }
                    else -> {
                        startSearch(item.value)
                    }
                }
            }
        }
        adapter = mAdapter
        lifecycleScope.launch {
            viewModel.suggestListFlow.collect {
                mAdapter.setList(it)
            }
        }
//        mAdapter.setList(items)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration: Configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }

}