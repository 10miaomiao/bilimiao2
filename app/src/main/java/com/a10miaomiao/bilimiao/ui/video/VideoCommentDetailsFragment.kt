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
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.entity.comment.ReplyBean
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.ui.commponents.commentItemView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.v
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

    val upperClick = { mid: Long ->
        startFragment(UserFragment.newInstance(mid))
        MainActivity.of(activity!!).hideBottomSheet()
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
        commentItemView(
                mid = reply.member.mid.toLong().v(),
                uname = reply.member.uname.v(),
                avatar = reply.member.avatar.v(),
                time = NumberUtil.converCTime(reply.ctime).v(),
                floor = reply.floor.v(),
                content = reply.content.message.v(),
                like = reply.like.v(),
                count = reply.count.v(),
                onUpperClick = {
                    val member = reply.member
                    startFragment(UpperInfoFragment.newInstance(
                            Owner(
                                    face = member.avatar,
                                    name = member.uname,
                                    mid = member.mid.toLong()
                            )
                    ))
                    MainActivity.of(context)
                            .hideBottomSheet()
                }
        ).apply {
            backgroundColor = config.blockBackgroundColor

            lparams(matchParent, matchParent) {
                topMargin = dip(10)
            }
        }

        if (reply.count > 0) {
            textView("全部回复").lparams {
                margin = dip(10)
            }

            recyclerView {
                backgroundColor = config.blockBackgroundColor
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

        }
    }

}