package com.a10miaomiao.miaoandriod.binding

import com.a10miaomiao.miaoandriod.MiaoObservableProperty

interface MiaoBinding {
    fun bindData(fn: (() -> Unit), key: String)
    fun updateView(key: String)
    fun <T> miao(initialValue: T) = MiaoObservableProperty(this, initialValue)
}