package cn.a10miaomiao.miao.binding.android.widget

import android.view.View
import android.widget.TextView
import cn.a10miaomiao.miao.binding.MiaoTarget
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect

inline var TextView._text: String
        get() { throw BindingOnlySetException() }
        set(value) = miaoEffect(value) {
            text = it
        }

inline var TextView._textColor: Int
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
        setTextColor(value)
    }

inline var TextView._textColorResource: Int
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
       setTextColor(context.resources.getColor(value))
    }
