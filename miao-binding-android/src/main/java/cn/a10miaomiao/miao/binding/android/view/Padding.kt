package cn.a10miaomiao.miao.binding.android.view

import android.os.Build.VERSION.SDK_INT
import android.view.View
import androidx.annotation.Px
import cn.a10miaomiao.miao.binding.miaoEffect
import kotlin.DeprecationLevel.HIDDEN

inline var View._padding: Int
    @Deprecated(NO_GETTER, level = HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        setPadding(value, value, value, value)
    }

inline var View._horizontalPadding: Int
    @Deprecated(NO_GETTER, level = HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        setPadding(value, paddingTop, value, paddingBottom)
    }

inline var View._verticalPadding: Int
    @Deprecated(NO_GETTER, level = HIDDEN) get() = noGetter
    set(@Px value) = miaoEffect(value) {
        setPadding(paddingLeft, value, paddingRight, value)
    }

inline var View._topPadding: Int
    get() = paddingTop
    set(@Px value) = miaoEffect(value) {
        setPadding(paddingLeft, value, paddingRight, paddingBottom)
    }

inline var View._bottomPadding: Int
    get() = paddingBottom
    set(@Px value) = miaoEffect(value) {
        setPadding(paddingLeft, paddingTop, paddingRight, value)
    }

inline var View._leftPadding: Int
    get() = paddingLeft
    set(@Px value) = miaoEffect(value) {
        setPadding(value, paddingTop, paddingRight, paddingBottom)
    }

inline var View._rightPadding: Int
    get() = paddingRight
    set(@Px value) = miaoEffect(value) {
        setPadding(paddingLeft, paddingTop, value, paddingBottom)
    }
