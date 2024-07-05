package cn.a10miaomiao.miao.binding.android.view

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect


inline var View._visibility: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        visibility = it
    }

inline var View._show: Boolean
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        visibility = if (it) View.VISIBLE else View.GONE
    }

inline var View._isEnabled: Boolean
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        isEnabled = it
    }

inline var View._tag: Any
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        tag = it
    }

inline var View._background: Drawable
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        background = value
    }

inline var View._backgroundColor: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        setBackgroundColor(value)
    }

inline var View._backgroundResource: Int
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        setBackgroundResource(value)
    }

inline var View._tooltipText: String
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tooltipText = value
        }
    }

inline var View._contentDescription: String
    @Deprecated(NO_GETTER, level = DeprecationLevel.HIDDEN) get() = noGetter
    set(value) = miaoEffect(value) {
        contentDescription = value
    }




