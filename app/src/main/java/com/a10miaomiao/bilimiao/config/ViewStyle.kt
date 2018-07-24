package com.a10miaomiao.bilimiao.config

import android.graphics.Color
import android.view.View
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.padding


object ViewStyle {
    val block = { v: View ->
        with(v) {
            backgroundColor = Color.WHITE
            padding = config.dividerSize
        }
    }
}