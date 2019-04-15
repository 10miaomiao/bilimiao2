package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class UpperResultFragment : Fragment() {
    lateinit var viewModel: UpperResultViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { UpperResultViewModel(this) }).get(UpperResultViewModel::class.java)
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
                padding = dip(10)


                rcImageView {
                    isCircle = true
                    binding.bind { item -> network(item.cover) }
                }.lparams(width = dip(64), height = dip(64)) {
                    rightMargin = dip(10)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textSize = 16f
                        textColorResource = R.color.colorForeground
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        bottomMargin = dip(5)
                    }

                    textView {
                        textSize = 14f
                        textColorResource = R.color.black_alpha_45
                        binding.bind { item ->
                            text = "粉丝：${NumberUtil.converString(item.fans)}   视频数：${NumberUtil.converString(item.archives)}"
                        }
                    }.lparams {
                        bottomMargin = dip(5)
                    }

                    textView {
                        textSize = 14f
                        textColorResource = R.color.black_alpha_45
                        binding.bind { item -> text = item.sign }
                    }

                }.lparams(width = matchParent, height = wrapContent)
            }
        }
        onItemClick { item, position ->
            startFragment(UpperInfoFragment.newInstance(
                    Owner(
                            face = item.cover
                            , name = item.title
                            , mid = item.param.toInt()
                    )
            ))
//                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, item.id)
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                viewModel.loadState.observe(this@UpperResultFragment, Observer {
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