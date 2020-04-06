package com.a10miaomiao.bilimiao.ui.theme

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Color
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.DebugMiao

class ThemeViewModel(
        val context: Context
) : ViewModel() {

    val list = arrayListOf(
            ThemeItem("哔哩粉", context.resources.getColor(R.color.pink), "PinkTheme"),
            ThemeItem("姨妈红", context.resources.getColor(R.color.red), "RedTheme"),
            ThemeItem("咸蛋黄", context.resources.getColor(R.color.yellow), "YellowTheme"),
            ThemeItem("早苗绿", context.resources.getColor(R.color.green), "GreenTheme"),
            ThemeItem("胖次蓝", context.resources.getColor(R.color.blue), "BlueTheme"),
            ThemeItem("基佬紫", context.resources.getColor(R.color.purple), "PurpleTheme")
    )

    val selected = MainActivity.of(context).themeUtil.theme
    data class ThemeItem(
            var name: String,
            var color: Int,
            var theme: String
    )

}