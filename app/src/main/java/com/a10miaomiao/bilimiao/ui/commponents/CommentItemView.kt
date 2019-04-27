package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.NumberUtil
import com.a10miaomiao.bilimiao.utils.loadPic
import com.a10miaomiao.bilimiao.utils.network
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.MiaoView
import com.a10miaomiao.miaoandriod.adapter.MiaoViewHolder
import com.a10miaomiao.miaoandriod.anko.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import com.a10miaomiao.miaoandriod.binding.bind
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.jetbrains.anko.*


class CommentItemView @JvmOverloads constructor(context: Context
                                                , attrs: AttributeSet? = null
                                                , defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    data class CommentItemModel(
            var uname: String = "",
            var avatar: String = "",
            var time: String = "",
            var floor: Int = 0,
            var content: String = "",
            var like: Int = 0,
            var count: Int = 0,
            var textIsSelectable: Boolean = false
    )

    var onUpperClick: (() -> Unit)? = null

    val fns = arrayListOf<() -> Unit>()

    var data = CommentItemModel()
        set(value) {
            field = value
            fns.forEach { it.invoke() }
        }

    init {
        addView(createUI(context).view, matchParent, matchParent)
    }

    private inline fun b(noinline fn: (() -> Unit)) {
        fns.add(fn)
    }

    fun createUI(context: Context) = context.UI {
        linearLayout {
            lparams(matchParent, wrapContent)
            padding = dip(10)
            selectableItemBackground()

            rcImageView {
                isCircle = true
                b {
                    Glide.with(context)
                            .loadPic(data.avatar)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ico_user_default)
                            .into(this)
//                    network(data.avatar)
                }
                setOnClickListener {
                    onUpperClick?.invoke()
                }
            }.lparams {
                width = dip(32)
                height = dip(32)
                rightMargin = dip(10)
                topMargin = dip(5)
            }

            verticalLayout {
                textView {
                    b {
                        text = data.uname
//                        setTextIsSelectable(data.textIsSelectable)
                    }
                    textColor = Color.parseColor("#222222")
                    textSize = 14f
                    setOnClickListener {
                        onUpperClick?.invoke()
                    }
                }

                linearLayout {
                    textView {
                        b { text = data.time }
                        textColor = Color.parseColor("#99a2aa")
                        textSize = 12f
                    }.lparams {
                        rightMargin = dip(10)
                    }
                    textView {
                        b { text = "#${data.floor}" }
                        textColor = Color.parseColor("#99a2aa")
                        textSize = 12f
                    }
                }

                textView {
                    b {
                        text = data.content
                        setTextIsSelectable(data.textIsSelectable)
                    }
                    textColor = Color.parseColor("#222222")
                    textSize = 14f
                }.lparams {
                    topMargin = dip(3)
                }

                textView {
                    b { text = "${data.like}赞    ${data.count}评论" }
                    textColor = Color.parseColor("#99a2aa")
                    textSize = 12f
                }.lparams {
                    topMargin = dip(3)
                }

            }.lparams(matchParent, wrapContent)
        }
    }
}