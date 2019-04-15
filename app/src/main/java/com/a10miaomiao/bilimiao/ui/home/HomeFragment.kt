package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.region.RegionFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.search.SearchViewModel
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.ui.video.VideoCommentDetailsFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.anko.liveUI
import com.bumptech.glide.Glide
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView


class HomeFragment : Fragment() {

    lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory {
            HomeViewModel(context!!)
        }).get(HomeViewModel::class.java)
        return render().view
    }


    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        return true
    }

    private fun render() = liveUI {
        verticalLayout {
            backgroundColor = config.background
            headerView {
                viewModel.title.observeNotNull(::title)
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(this@HomeFragment::onMenuItemClick)
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
                            viewModel.time.observeNotNull { text = "当前时间线：$it" }
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
                        viewModel.adInfo.observeNotNull {
                            visibility = if (it.isShow) View.VISIBLE else View.GONE
                        }

                        backgroundColor = Color.WHITE
                        padding = config.dividerSize
                        textView {
                            viewModel.adInfo.observeNotNull { text = it.title }
                        }.lparams{
                            width = matchParent
                            weight = 1f
                        }

                        textView {
                            selectableItemBackgroundBorderless()
                            textColorResource = attr(android.R.attr.colorAccent)
                            viewModel.adInfo.observeNotNull { text = it.link.text }
                        }

                        setOnClickListener { viewModel.openAd() }
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }


//                    button("测试"){
//                        setOnClickListener {
//                            MainActivity.of(context)
//                                    .showBottomSheet(VideoCommentDetailsFragment.newInstance())
//                        }
//                    }


                }
            }
        }
    }
}
