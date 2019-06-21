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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import com.a10miaomiao.bilimiao.config.config


class VideoCommentFragment : Fragment() {

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
        viewModel = ViewModelProviders.of(this, newViewModelFactory { VideoCommentViewModel(id) })
                .get(VideoCommentViewModel::class.java)
        return createUI().view
    }

    private fun createUI() = UI {
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
                        backgroundColor = Color.WHITE
                        createAdapter(viewModel.hotList)
                        isNestedScrollingEnabled = false
                        layoutManager = LinearLayoutManager(context)
                    }


                    textView("全部评论").lparams {
                        margin = dip(10)
                    }
                    recyclerView {
                        backgroundColor = Color.WHITE
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
        }

    }

    private fun RecyclerView.createAdapter(list: MiaoList<ReplyBean>) = miao(list) {
        itemView { b ->
            commentItemView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                b.bind { item ->
                    data = CommentItemView.CommentItemModel(
                            uname = item.member.uname
                            , avatar = item.member.avatar
                            , time = NumberUtil.converCTime(item.ctime)
                            , floor = item.floor
                            , content = item.content.message
                            , like = item.like
                            , count = item.count
                    )
                    onUpperClick = {
                        val member = item.member
                        startFragment(UpperInfoFragment.newInstance(
                                Owner(
                                        face = member.avatar,
                                        name = member.uname,
                                        mid = member.mid.toLong()
                                )
                        ))
                    }
                }

            }
        }

        onItemClick { item, position ->
            val fragment = VideoCommentDetailsFragment.newInstance(item)
            MainActivity.of(context)
                    .showBottomSheet(fragment)
        }
    }

}