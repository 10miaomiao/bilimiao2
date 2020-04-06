package com.a10miaomiao.bilimiao.ui.commponents

import android.text.TextUtils
import android.view.ViewManager
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.utils.network
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.ValueManager
import org.jetbrains.anko.*

fun ViewManager.bangumiItemView( cover: ValueManager<String>,
                                 title: ValueManager<String>,
                                 smallTitle1: ValueManager<String>,
                                 smallTitle2: ValueManager<String>
) {
    linearLayout {
        lparams(matchParent, wrapContent)
        selectableItemBackground()
        padding = dip(5)

        rcImageView {
            radius = dip(5)
            cover(::network)
        }.lparams(width = dip(100), height = dip(133)) {
            rightMargin = dip(5)
        }

        verticalLayout {
            textView {
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
                textSize = 16f
                textColor = config.foregroundColor
                title { text = it }
            }.lparams(matchParent, matchParent) {
                weight = 1f
            }

            textView {
                textSize = 14f
                textColor = config.foregroundAlpha45Color
                smallTitle1 { text = it }
            }.lparams {
                bottomMargin = dip(5)
            }

            textView {
                textSize = 14f
                textColor = config.foregroundAlpha45Color
                smallTitle2 { text = it }
            }
        }.lparams(width = matchParent, height = matchParent)
    }
}