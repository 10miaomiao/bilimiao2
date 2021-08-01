package com.a10miaomiao.bilimiao.ui.user

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
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
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

class FavDetailsFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(id: Long, title: String): FavDetailsFragment {
            val fragment = FavDetailsFragment()
            val bundle = Bundle()
            bundle.putLong("id", id)
            bundle.putString("title", title)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val id by lazy { arguments!!.getLong("id") }
    private val title by lazy { arguments!!.getString("title") }
    private lateinit var viewModel: FavDetailsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { FavDetailsViewModel(context!!, id) }
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            lparams(matchParent, matchParent)
            headerView {
                title(title)
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
                    binding.bind { item -> network(item.cover) }
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
                            binding.bind { item -> text = item.upper.name }
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
                            textColor = config.foregroundAlpha45Color
                            binding.bind { item -> text = NumberUtil.converString(item.cnt_info.play) }
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
                            textColor = config.foregroundAlpha45Color
                            binding.bind { item -> text = NumberUtil.converString(item.cnt_info.danmaku) }
                        }
                    }

                    linearLayout {

                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.id.toString()))
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
            if (-viewModel.loadState == LoadMoreView.State.NOMORE) {
                return@onLoadMore
            }
            viewModel.loadData()
        }
    }


}