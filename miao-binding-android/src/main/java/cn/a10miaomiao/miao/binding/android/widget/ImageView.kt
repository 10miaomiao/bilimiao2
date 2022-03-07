package cn.a10miaomiao.miao.binding.android.widget

import android.widget.ImageView
import androidx.annotation.DrawableRes
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect

inline var ImageView._imageResource: Int
    get() { throw BindingOnlySetException() }
    set(@DrawableRes value) = miaoEffect(value) {
        setImageResource(value)
    }
