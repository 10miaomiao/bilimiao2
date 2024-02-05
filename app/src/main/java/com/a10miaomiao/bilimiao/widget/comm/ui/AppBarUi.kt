package com.a10miaomiao.bilimiao.widget.comm.ui

import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import splitties.views.dsl.core.Ui

interface AppBarUi : Ui {
    fun setProp(prop: AppBarView.PropInfo?)

    fun updateTheme()
}