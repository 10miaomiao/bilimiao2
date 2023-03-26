package com.a10miaomiao.bilimiao.commponents.dynamic

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.rcImageView
import splitties.dimensions.dip
import splitties.views.bottomPadding
import splitties.views.dsl.core.*
import splitties.views.padding
import splitties.views.topPadding


fun MiaoUI.dynamicAuthorView(
    dynamicType: Int,
    mid: String,
    name: String,
    face: String,
    labelText: String,
    onAuthorClick: View.OnClickListener? = null,
): View {
    return horizontalLayout {
        _tag = Pair(dynamicType, mid)
        padding = config.pagePadding
        bottomPadding = config.smallPadding
        setBackgroundResource(config.selectableItemBackground)
        onAuthorClick?.let { setOnClickListener(it) }

        views {
            // 头像
            +rcImageView {
                isCircle = true
//              _tag = mid.toString()
//              onUpperClick?.let {
//                  setOnClickListener(onUpperClick)
//              }
                _network(face)
//                  .placeholder(R.drawable.ico_user_default)
            }..lParams {
                width = dip(40)
                height = dip(40)
                rightMargin = config.smallPadding
            }

            +verticalLayout {
                views {
                    +textView {
                        _text = name
                        setTextColor(config.foregroundColor)
                        textSize = 16f
                    }..lParams {
                        width = wrapContent
                        height = wrapContent
                    }

                    +textView {
                        _text = labelText
                        setTextColor(config.foregroundAlpha45Color)
                    }..lParams {
                        width = wrapContent
                        height = wrapContent
                        topMargin = dip(2)
                    }
                }
            }
        }
    }
}


fun MiaoUI.dynamicStatView(
    like: Long,
    reply: Long,
): View {
    return horizontalLayout {
        padding = config.pagePadding
        topPadding = config.smallPadding
        gravity = Gravity.CENTER
            views {
                val iconSize = dip(14)
                +imageView {
                    setImageResource(R.drawable.ic_comment_unlike)
                    imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
//                    _tag = index
//                    miaoEffect(isLike) {
//                        imageTintList = ColorStateList.valueOf(
//                            if (it) {
//                                config.themeColor
//                            } else {
//                                config.foregroundAlpha45Color
//                            }
//                        )
//                    }
//                    onLikeClick?.let { setOnClickListener(it) }
                }..lParams(iconSize, iconSize) {
                    rightMargin = dip(4)
                }
                +textView {
                    textSize = 14f
                    _text = NumberUtil.converString(like)
                    setTextColor(config.foregroundAlpha45Color)
//                    _tag = index
//                    miaoEffect(isLike) {
//                        setTextColor(
//                            if (it) {
//                                config.themeColor
//                            } else {
//                                config.foregroundAlpha45Color
//                            }
//                        )
//                    }
//                    onLikeClick?.let { setOnClickListener(it) }
                }..lParams(dip(150), wrapContent)
                +imageView {
                    setImageResource(R.drawable.ic_comment_reply)
                    imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                }..lParams(iconSize, iconSize) {
                    rightMargin = dip(4)
                }
                +textView {
                    setTextColor(config.foregroundAlpha45Color)
                    textSize = 14f

                    _text = NumberUtil.converString(reply)
                }
            }
    }
}


fun MiaoUI.dynamicCardView(
    dynamicType: Int,
    mid: String,
    name: String,
    face: String,
    labelText: String,
    like: Long,
    reply: Long,
    contentView: View,
    onAuthorClick: View.OnClickListener? = null,
): View {
    return verticalLayout {
        views {

            +dynamicAuthorView(
                dynamicType = dynamicType,
                mid = mid,
                name = name,
                face = face,
                labelText = labelText,
                onAuthorClick = onAuthorClick,
            )..lParams(
                width = matchParent,
                height = wrapContent,
            )

            +contentView..lParams(
                width = matchParent,
                height = wrapContent,
            )

            +dynamicStatView(
                like = like,
                reply = reply,
            )..lParams(
                width = matchParent,
                height = wrapContent,
            )
        }
    }
}