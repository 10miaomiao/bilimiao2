package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.cover.CoverActivity
import com.a10miaomiao.bilimiao.ui.region.RegionFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.mergeMiaoObserver
import com.bumptech.glide.Glide
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView


class HomeFragment : Fragment() {

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

    private fun render() = UI {
        val timeSettingStore = MainActivity.of(context!!).timeSettingStore
        val observeTime = timeSettingStore.observe()

        verticalLayout {
            backgroundColor = config.background
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
                        backgroundColor = Color.WHITE
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
                                    Glide.with(context)
                                            .load(item.icon)
                                            .override(dip(24), dip(24))
                                            .into(this)
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
                        backgroundColor = Color.WHITE
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
                        backgroundColor = Color.WHITE
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
                }
            }
        }
    }
}
