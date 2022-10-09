package com.a10miaomiao.bilimiao.commponents.comment

import android.graphics.Color
import android.text.Spannable
import android.view.View
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget._setContent
import com.a10miaomiao.bilimiao.widget.expandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.UrlImageSpan
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.padding


private fun MiaoUI.commentContentView(
    content: VideoCommentReplyInfo.Content,
    onLinkClick: ExpandableTextView.OnLinkClickListener? = null
): View {
    return expandableTextView {
        setLineSpacing(dip(4).toFloat(), 1.0f)
        setTextColor(config.foregroundColor)
        textSize = 14f
        isNeedContract = true
        isNeedExpend = false
        setNeedMention(false)
        isNeedSelf = true
        setNeedConvertUrl(false)
        miaoEffect(content.emote ?: 0) {
            tag = content.emote ?: 0
            setNextContentListener { ssb ->
                val content = ssb.toString()
                var emote = tag
                if (emote is Map<*, *>) {
                    emote = emote as Map<String, VideoCommentReplyInfo.Emote>
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
        }
        _setContent(BiliUrlMatcher.customString(content.message))
        linkClickListener = onLinkClick
    }
}

fun MiaoUI.videoCommentView(
    mid: String,
    uname: String,
    avatar: String,
    time: String,
    location: String,
    floor: Int,
    content: VideoCommentReplyInfo.Content,
    like: Int,
    count: Int,
    textIsSelectable: Boolean = false,
    onUpperClick: View.OnClickListener? = null,
    onLinkClick: ExpandableTextView.OnLinkClickListener? = null
): View {
    return horizontalLayout {
        padding = dip(10)
        setBackgroundResource(config.selectableItemBackground)

        views {
            // 头像
            +rcImageView {
                isCircle = true
                _tag = mid
                onUpperClick?.let {
                    setOnClickListener(onUpperClick)
                }

                _network(avatar)
//              .placeholder(R.drawable.ico_user_default)

            }..lParams {
                width = dip(32)
                height = dip(32)
                rightMargin = dip(10)
                topMargin = dip(5)
            }

            +verticalLayout {
                views {
                    +textView {
                        setTextColor(config.foregroundColor)
                        textSize = 14f
                        tag = mid
                        onUpperClick?.let {
                            setOnClickListener(onUpperClick)
                        }

                        _text = uname
                    }

                    +horizontalLayout {
                        views {
                            +textView {
                                setTextColor(Color.parseColor("#99a2aa"))
                                textSize = 12f

                                _text = time
                            }..lParams {
                                rightMargin = dip(10)
                            }
                            +textView {
                                setTextColor(Color.parseColor("#99a2aa"))
                                textSize = 12f

                                _show = floor != 0
                                _text = "#${floor}"
                            }..lParams {
                                rightMargin = dip(10)
                            }
                            +textView {
                                setTextColor(Color.parseColor("#99a2aa"))
                                textSize = 12f

                                _show = location.isNotBlank()
                                _text = location
                            }
                        }
                    }

                    +commentContentView(
                        content = content,
                        onLinkClick = onLinkClick,
                    )..lParams {
                        width = matchParent
                        height = wrapContent
                        topMargin = dip(3)
                    }

                    +horizontalLayout {
                        views {
                            +textView {
                                setTextColor(Color.parseColor("#99a2aa"))
                                textSize = 12f
                                _text = "${like}赞"
                            }
                            +textView {
                                setTextColor(Color.parseColor("#99a2aa"))
                                textSize = 12f

                                _text = "${count}评论"
                            }..lParams {
                                leftMargin = dip(36)
                            }
                        }
                    }..lParams {
                        topMargin = dip(3)
                    }
                }
            }..lParams(matchParent, wrapContent)
        }

    }
}