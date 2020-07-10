package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.entity.comment.ReplyBean
import com.a10miaomiao.bilimiao.entity.comment.VideoComment
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.*
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment


class VideoCommentFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(id: String): VideoCommentFragment {
            val fragment = VideoCommentFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    val id by lazy { arguments!!.getString(ConstantUtil.ID) }
    lateinit var viewModel: VideoCommentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { VideoCommentViewModel(context!!, id) })
                .get(VideoCommentViewModel::class.java)
        return attachToSwipeBack(createUI().view)
    }

    val upperClick = { mid: Long ->
        startFragment(UserFragment.newInstance(mid))
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
                title("评论列表")
            }
            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                viewModel.loading.observe(owner, Observer {
                    isRefreshing = it!!
                })
                setOnRefreshListener { viewModel.refreshList() }

                nestedScrollView {
                    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                        if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                            viewModel.loadData()
                        }
                    })

                    verticalLayout {
                        textView("热门评论").lparams {
                            margin = dip(10)
                        }
                        recyclerView {
                            backgroundColor = config.blockBackgroundColor
                            createAdapter(viewModel.hotList)
                            isNestedScrollingEnabled = false
                            layoutManager = LinearLayoutManager(context)
                        }


                        textView("全部评论").lparams {
                            margin = dip(10)
                        }
                        recyclerView {
                            backgroundColor = config.blockBackgroundColor
                            createAdapter(viewModel.list)
                            isNestedScrollingEnabled = false
                            layoutManager = LinearLayoutManager(context)
                        }

                        loadMoreView {
                            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                            viewModel.loadState.observe(this@VideoCommentFragment, Observer {
                                state = it!!
                            })
                        }
                    }
                }
            }.lparams(width = matchParent, height = matchParent)
        }

    }

    private fun RecyclerView.createAdapter(list: MiaoList<ReplyBean>) = miao(list) {
        itemView { b ->
            commentItemView(
                    mid = b.itemValue { member.mid.toLong() },
                    uname = b.itemValue { member.uname },
                    avatar = b.itemValue { member.avatar },
                    time = b.itemValue { NumberUtil.converCTime(ctime) },
                    floor = b.itemValue { floor },
                    content = b.itemValue { content.message },
                    like = b.itemValue { like },
                    count = b.itemValue { count },
                    onUpperClick = upperClick
            )
        }

        onItemClick { item, position ->
            val fragment = VideoCommentDetailsFragment.newInstance(item)
            MainActivity.of(context)
                    .showBottomSheet(fragment)
        }
    }

}