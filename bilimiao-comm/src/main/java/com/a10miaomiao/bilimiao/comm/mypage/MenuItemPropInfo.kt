package com.a10miaomiao.bilimiao.comm.mypage

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes

data class MenuItemPropInfo (
    var key: Int? = null,
    var title: String? = null,
    var subTitle: String? = null,
    var iconDrawable: Drawable? = null,
    @DrawableRes
    var iconResource: Int? = null,
    var visibility: Int = View.VISIBLE
)