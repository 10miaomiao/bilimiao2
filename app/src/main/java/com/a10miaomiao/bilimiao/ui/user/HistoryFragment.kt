package com.a10miaomiao.bilimiao.ui.user

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class HistoryFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(): HistoryFragment {
            val fragment = HistoryFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    private val viewModel by lazy { getViewModel { HistoryViewModel(context!!) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            lparams(matchParent, matchParent)
            headerView {
                title("历史记录")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    backgroundColor = config.blockBackgroundColor
                    createAdapter()
                }
            }
        }
    }

    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(LinearLayoutManager(context))
        itemView { binding ->
            linearLayout {
                lparams(matchParent, wrapContent)
                selectableItemBackground()
                padding = dip(5)

                rcImageView {
                    radius = dip(5)
                    binding.bind { item -> network(item.pic) }
                }.lparams(width = dip(140), height = dip(85)) {
                    rightMargin = dip(5)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textColor = config.foregroundColor
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView {
                            imageResource = R.drawable.icon_up
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColor = config.foregroundAlpha45Color
                            binding.bind { item -> text = item.owner.name }
                        }
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL

                        textView {
                            textSize = 12f
                            textColor = config.foregroundAlpha45Color
                            binding.bind { item -> text = NumberUtil.converCTime(item.view_at ?: 0) }
                        }
                    }

                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.aid.toString()))
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                (+viewModel.loadState){
                    state = it
                }
            }
        }
        onLoadMore {
            viewModel.pageNum++
            viewModel.loadData()
        }
    }


}