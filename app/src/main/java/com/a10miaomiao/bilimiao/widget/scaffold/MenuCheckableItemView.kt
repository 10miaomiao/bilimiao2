package com.a10miaomiao.bilimiao.widget.scaffold

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config

class MenuCheckableItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MenuItemView(context, attrs) {

    var themeColor = 0
        set(value) = run {
            field = value
            updateChecked()
        }

    var checked: Boolean = false
        set(value) {
            field = value
            updateChecked()
        }

    private fun updateChecked() {
        if (checked) {
            setBackgroundResource(R.drawable.shape_menu_item_checked)
            ui.title.setTextColor(themeColor)
            ui.icon.imageTintList = ColorStateList.valueOf(themeColor)
        } else {
            setBackgroundResource(config.selectableItemBackgroundBorderless)
            ui.title.setTextColor(config.foregroundAlpha45Color)
            ui.icon.imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
        }
    }

}