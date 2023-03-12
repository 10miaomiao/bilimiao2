package com.a10miaomiao.bilimiao.commponents.bangumi

import android.text.TextUtils
import android.view.View
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.rcImageView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.padding

fun MiaoUI.bangumiItem(
    title: String? = null,
    cover: String? = null,
    statusText: String? = null,
    desc: String? = null,
): View {
    return horizontalLayout {
        setBackgroundResource(config.selectableItemBackground)
        padding = config.pagePadding

        views {
            +rcImageView {
                radius = dip(5)
                _network(cover, "@560w_746h")
            }..lParams(width = dip(100), height = dip(133)) {
                rightMargin = dip(5)
            }

            +verticalLayout {

                views {
                    +textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textSize = 16f
                        setTextColor(config.foregroundColor)

                        _text = title ?: ""
                    }..lParams(matchParent, matchParent) {
                        weight = 1f
                    }

                    +textView {
                        textSize = 14f
                        setTextColor(config.foregroundAlpha45Color)

                        _show = statusText != null
                        _text = statusText ?: ""
                    }..lParams {
                        bottomMargin = dip(5)
                    }

                    +textView {
                        textSize = 14f
                        setTextColor(config.foregroundAlpha45Color)

                        _show = desc != null
                        _text = desc ?: ""
                    }
                }
            }..lParams(width = matchParent, height = matchParent)
        }
    }
}