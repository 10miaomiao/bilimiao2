package com.a10miaomiao.miaoandriod.binding

import android.arch.lifecycle.LiveData
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

inline fun <T> MiaoBinding.bind(v: KProperty0<T>, crossinline fn: ((value: T) -> Unit)) {
    var fn1: () -> Unit = {
        fn(v.get())
    }
    fn1()
    bindData(fn1, v.key)
}


inline fun MiaoBinding.bind(noinline fn: (() -> Unit)) {
    fn()
    bindData(fn, "all")
}

inline val <T> KProperty<T>.key get() = this.toString()