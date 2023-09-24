package com.a10miaomiao.bilimiao.commponents.video

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.glide.GlideBlurTransformation
import com.a10miaomiao.bilimiao.comm.loadImageUrl
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.HtmlTagHandler
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*


private fun MiaoUI.miniVideoItemCover(
    pic: String? = null,
    playNum: String? = null,
    damukuNum: String? = null,
    duration: String? = null,
): View {
    return frameLayout {
        views {
            +imageView {
                scaleType = ImageView.ScaleType.CENTER_CROP

                _network(pic, "@672w_378h_1c_")
//                        miaoEffect(pic) {
//                            if (it != null && it.isNotEmpty()) {
//                                Glide.with(context)
//                                    .loadImageUrl(it, "@672w_378h_1c_")
//                                    .apply(RequestOptions().transform(GlideBlurTransformation(context, 25f)))
//                                    .into(this)
//                            }
//                        }
            }..lParams(matchParent, matchParent)

            +horizontalLayout {
                padding = dip(5)
                _show = playNum != null || damukuNum != null
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundResource(R.drawable.gradient_reverse)

                views {
                    +imageView {
                        imageResource = R.drawable.ic_play_circle_outline_white_24dp
                    }..lParams {
                        height = dip(15)
                        width = dip(20)
                        rightMargin = dip(3)
                    }
                    +textView {
                        textSize = 12f
                        setTextColor(0xFFFFFFFF.toInt())
                        _text = NumberUtil.converString(playNum ?: "0")
                    }
                    +space()..lParams(width = dip(10))
                    +imageView {
                        imageResource = R.drawable.ic_subtitles_white_24dp
                    }..lParams {
                        height = dip(15)
                        width = dip(20)
                        rightMargin = dip(3)
                    }
                    +textView {
                        textSize = 12f
                        setTextColor(0xFFFFFFFF.toInt())
                        _text = NumberUtil.converString(damukuNum ?: "0")
                    }
                    +space()..lParams { weight = 1f }
                    +textView {
                        textSize = 12f
                        setTextColor(0xFFFFFFFF.toInt())
                        _text = duration ?: ""
                    }
                }
            }..lParams(matchParent, wrapContent) {
                gravity = Gravity.BOTTOM
            }
        }
    }
}

fun MiaoUI.miniVideoItem(
    title: String? = null,
    pic: String? = null,
    upperName: String? = null,
    remark: String? = null,
    playNum: String? = null,
    damukuNum: String? = null,
    duration: String? = null,
): View {
    return frameLayout {
        padding = dip(5)

        views {
            +frameLayout {
                setBackgroundResource(config.blockBackgroundResource)
                apply(ViewStyle.roundRect(dip(5)))

                views {
                    +verticalLayout {
                        setBackgroundResource(config.selectableItemBackground)

                        views {
                            +miniVideoItemCover(
                                pic = pic,
                                playNum = playNum,
                                damukuNum = playNum,
                                duration = duration,
                            )..lParams(matchParent, dip(120))

                            // 标题
                            +textView {
                                ellipsize = TextUtils.TruncateAt.END
                                maxLines = 2
                                setTextColor(config.foregroundColor)
                                textSize = 16f
                                gravity = Gravity.CENTER_VERTICAL
                                horizontalPadding = dip(5)
//                            if (isHtml) {
//                                miaoEffect(title) {
//                                    DebugMiao.log(it)
//                                    text = HtmlTagHandler.fromHtml( "<html><body>$it</body></html>")
//                                }
//                            } else {
                                _text = title ?: ""
//                            }
                            }..lParams(matchParent, dip(50))

                            +horizontalLayout {
                                bottomPadding = dip(5)
                                horizontalPadding = dip(5)
                                gravity = Gravity.CENTER_VERTICAL
                                _show = upperName != null

                                views {
                                    +imageView {
                                        imageResource = R.drawable.icon_up
                                        apply(ViewStyle.roundRect(dip(5)))
                                    }..lParams {
                                        width = dip(16)
                                        rightMargin = dip(5)
                                    }

                                    +textView {
                                        textSize = 14f
                                        setTextColor(config.foregroundAlpha45Color)
                                        maxLines = 1
                                        _text = upperName ?: ""
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}