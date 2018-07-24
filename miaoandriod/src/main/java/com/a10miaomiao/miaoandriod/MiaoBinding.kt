package com.a10miaomiao.miaoandriod

import android.util.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

interface MiaoBinding {
    fun bindData(fn: (() -> Unit), key: String)
    fun updateView(key: String)
    fun <T> miao(initialValue: T) = MiaoObservableProperty(this, initialValue)
}

open class MiaoBindingImpl : MiaoBinding {
    var bindFns: Map<String, List<() -> Unit>> = mapOf()

    override fun bindData(fn: () -> Unit, key: String) {
        var mfuns = bindFns.toMutableMap()
        if (bindFns.containsKey(key)) {
            var fs = bindFns[key]!!.toMutableList()
            fs.add(fn)
            mfuns[key] = fs
        } else {
            mfuns[key] = arrayListOf(fn)
        }
        bindFns = mfuns
    }

    override fun updateView(key: String) {
        bindFns[key]?.forEach { it() }
        bindFns["all"]?.forEach { it() }
    }

    fun updateView() {
        bindFns.forEach { it.value.forEach { it() }}
    }
}


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