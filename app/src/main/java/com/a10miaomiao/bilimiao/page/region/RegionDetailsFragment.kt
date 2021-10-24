package com.a10miaomiao.bilimiao.page.region

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.MainViewModel
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.imageResource
import splitties.views.padding

class RegionDetailsFragment : Fragment(), DIAware {

    companion object {
        const val TID = "tid"
        fun newInstance(tid: Int): RegionDetailsFragment {
            val fragment = RegionDetailsFragment()
            val bundle = Bundle()
            bundle.putInt(TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@RegionDetailsFragment }
    }

    private val viewModel by diViewModel<RegionDetailsViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = bundleOf(
            MainNavGraph.args.avid to item.id
        )
        Navigation.findNavController(view)
            .navigate(MainNavGraph.action.region_to_videoInfo, args)
    }

    private val handleScroll = object : RecyclerView.OnScrollListener () {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    val itemUi = miaoBindingItemUi<RegionTypeDetailsInfo> { item, index ->
        horizontalLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            setBackgroundResource(config.selectableItemBackground)
            padding = dip(5)

            views {
                // 封面
                +rcImageView {
                    radius = dip(5)
                    _network(item.pic)
                }..lParams {
                    width = dip(140)
                    height = dip(85)
                    rightMargin = dip(5)
                }

                +verticalLayout {

                    views {
                        // 标题
                        +textView {
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 2
                            setTextColor(config.foregroundColor)
                            _text = item.title
                        }..lParams(matchParent, matchParent) {
                            weight = 1f
                        }

                        // UP主
                        +horizontalLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            views {
                                +imageView {
                                    imageResource = R.drawable.icon_up
                                    apply(ViewStyle.roundRect(dip(5)))
                                }..lParams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }

                                +textView {
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                    _text = item.author
                                }
                            }
                        }

                        // 播放量，弹幕数量
                        +horizontalLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            views {
                                +imageView {
                                    imageResource = R.drawable.ic_play_circle_outline_black_24dp
                                }..lParams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }
                                +textView {
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                    _text = NumberUtil.converString(item.play)
                                }
                                +space()..lParams(width = dip(10))
                                +imageView {
                                    imageResource = R.drawable.ic_subtitles_black_24dp
                                }..lParams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }
                                +textView {
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                    _text = NumberUtil.converString(item.video_review)
                                }
                            }
                        }

                    }

                }..lParams(width = matchParent, height = matchParent)

            }


        }
    }

    val ui = miaoBindingUi {
        verticalLayout {
            views {
                +recyclerView {
                    backgroundColor = config.blockBackgroundColor
                    layoutManager = GridAutofitLayoutManager(context, dip(300))

                    val footerView = listStateView(
                        when {
                            viewModel.triggered -> ListState.NORMAL
                            viewModel.list.loading -> ListState.LOADING
                            viewModel.list.fail -> ListState.FAIL
                            viewModel.list.finished -> ListState.NOMORE
                            else -> ListState.NORMAL
                        }
                    )
                    footerView..lParams(matchParent, wrapContent)

                    _miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMode()
                        }
                        addFooterView(footerView)
                    }
                }.wrapInSwipeRefreshLayout {
                    setColorSchemeResources(config.themeColorResource)
                    setOnRefreshListener(handleRefresh)
                    _isRefreshing = viewModel.triggered
                }..lParams(matchParent, matchParent)
            }
        }
    }
}

