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
            ThemeItem("哔哩粉", context.resources.getColor(R.color.pink), R.style.PinkTheme),
            ThemeItem("姨妈红", context.resources.getColor(R.color.red), R.style.RedTheme),
            ThemeItem("咸蛋黄", context.resources.getColor(R.color.yellow), R.style.YellowTheme),
            ThemeItem("早苗绿", context.resources.getColor(R.color.green), R.style.GreenTheme),
            ThemeItem("胖次蓝", context.resources.getColor(R.color.blue), R.style.BlueTheme),
            ThemeItem("基佬紫", context.resources.getColor(R.color.purple), R.style.PurpleTheme)
    )
    val selected = MainActivity.of(context).themeUtil.theme

    data class ThemeItem(
            var name: String,
            var color: Int,
            var theme: Int
    )

}