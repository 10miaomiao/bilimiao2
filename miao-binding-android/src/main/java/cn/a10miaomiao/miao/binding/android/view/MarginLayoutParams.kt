package cn.a10miaomiao.miao.binding.android.view

import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import cn.a10miaomiao.miao.binding.miaoEffect

inline var ViewGroup.MarginLayoutParams._height: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        height = value
    }

inline var ViewGroup.MarginLayoutParams._width: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        width = value
    }
inline var ViewGroup.MarginLayoutParams._margin: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        setMargins(value, value, value, value)
    }

inline var ViewGroup.MarginLayoutParams._horizontalMargin: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        leftMargin = value
        rightMargin = value
    }

inline var ViewGroup.MarginLayoutParams._verticalMargin: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        topMargin = value
        bottomMargin = value
    }

inline var ViewGroup.MarginLayoutParams._topMargin: Int
    get() = topMargin
    set(@Px value) = miaoEffect(value) {
        topMargin = value
    }

inline var ViewGroup.MarginLayoutParams._bottomMargin: Int
    get() = bottomMargin
    set(@Px value) = miaoEffect(value) {
        bottomMargin = value
    }

inline var ViewGroup.MarginLayoutParams._leftMargin: Int
    get() = leftMargin
    set(@Px value) = miaoEffect(value) {
        leftMargin = value
    }