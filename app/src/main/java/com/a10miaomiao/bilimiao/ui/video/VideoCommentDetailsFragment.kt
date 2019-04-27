package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.entity.comment.ReplyBean
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.CommentItemView
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.ui.commponents.commentItemView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView

class VideoCommentDetailsFragment : Fragment() {

    companion object {
        fun newInstance(reply: ReplyBean): VideoCommentDetailsFragment {
            val fragment = VideoCommentDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("reply", reply)
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var viewModel: VideoCommentDetailsViewModel

    val reply by lazy { arguments!!.getParcelable<ReplyBean>("reply") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { VideoCommentDetailsViewModel(reply) })
                .get(VideoCommentDetailsViewModel::class.java)
        return createUI().view
    }

    private fun createUI() = UI {
        verticalLayout {
            bottomSheetHeaderView("查看评论", View.OnClickListener {
                MainActivity.of(context)
                        .hideBottomSheet()
            })


            nestedScrollView {
                setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                    if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                        viewModel.loadData()
                    }
                })

                createBody()
            }
        }
    }

    private fun ViewManager.createBody() = verticalLayout {
        commentItemView {
            backgroundColorResource = R.color.colorWhite
            data = CommentItemView.CommentItemModel(
                    uname = reply.member.uname
                    , avatar = reply.member.avatar
                    , time = NumberUtil.converCTime(reply.ctime)
                    , floor = reply.floor
                    , content = reply.content.message
                    , like = reply.like
                    , count = reply.count
                    , textIsSelectable = true
            )
            onUpperClick = {
                val member = reply.member
                startFragment(UpperInfoFragment.newInstance(
                        Owner(
                                face = member.avatar,
                                name = member.uname,
                                mid = member.mid.toInt()
                        )
                ))
                MainActivity.of(context)
                        .hideBottomSheet()
            }
        }.lparams(matchParent, matchParent) {
            topMargin = dip(10)
        }

        if (reply.count > 0) {
            textView("全部回复").lparams {
                margin = dip(10)
            }

            recyclerView {
                backgroundColor = Color.WHITE
                createAdapter()
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(context)
            }

            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                viewModel.loadState.observe(this@VideoCommentDetailsFragment, Observer {
                    state = it!!
                })
            }

        } else {
            textView("空空如也") {
                gravity = Gravity.CENTER
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            }.lparams {
                height = matchParent
                width = matchParent
                verticalMargin = dip(48)
            }
        }

    }

    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
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
                            , textIsSelectable = true
                    )
                    onUpperClick = {
                        val member = item.member
                        startFragment(UpperInfoFragment.newInstance(
                                Owner(
                                        face = member.avatar,
                                        name = member.uname,
                                        mid = member.mid.toInt()
                                )
                        ))
                        MainActivity.of(context)
                                .hideBottomSheet()
                    }
                }

            }
        }
        onItemClick { item, position ->

        }
    }

}