package com.a10miaomiao.bilimiao.ui.region

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.*
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class RegionDetailsFragment : Fragment() {

    companion object {
        fun newInstance(tid: Int): RegionDetailsFragment {
            val fragment = RegionDetailsFragment()
            val bundle = Bundle()
            bundle.putInt(ConstantUtil.TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    val tid by lazy { arguments!!.getInt(ConstantUtil.TID) }

    lateinit var viewModel: RegionDetailsViewModel
    var mAdapter: MiaoRecyclerViewAdapter<RegionTypeDetailsInfo.Result>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel{ RegionDetailsViewModel(context!!, tid) }
        return render().view
    }


    // arrayOf("click", "scores", "stow", "coin", "dm")
    private fun getRankOrder(@IdRes id: Int) = when (id) {
        R.id.order_click -> "click"
        R.id.order_scores -> "scores"
        R.id.order_stow -> "stow"
        R.id.order_click -> "coin"
        R.id.order_dm -> "dm"
        else -> "click"
    }

    override fun onResume() {
        super.onResume()
    }

    private fun render() = UI {
        val observeTime = viewModel.timeSettingStore.observe()
        var timeValue = viewModel.timeSettingStore.value
        observeTime {
            val newTimeValue = viewModel.timeSettingStore.value
            if (timeValue != newTimeValue) {
                timeValue = newTimeValue
                viewModel.refreshList()
            }
        }

        verticalLayout {
            linearLayout {
                backgroundColor = Color.WHITE
                padding = dip(5)
                gravity = Gravity.CENTER_VERTICAL
                textView {
                    observeTime { text = viewModel.timeSettingStore.value }
                    textSize = 14f
                    setOnClickListener {
                        startFragment(TimeSettingFragment())
                    }
                }.lparams(width = matchParent, weight = 1f)
                dropMenuView {
                    text = "播放量"
                    ico = R.drawable.ic_arrow_drop_down_24dp
                    popupMenu.inflate(R.menu.rank_order)
                    onMenuItemClick {
                        val rankOrder = getRankOrder(it.itemId)
                        if (rankOrder != viewModel.rankOrder) {
                            viewModel.rankOrder = rankOrder
                            viewModel.refreshList()
                        }
                    }
                }
            }.lparams(width = matchParent) { bottomMargin = dip(5) }
            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading){ isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    backgroundColor = Color.WHITE
                    mAdapter = createAdapter()
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
                }.lparams {
                    width = dip(140)
                    height = dip(85)
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
                        imageView {
                            imageResource = R.drawable.icon_up
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = item.author }
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
                            binding.bind { item -> text = NumberUtil.converString(item.play) }
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
                            binding.bind { item -> text = NumberUtil.converString(item.video_review) }
                        }
                    }

                    linearLayout {

                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.id))
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                (+viewModel.loadState){ state = it }
            }
        }
        onLoadMore {
            viewModel.pageNum++
            viewModel.loadData()
        }
    }
}