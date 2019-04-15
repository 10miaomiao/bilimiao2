package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class BangumiResultFragment : Fragment() {

    lateinit var viewModel: BangumiResultViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { BangumiResultViewModel(this) }).get(BangumiResultViewModel::class.java)
        return render().view
    }

    private fun render() = UI {
        swipeRefreshLayout {
            setColorSchemeResources(R.color.colorPrimary)
            viewModel.loading.observe(owner, Observer {
                isRefreshing = it!!
            })
            setOnRefreshListener { viewModel.refreshList() }
            recyclerView {
                backgroundColor = Color.WHITE
                createAdapter()
            }
        }
    }

    // 列表适配器
    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(LinearLayoutManager(context))
        itemView { binding ->
            linearLayout {
                lparams(matchParent, wrapContent)
                selectableItemBackground()
                padding = dip(5)

                rcImageView {
                    radius = dip(5)
                    binding.bind { item -> network(item.cover) }
                }.lparams(width = dip(100), height = dip(133)) {
                    rightMargin = dip(5)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textSize = 16f
                        textColorResource = R.color.colorForeground
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    textView {
                        textSize = 14f
                        textColorResource = R.color.black_alpha_45
                        binding.bind { item ->
                            text = if (item.finish == 1)//是否完结
                                "${item.newest_season}，${item.total_count}话全"
                            else
                                "${item.newest_season}，更新至第${item.total_count}话"
                        }
                    }.lparams {
                        bottomMargin = dip(5)
                    }

                    textView {
                        textSize = 14f
                        textColorResource = R.color.black_alpha_45
                        binding.bind { item -> text = item.cat_desc }
                    }

                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(BangumiFragment.newInstance(item.param))
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                viewModel.loadState.observe(this@BangumiResultFragment, Observer {
                    state = it!!
                })
            }
        }
        onLoadMore {
            viewModel.pageNum++
            viewModel.loadData()
        }
    }
}