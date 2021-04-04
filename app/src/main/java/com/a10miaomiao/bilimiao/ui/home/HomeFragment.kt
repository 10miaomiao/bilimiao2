package com.a10miaomiao.bilimiao.ui.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.delegate.DownloadDelegate
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.region.RegionFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.bumptech.glide.Glide
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView


class HomeFragment : SupportFragment() {

    lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { HomeViewModel(context!!) }
        return MainActivity.of(context!!).dynamicTheme(this) { render().view }
    }


    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        viewModel.loadRegionData()
    }

    private fun render() = UI {
        val timeSettingStore = Store.from(context!!).timeSettingStore
        val observeTime = timeSettingStore.observe()

        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                viewModel.title.observe()(::title)
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    MainActivity.of(context!!).openDrawer()
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(onMenuItemClick)
            }
            nestedScrollView {
                verticalLayout {
                    lparams {
                        width = matchParent
                        bottomMargin = config.dividerSize
                    }
                    // 分区列表
                    recyclerView {
                        isNestedScrollingEnabled = false
                        backgroundColor = config.blockBackgroundColor
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }.miao(viewModel.region).itemView { miao ->
                        verticalLayout {
                            selectableItemBackground()
                            gravity = Gravity.CENTER
                            verticalPadding = dip(10)
                            imageView {
                                miao.bind { item ->
                                    if (item.icon != null) {
                                        Glide.with(context)
                                                .load(item.icon)
                                                .override(dip(24), dip(24))
                                                .into(this)
                                    }
                                    if (item.logo != null) {
                                        Glide.with(context)
                                                .loadPic(item.logo!!)
                                                .override(dip(24), dip(24))
                                                .into(this)
                                    }
                                }
                            }.lparams(dip(24), dip(24))
                            textView {
                                miao.bind { item -> text = item.name }
                                gravity = Gravity.CENTER
                            }
                        }
                    }.onItemClick { item, position ->
                        startFragment(RegionFragment.newInstance(item))
                    }.layoutManager(GridLayoutManager(activity, 5))

                    // 时间线时间显示
                    linearLayout {
                        selectableItemBackground()
                        backgroundColor = config.blockBackgroundColor
                        padding = config.dividerSize
                        textView {
                            observeTime {
                                text = "当前时间线：" + timeSettingStore.value
                            }
                        }
                        setOnClickListener {
                            startFragment(TimeSettingFragment())
                        }
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }

                    // 广告通知
                    linearLayout {
                        visibility = View.GONE
                        backgroundColor = config.blockBackgroundColor
                        padding = config.dividerSize

                        val observeAdInfo = viewModel.adInfo.observeNotNull()

                        observeAdInfo {
                            visibility = if (it?.isShow == true) View.VISIBLE else View.GONE
                        }

                        textView {
                            observeAdInfo { text = it!!.title }
                        }.lparams {
                            width = matchParent
                            weight = 1f
                        }

                        textView {
                            selectableItemBackgroundBorderless()
                            textColorResource = attr(android.R.attr.colorAccent)
                            observeAdInfo { text = it!!.link.text }
                        }

                        setOnClickListener { viewModel.openAd() }
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }

//                    button("测试") {
//                        setOnClickListener {
//                            val behavior = MainActivity.of(context!!).videoPlayerDelegate.haederBehavior
//                            if (behavior.isShow()) {
//                                behavior.hide()
//                            } else {
//                                behavior.show()
//                            }
//                        }
//                    }
//                    textView {
//                        val ss = SpannableString("bilibili哈哈哈")
//                        //用这个drawable对象代替字符串easy
//                        val span = UrlImageSpan(
//                                context,
//                                "https://i0.hdslb.com/bfs/archive/622017dd4b0140432962d3ce0c6db99d77d2e937.png",
//                                this
//                        )
//                        ss.setSpan(span, 0, "bilibili".length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
//                        append(ss)
//                    }
                }
            }
        }
    }
}
