package com.a10miaomiao.bilimiao.commponents.upper

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

fun MiaoUI.upperItem(
    name: String? = null,
    face: String? = null,
    remarks: String? = null,
    sign: String? = null,
): View {
    return horizontalLayout {
        setBackgroundResource(config.selectableItemBackground)
        padding = config.pagePadding

        views {
            +rcImageView {
                isCircle = true
                _network(face)
            }..lParams (width = dip(64), height = dip(64)) {
                rightMargin = config.dividerSize
            }

            +verticalLayout {

                views {
                    +textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textSize = 16f
                        setTextColor(config.foregroundColor)
                        _text = name ?: ""
                    }..lParams(matchParent, matchParent) {
                        bottomMargin = dip(5)
                    }

                    +textView {
                        textSize = 14f
                        setTextColor(config.foregroundAlpha45Color)

                        _show = remarks != null
                        _text = remarks ?: ""
                    }..lParams {
                        bottomMargin = dip(5)
                    }

                    +textView {
                        textSize = 14f
                        setTextColor(config.foregroundAlpha45Color)
                        _show = sign != null
                        _text = sign ?: ""
                    }
                }
            }..lParams(width = matchParent, height = wrapContent)
        }





    }
}