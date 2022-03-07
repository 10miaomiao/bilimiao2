package cn.a10miaomiao.miao.binding.android.widget

import android.view.View
import android.widget.CompoundButton
import cn.a10miaomiao.miao.binding.android.view.NO_GETTER
import cn.a10miaomiao.miao.binding.android.view.noGetter
import cn.a10miaomiao.miao.binding.miaoEffect

inline var CompoundButton._isChecked: Boolean
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        isChecked = it
    }