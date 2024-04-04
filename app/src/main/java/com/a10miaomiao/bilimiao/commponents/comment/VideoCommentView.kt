package com.a10miaomiao.bilimiao.commponents.comment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import android.text.Spannable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.core.view.marginBottom
import bilibili.main.community.reply.v1.ReplyOuterClass
import cn.a10miaomiao.miao.binding.android.view._contentDescription
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
import splitties.views.horizontalPadding
import splitties.views.padding
import splitties.views.textColorResource
import splitties.views.verticalPadding

@Parcelize
data class VideoCommentViewInfo(
    val oid: Long,
    val id: Long,
    val mid: Long,
    val uname: String,
    val avatar: String,
    val time: String,
    val location: String,
    val floor: Int,
    val content: VideoCommentViewContent,
    val like: Long,
    val count: Long,
    val cardLabels: List<String>,
    val isLike: Boolean = false,
    val isDelete: Boolean = false,
): Parcelable

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
    upMid: Long = -1,
    cardLabels: List<String> = listOf(),
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

        _contentDescription = with(StringBuilder()) {
            if (uname.isNotBlank()) {
                if (upMid == mid) {
                    append("UP主")
                }
                append(uname + "的评论")
            }
            if (time.isNotBlank()) {
                append(",")
                append("发表于：$time")
            }
            if (location.isNotBlank()) {
                append(",")
                append("IP属地：$location")
            }
            if (content.message.isNotBlank()) {
                append(",")
                append("评论内容：")
                append(content.message)
            }
            append(",")
            append("点赞数：${like}")
            append(",")
            append("评论数：${count}")
            if (isLike) {
                append(",你已点赞过该评论")
            }
            toString()
        }

        views {
            // 头像
            +rcImageView {
                isCircle = true
                _tag = mid.toString()
                onUpperClick?.let {
                    setOnClickListener(onUpperClick)
                }
                _contentDescription = "${uname}的头像"

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
                    +horizontalLayout {
                        gravity = Gravity.CENTER_VERTICAL
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

                            +textView {
                                _show = upMid == mid
                                setTextColor(config.foregroundColor)
                                textSize = 12f
                                text = "UP主"
                                textColorResource = R.color.white
                                background = GradientDrawable().apply {
                                    val radius = dip(5f)
                                    cornerRadii = floatArrayOf(
                                        radius, radius,
                                        radius, radius,
                                        radius, radius,
                                        radius, radius
                                    )
                                    setColor(config.themeColor)
                                    setStroke(0, 0)
                                }

                                horizontalPadding = dip(4)
                                verticalPadding = dip(2)
                            }..lParams {
                                leftMargin = dip(5)
                            }
                        }
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
                                _contentDescription = if (isLike) {
                                    "点赞图标：已点赞"
                                } else {
                                    "点赞图标：未点赞"
                                }
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
                                _contentDescription = "点赞数量：$like"
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
                                contentDescription = "回复图标"
                                imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                            }..lParams(iconSize, iconSize) {
                                rightMargin = dip(4)
                            }
                            +textView {
                                setTextColor(config.foregroundAlpha45Color)
                                textSize = 14f

                                _text = NumberUtil.converString(count)
                                _contentDescription = "回复数量：$like"
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

inline fun MiaoUI.videoCommentView(
    viewInfo: VideoCommentViewInfo,
    index: Int = -1,
    upMid: Long = -1,
    textIsSelectable: Boolean = false,
    onUpperClick: View.OnClickListener? = null,
    onLinkClick: ExpandableTextView.OnLinkClickListener? = null,
    onLikeClick: View.OnClickListener? = null,
    onImageItemClick: OnImageItemClickListener? = null,
): View {
    return videoCommentView(
        index = index,
        mid = viewInfo.mid,
        uname = viewInfo.uname,
        avatar = viewInfo.avatar,
        time = viewInfo.time,
        location = viewInfo.location,
        floor = viewInfo.floor,
        content = viewInfo.content,
        like = viewInfo.like,
        count = viewInfo.count,
        isLike = viewInfo.isLike,
        upMid = upMid,
        textIsSelectable = textIsSelectable,
        onUpperClick = onUpperClick,
        onLinkClick = onLinkClick,
        onLikeClick = onLikeClick,
        onImageItemClick = onImageItemClick,
    )
}