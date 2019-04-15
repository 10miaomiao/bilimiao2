package com.a10miaomiao.bilimiao.ui.commponents

import android.view.Gravity
import android.view.View
import android.view.ViewManager
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.selectableItemBackgroundBorderless
import org.jetbrains.anko.*

fun ViewManager.bottomSheetHeaderView(
        title: String,
        onClickListener: View.OnClickListener
) = linearLayout {
    backgroundColorResource = R.color.colorWhite
    gravity = Gravity.CENTER_VERTICAL

    imageView {
        imageResource = R.drawable.ic_close_grey_24dp
        selectableItemBackgroundBorderless()
        horizontalPadding = dip(10)

        setOnClickListener(onClickListener)
    }

    textView {
        text = title
        textSize = 20f
        verticalPadding = dip(10)
    }

}