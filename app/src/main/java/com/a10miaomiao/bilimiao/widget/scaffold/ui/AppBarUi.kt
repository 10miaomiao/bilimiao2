package com.a10miaomiao.bilimiao.widget.scaffold.ui

import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import splitties.views.dsl.core.Ui

interface AppBarUi : Ui {
    fun setProp(prop: AppBarView.PropInfo?)

    fun updateTheme(color: Int, bgColor: Int)
}