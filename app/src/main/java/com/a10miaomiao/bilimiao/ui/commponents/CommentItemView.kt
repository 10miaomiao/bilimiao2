package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewManager
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.utils.loadPic
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.ValueManager
import com.a10miaomiao.miaoandriod.v
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.jetbrains.anko.*


fun ViewManager.commentItemView(
        mid: ValueManager<Long>,
        uname: ValueManager<String>,
        avatar: ValueManager<String>,
        time: ValueManager<String>,
        floor: ValueManager<Int>,
        content: ValueManager<String>,
        like: ValueManager<Int>,
        count: ValueManager<Int>,
        textIsSelectable: ValueManager<Boolean> = false.v(),
        onUpperClick: ((mid: Long) -> Unit)? = null
) = linearLayout {
    lparams(matchParent, wrapContent)
    padding = dip(10)
    selectableItemBackground()

    rcImageView {
        isCircle = true
        avatar {
            Glide.with(context)
                    .loadPic(it)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ico_user_default)
                    .into(this)
//                    network(data.avatar)
        }
        mid { id ->
            setOnClickListener {
                onUpperClick?.invoke(id)
            }
        }

    }.lparams {
        width = dip(32)
        height = dip(32)
        rightMargin = dip(10)
        topMargin = dip(5)
    }

    verticalLayout {
        textView {
            uname { text = it }
            textColor = config.foregroundColor
            textSize = 14f
            mid { id ->
                setOnClickListener {
                    onUpperClick?.invoke(id)
                }
            }
        }

        linearLayout {
            textView {
                time { text = it }
                textColor = Color.parseColor("#99a2aa")
                textSize = 12f
            }.lparams {
                rightMargin = dip(10)
            }
            textView {
                floor { text = "#${it}" }
                textColor = Color.parseColor("#99a2aa")
                textSize = 12f
            }
        }

        textView {
            content { text = it }
            textIsSelectable { setTextIsSelectable(it) }
            textColor = config.foregroundColor
            textSize = 14f
        }.lparams {
            topMargin = dip(3)
        }

        linearLayout {
            textView {
                like { text = "${it}赞" }
                textColor = Color.parseColor("#99a2aa")
                textSize = 12f
            }
            textView {
                count { text = "${it}评论" }
                textColor = Color.parseColor("#99a2aa")
                textSize = 12f
            }.lparams {
                leftMargin = dip(36)
            }

        }.lparams {
            topMargin = dip(3)
        }


    }.lparams(matchParent, wrapContent)
}

