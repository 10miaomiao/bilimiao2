package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentStatePagerAdapter
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import kotlinx.android.synthetic.main.fragment_search.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import com.a10miaomiao.bilimiao.ui.widget.flow.FlowAdapter
import com.a10miaomiao.bilimiao.ui.widget.flow.FlowLayout
import com.a10miaomiao.bilimiao.utils.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert


class SearchFragment : SwipeBackFragment() {

    companion object {
        val keyword = MutableLiveData<String>()

        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var viewModel: SearchViewModel

    val fragments = listOf(SearchResultFragment(), BangumiResultFragment(), UpperResultFragment())
    val titles = listOf("视频", "番剧", "UP主")
    private val historyAdapter by lazy { MyAdapter(viewModel.historyList) }
    private val suggestAdapter by lazy { MyAdapter(viewModel.suggestList) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_search, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, newViewModelFactory {
            SearchViewModel(context!!)
        }).get(SearchViewModel::class.java)
        initView()
        initToolbar()
        initSearchBox()
        viewModel.showSearchBox.observe(this, Observer { show ->
            if (show!!) {
                search_box_layout.visibility = View.VISIBLE
                showSoftInput(et_search_keyword)
            } else {
                search_box_layout.visibility = View.GONE
                hideSoftInput()
            }
        })
        viewModel.keyword.observe(this, Observer { keyword ->
            if (et_search_keyword.text.toString() != keyword) {
                et_search_keyword.setText(keyword)
                et_search_keyword.setSelection(keyword!!.length)
            }
            tv_search_keyword.text = keyword
            viewModel.loadSuggestData(suggestAdapter) // 加载提示
        })
    }

    private fun initView() {
        initToolbar()
        val mAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int) = fragments[p0]
            override fun getCount() = fragments.size
            override fun getPageTitle(position: Int) = titles[position]
        }
        mViewPager.adapter = mAdapter
        mTabLayout.setTabsFromPagerAdapter(mAdapter)
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        mTabLayout.setupWithViewPager(mViewPager)
    }

    private fun initToolbar() {
        val statusBarHeight = getStatusBarHeight()
        app_bar_layout.setPadding(0, statusBarHeight, 0, 0)
        app_bar_layout2.setPadding(0, statusBarHeight, 0, 0)
//        toolbar.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { pop() }
        toolbar2.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar2.setNavigationOnClickListener { pop() }
        app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            toolbar.alpha = 1 - Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
        })
    }

    private fun initSearchBox() {
        tv_search_keyword.setOnClickListener {
            viewModel.showSearchBox.value = true
        }
        // 搜索框
        et_search_keyword.requestFocus()
        et_search_keyword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                if (viewModel.keyword.value != text)
                    viewModel.keyword.value = text
            }
        })
        et_search_keyword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                viewModel.startSearch(viewModel.keyword.value!!)
                true
            } else {
                false
            }
        }
        // 右上角关闭图标按钮
        iv_search_close.setOnClickListener {
            if (et_search_keyword.text.isEmpty()) {
                if (viewModel.canGoBack) {
                    hideSoftInput()
                    viewModel.showSearchBox.value = false
                    viewModel.keyword.value = SearchFragment.keyword.value
                } else {
                    pop()
                }
            } else {
                et_search_keyword.setText("")
            }
        }
        search_box_layout.setOnClickListener {

        }
        deleteAllTv.setOnClickListener { deleteHistoryAll() }
        suggestAdapter.onItemClickListener = { position: Int ->
            val text = viewModel.suggestList[position]
            viewModel.startSearch(text)
        }
        historyAdapter.onItemClickListener = { position: Int ->
            val text = viewModel.historyList[position]
            viewModel.startSearch(text)
        }
        historyAdapter.onItemLongClickListener = { position -> deleteHistoryItem(position) }
        suggestFlowLayout.setAdapter(suggestAdapter)
        historyFlowLayout.setAdapter(historyAdapter)
        viewModel.historyFlowAdapter = historyAdapter
    }

    private fun deleteHistoryItem(position: Int) {
        val text = viewModel.historyList[position]
        alert {
            title = "确定删除“$text”？"
            yesButton {
                viewModel.deleteSearchHistory(text)
            }
            noButton { }
            show()
        }
    }

    private fun deleteHistoryAll() {
        alert {
            title = "确定清空？"
            yesButton {
                viewModel.deleteAllSearchHistory()
            }
            noButton { }
            show()
        }
    }

    override fun onPause() {
        super.onPause()
        hideSoftInput()
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        mViewPager.adapter?.notifyDataSetChanged()
    }

    override fun onBackPressedSupport(): Boolean {
        if (viewModel.canGoBack) {
            viewModel.showSearchBox.value = false
            return true
        }
        return super.onBackPressedSupport()
    }

    private inner class MyAdapter(dataList: List<String>) : FlowAdapter<String>(dataList) {
        var onItemClickListener: ((position: Int) -> Unit)? = null
        var onItemLongClickListener: ((position: Int) -> Unit)? = null

        override fun getView(position: Int, parent: FlowLayout) = parent.context!!.UI {
            frameLayout {
                applyRecursively(ViewStyle.roundRect(dip(5)))
                backgroundColorResource = R.color.colorBackground
                textView {
                    text = getItem(position)
                    padding = dip(5)
                    selectableItemBackground()
                    setOnClickListener { onItemClickListener?.invoke(position) }
                    setOnLongClickListener {
                        onItemLongClickListener?.invoke(position)
                        return@setOnLongClickListener true
                    }
                }
            }

        }.view
    }

}