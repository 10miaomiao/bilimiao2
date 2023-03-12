package com.a10miaomiao.bilimiao.commponents.comment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Parcelable
import android.text.Spannable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.view.marginBottom
import bilibili.main.community.reply.v1.ReplyOuterClass
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
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget._setContent
import com.a10miaomiao.bilimiao.widget.expandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.UrlImageSpan
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.a10miaomiao.bilimiao.widget.gridimage.GlideNineGridImageLoader
import com.a10miaomiao.bilimiao.widget.gridimage.OnImageItemClickListener
import com.a10miaomiao.bilimiao.widget.nineGridImageView
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.parcel.Parcelize
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.padding

@Parcelize
data class VideoCommentViewContent(
//        val device: String,
    val message: String,
    val emote: List<Emote>?,
    val picturesList: List<String>,
) : Parcelable {

    @Parcelize
    data class Emote(
        val id: Long,
        val text: String,
        val url: String
    ) : Parcelable

    @Parcelize
    data class Picture(
        val src: String,
        val width: Int,
        val height: Int,
        val size: Int,
    ) : Parcelable

}

private fun MiaoUI.commentContentView(
    content: VideoCommentViewContent,
    textIsSelectable: Boolean,
    onLinkClick: ExpandableTextView.OnLinkClickListener? = null
): View {
    return expandableTextView {
        setLineSpacing(dip(4).toFloat(), 1.0f)
        setTextColor(config.foregroundColor)
        textSize = 16f
        isNeedContract = true
        isNeedExpend = false
//        setTextIsSelectable(textIsSelectable)
        setNeedMention(false)
        isNeedSelf = true
        setNeedConvertUrl(false)

        miaoEffect(content.emote) {
            tag = content.emote
        }
        miaoEffect(null) {
            setNextContentListener { ssb ->
                val content = ssb.toString()
                (tag as? List<VideoCommentViewContent.Emote>)?.forEach { emote ->
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
        _setContent(BiliUrlMatcher.customString(content.message))

        linkClickListener = onLinkClick
    }
}

fun MiaoUI.videoCommentView(
    index: Int = -1,
    mid: Long,
    uname: String,
    avatar: String,
    time: String,
    location: String,
    floor: Int,
    content: VideoCommentViewContent,
    like: Long,
    count: Long,
    textIsSelectable: Boolean = false,
    isLike: Boolean = false,
    onUpperClick: View.OnClickListener? = null,
    onLinkClick: ExpandableTextView.OnLinkClickListener? = null,
    onLikeClick: View.OnClickListener? = null,
    onImageItemClick: OnImageItemClickListener? = null,
): View {
    return horizontalLayout {
        padding = dip(10)
        setBackgroundResource(config.selectableItemBackground)

        views {
            // 头像
            +rcImageView {
                isCircle = true
                _tag = mid.toString()
                onUpperClick?.let {
                    setOnClickListener(onUpperClick)
                }

                _network(avatar)
//              .placeholder(R.drawable.ico_user_default)

            }..lParams {
                width = dip(40)
                height = dip(40)
                rightMargin = dip(10)
                topMargin = dip(4)
            }

            +verticalLayout {
                views {
                    +textView {
                        setTextColor(config.foregroundColor)
                        textSize = 16f
                        _tag = mid
                        onUpperClick?.let {
                            setOnClickListener(onUpperClick)
                        }

                        _text = uname
                    }

                    +horizontalLayout {
                        views {
                            +textView {
                                setTextColor(config.foregroundAlpha45Color)
                                textSize = 14f

                                _text = time
                            }..lParams {
                                rightMargin = dip(10)
                            }
                            +textView {
                                setTextColor(config.foregroundAlpha45Color)
                                textSize = 14f

                                _show = floor != 0
                                _text = "#${floor}"
                            }..lParams {
                                rightMargin = dip(10)
                            }
                            +textView {
                                setTextColor(config.foregroundAlpha45Color)
                                textSize = 14f

                                _show = location.isNotBlank()
                                _text = location
                            }
                        }
                    }..lParams {
                        topMargin = dip(4)
                    }

                    +commentContentView(
                        content = content,
                        textIsSelectable = textIsSelectable,
                        onLinkClick = onLinkClick,
                    )..lParams {
                        width = matchParent
                        height = wrapContent
                        verticalMargin = dip(8)
                    }

                    +nineGridImageView {
                        spacing = dip(10)
                        onlyOneSize = dip(200)
                        miaoEffect(null){
                            imageLoader = GlideNineGridImageLoader()
                        }
                        miaoEffect(content.picturesList) {
//                            externalPosition = holder.bindingAdapterPosition
                            setUrlList(content.picturesList)
                        }
                        onImageItemClick?.let { onImageItemClickListener = it }
                    }..lParams {
                        width = matchParent
                        height = wrapContent
                        bottomMargin = dip(8)
                    }

                    +horizontalLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        views {
                            val iconSize = dip(14)
                            +imageView {
                                setImageResource(R.drawable.ic_comment_unlike)
                                _tag = index
                                miaoEffect(isLike) {
                                    imageTintList = ColorStateList.valueOf(
                                        if (it) {
                                            config.themeColor
                                        } else {
                                            config.foregroundAlpha45Color
                                        }
                                    )
                                }
                                onLikeClick?.let { setOnClickListener(it) }
                            }..lParams(iconSize, iconSize) {
                                rightMargin = dip(4)
                            }
                            +textView {
                                textSize = 14f
                                _text = NumberUtil.converString(like)
                                _tag = index
                                miaoEffect(isLike) {
                                    setTextColor(
                                        if (it) {
                                            config.themeColor
                                        } else {
                                            config.foregroundAlpha45Color
                                        }
                                    )
                                }
                                onLikeClick?.let { setOnClickListener(it) }
                            }..lParams(dip(80), wrapContent)
                            +imageView {
                                setImageResource(R.drawable.ic_comment_reply)
                                imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                            }..lParams(iconSize, iconSize) {
                                rightMargin = dip(4)
                            }
                            +textView {
                                setTextColor(config.foregroundAlpha45Color)
                                textSize = 14f

                                _text = NumberUtil.converString(count)
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