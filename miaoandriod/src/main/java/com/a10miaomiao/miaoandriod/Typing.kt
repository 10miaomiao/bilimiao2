package com.a10miaomiao.miaoandriod

import android.arch.lifecycle.LifecycleOwner


typealias MiaoObserver<T> = LifecycleOwner.(observer: (t: T) -> Unit) -> Unit
typealias MiaoObserverAll = LifecycleOwner.(observer: () -> Unit) -> Unit

typealias ValueManager<T> = (f: ((v: T) -> Unit)) -> Unit

fun <T> T.v(): ValueManager<T> {
    return { f -> f(this) }
}