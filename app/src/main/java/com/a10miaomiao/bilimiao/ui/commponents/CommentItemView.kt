package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.view.ViewManager
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.comment.Content
import com.a10miaomiao.bilimiao.entity.comment.Emote
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.ui.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.utils.UrlImageSpan
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
        content: ValueManager<Content>,
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

        include<ExpandableTextView>(R.layout.layout_expandable) {
            content {
                if (text.isNotBlank()) {
                    return@content
                }
                setContent(BiliUrlMatcher.customString(it.message))
                tag = it.emote
//                text = it.message
            }
            // 遍历表情
            setNextContentListener { ssb ->
                val content = ssb.toString()
                var emote = tag
                if (emote is Map<*, *>) {
                    emote = emote as Map<String, Emote>
                    emote?.values?.forEach { emote ->
                        val textLen = emote.text.length
                        var index = content.indexOf(emote.text)
                        while (index != -1) {
                            val span = UrlImageSpan(
                                context,
                                emote.url,
                                this
                            )
                            ssb.setSpan(span, index, index + textLen, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                            index = content.indexOf(emote.text, index + textLen)
                        }
                    }
                }
            }
            linkClickListener = ExpandableTextView.OnLinkClickListener { linkType, content, selfContent -> //根据类型去判断
                when (linkType) {
                    LinkType.LINK_TYPE -> {
                        BiliUrlMatcher.toLink(context, content)
                    }
                    LinkType.MENTION_TYPE -> {
                        context.toast("你点击了@用户 内容是：$content")
                    }
                    LinkType.SELF -> {
                        BiliUrlMatcher.toLink(context, selfContent)
                    }
                }
            }
            isNeedExpend = false
//            textIsSelectable { setTextIsSelectable(it) }
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

