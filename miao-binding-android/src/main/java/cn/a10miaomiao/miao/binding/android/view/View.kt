package cn.a10miaomiao.miao.binding.android.view

import android.view.View
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect

inline var View._visibility: Int
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
        visibility = it
    }

inline var View._show: Boolean
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
        visibility = if (it) View.VISIBLE else View.GONE
    }