package com.a10miaomiao.bilimiao.ui.upper

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
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
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.entity.UpperChannel
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

class UpperChannelVideoListFragment : SwipeBackFragment() {
    companion object {
        fun newInstance(channel: UpperChannel): UpperChannelVideoListFragment {
            val fragment = UpperChannelVideoListFragment()
            val bundle = Bundle()
            bundle.putParcelable("channel", channel)
            fragment.arguments = bundle
            return fragment
        }
    }

    val channel by lazy { arguments!!.getParcelable<UpperChannel>("channel") }

    lateinit var viewModel: UpperChannelVideoListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { UpperChannelVideoListViewModel(channel) })
                .get(UpperChannelVideoListViewModel::class.java)
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            lparams(matchParent, matchParent)
            headerView {
                title(viewModel.channel.name)
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
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
                        textColorResource = R.color.colorForeground
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
//                        imageView {
//                            imageResource = R.drawable.icon_up
//                        }.lparams {
//                            width = dip(16)
//                            rightMargin = dip(3)
//                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = NumberUtil.converCTime(item.pubdate) }
                        }
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView {
                            imageResource = R.drawable.ic_play_circle_outline_black_24dp
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = NumberUtil.converString(item.stat.view) }
                        }
                        space().lparams(width = dip(10))
                        imageView {
                            imageResource = R.drawable.ic_subtitles_black_24dp
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = NumberUtil.converString(item.stat.danmaku) }
                        }
                    }

                    linearLayout {

                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.aid))
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                viewModel.loadState.observe(this@UpperChannelVideoListFragment, Observer {
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